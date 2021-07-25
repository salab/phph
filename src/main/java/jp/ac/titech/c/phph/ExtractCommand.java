package jp.ac.titech.c.phph;

import jp.ac.titech.c.phph.diff.DiffUtils;
import jp.ac.titech.c.phph.diff.Differencer;
import jp.ac.titech.c.phph.diff.DynamicProgrammingDifferencer;
import jp.ac.titech.c.phph.model.Statement;
import jp.ac.titech.c.phph.model.Revision;
import jp.ac.titech.c.phph.parse.MPAStatementExtractor;
import jp.ac.titech.c.phph.parse.StatementExtractor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.revwalk.RevCommit;
import picocli.CommandLine.*;
import yoshikihigo.cpanalyzer.CPAConfig;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

@Log4j2
@Command(name = "extract", description = "Extract commits from a repository")
public class ExtractCommand implements Callable<Integer> {
    @ParentCommand
    Application app;

    public static class Config {
        @Option(names = {"-r", "--repository"}, paramLabel = "<repo>", description = "repository path")
        String repository;

        @Option(names = "--from", paramLabel = "<rev>", description = "Revision to skip go further (exclusive)")
        String from;

        @Option(names = "--end", paramLabel = "<rev>", description = "Revision to start traversing (default: ${DEFAULT-VALUE})")
        String to = "HEAD";
    }

    @Mixin
    Config config = new Config();

    final Differencer<Statement> differencer = new DynamicProgrammingDifferencer<>();

    final StatementExtractor extractor = new MPAStatementExtractor();

    static {
        CPAConfig.initialize(new String[] {"-n", "-cs", "10"});
    }

    @Override
    public Integer call() {
        try (final RepositoryAccess ra = new RepositoryAccess(config.repository)) {
            log.info("Repository: {}", config.repository);
            for (final RevCommit c : ra.walk(config.from, config.to)) {
                if (isMergeCommit(c)) {
                    log.debug("{}: skipped (merge commit)", c.getId().name());
                    continue;
                }

                final List<DiffEntry> entries = ra.getChanges(c);
                log.debug("{}: {} file changes", c.getId().name(), entries.size());
                entries.stream().filter(e -> isSupportedFileChange(e, "java")).forEach(e -> {
                    log.debug("-- change: {}", e.getNewPath());
                    final EditList edits = extractChange(ra, e);
                });
            }
        }
        return 0;
    }

    private boolean isMergeCommit(final RevCommit c) {
        return c.getParentCount() > 1;
    }

    private Revision createRevision(final RevCommit c) {
        final String id = c.getId().getName();
        final String message = c.getFullMessage();
        final String author = c.getAuthorIdent().getName();
        final Date date = new Date(c.getCommitTime());
        return new Revision(id, message, author, date);
    }

    private boolean isSupportedFileChange(final DiffEntry e, final String extension) {
        final String oldPath = e.getOldPath();
        final String newPath = e.getNewPath();
        final String oldExtension = FilenameUtils.getExtension(oldPath);
        final String newExtension = FilenameUtils.getExtension(newPath);
        return extension.equals(oldExtension) && extension.equals(newExtension);
    }

    private EditList extractChange(final RepositoryAccess ra, final DiffEntry entry) {
        final String beforeText = ra.readBlob(entry.getOldId().toObjectId());
        final String afterText = ra.readBlob(entry.getNewId().toObjectId());
        final List<Statement> beforeStatements = extractor.extractStatements(beforeText);
        final List<Statement> afterStatements = extractor.extractStatements(afterText);
        final EditList edits = differencer.compute(beforeStatements, afterStatements);
        log.debug("{}", DiffUtils.toString(edits, beforeStatements, afterStatements));
        return edits;
    }
}
