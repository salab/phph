package jp.ac.titech.c.phph;

import jp.ac.titech.c.phph.model.Change;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.parse.ChangeExtractor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.revwalk.RevCommit;
import picocli.CommandLine.*;
import yoshikihigo.cpanalyzer.CPAConfig;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Log4j2
@Command(name = "extract", description = "Extract commits from a repository")
public class ExtractCommand implements Callable<Integer> {
    @ParentCommand
    Application app;

    public static class Config {
        @Option(names = {"-r", "--repository"}, paramLabel = "<repo>", description = "repository path")
        Path repository = Path.of(".git");

        @Option(names = {"-f", "--database"}, paramLabel = "<db>", description = "database file path")
        Path database = Path.of("phph.db");

        @Option(names = "--from", paramLabel = "<rev>", description = "Revision to skip go further (exclusive)")
        String from;

        @Option(names = "--end", paramLabel = "<rev>", description = "Revision to start traversing (default: ${DEFAULT-VALUE})")
        String to = "HEAD";
    }

    @Mixin
    Config config = new Config();

    static {
        CPAConfig.initialize(new String[] {"-n", "-cs", "10"});
    }

    @Override
    public Integer call() {
        process(config.repository, config.from, config.to);
        log.debug("Finished");
        return 0;
    }

    private void process(final Path repositoryPath, final String from, final String to) {
        try (final RepositoryAccess ra = new RepositoryAccess(repositoryPath)) {
            log.info("Process {}", repositoryPath);
            final ChangeExtractor extractor = new ChangeExtractor(ra);
            for (final RevCommit c : ra.walk(from, to)) {
                if (c.getParentCount() > 1) {
                    log.debug("Skip {} (merge commit)", c.getId().name());
                } else {
                    log.debug("Process {}", c.getId().name());
                    process(c, extractor);
                }
            }
        }
    }

    private void process(final RevCommit c, final ChangeExtractor extractor) {
        for (final Change chg : extractor.process(c)) {
            final Pattern p = chg.toPattern();
            log.debug("[{}] {} at {}:{}", p.getHash().abbreviate(6).name(), p, chg.getFile(), chg.getNewOffset());
        }
    }
}
