package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.RepositoryAccess;
import jp.ac.titech.c.phph.diff.Differencer;
import jp.ac.titech.c.phph.diff.DynamicProgrammingDifferencer;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.model.Statement;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ChunkExtractor {

    final Differencer<Statement> differencer = new DynamicProgrammingDifferencer<>();

    final StatementExtractor statementExtractor = new MPAStatementExtractor();

    final RepositoryAccess ra;

    public ChunkExtractor(final RepositoryAccess ra) {
        this.ra = ra;
    }

    /**
     * Processes a commit and obtains a list of changes.
     */
    public List<Chunk> process(final RevCommit c) {
        final List<DiffEntry> entries = ra.getChanges(c);
        log.debug("{}: {} changed files", c.getId().name(), entries.size());
        return entries.stream()
                .filter(e -> isSupportedFileChange(e, "java"))
                .flatMap(e -> extractChanges(e))
                .collect(Collectors.toList());
    }

    /**
     * Checks the filteriing criteria.
     */
    protected boolean isSupportedFileChange(final DiffEntry e, final String extension) {
        final String oldPath = e.getOldPath();
        final String newPath = e.getNewPath();
        final String oldExtension = FilenameUtils.getExtension(oldPath);
        final String newExtension = FilenameUtils.getExtension(newPath);
        return oldPath.equals(newPath) && extension.equals(oldExtension) && extension.equals(newExtension);
    }

    /**
     * Extracts a stream of changes from a DiffEntry.
     */
    protected Stream<Chunk> extractChanges(final DiffEntry entry) {
        final String oldSource = ra.readBlob(entry.getOldId().toObjectId());
        final String newSource = ra.readBlob(entry.getNewId().toObjectId());
        final List<Statement> oldStatements = statementExtractor.extractStatements(oldSource);
        final List<Statement> newStatements = statementExtractor.extractStatements(newSource);
        return differencer.compute(oldStatements, newStatements).stream()
                .map(e -> Chunk.of(entry.getNewPath(), oldStatements, newStatements, e));
    }
}
