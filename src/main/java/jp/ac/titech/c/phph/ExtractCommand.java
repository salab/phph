package jp.ac.titech.c.phph;

import com.google.common.base.Stopwatch;
import jp.ac.titech.c.phph.db.Database;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.parse.ChunkExtractor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import yoshikihigo.cpanalyzer.CPAConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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

    Handle handle;

    Dao dao;

    static {
        CPAConfig.initialize(new String[] {"-n", "-cs", "10"});
    }

    @Override
    public Integer call() {
        Stopwatch w = Stopwatch.createStarted();
        final Jdbi jdbi = initializeDatabase();
        try (final Handle h = jdbi.open()) {
            this.handle = h;
            this.dao = h.attach(Dao.class);
            h.useTransaction(h0 -> process(config.repository, config.from, config.to));
            this.dao = null;
            this.handle = null;
        }
        log.info("Finished -- {} ms", w.elapsed(TimeUnit.MILLISECONDS));
        return 0;
    }

    private Jdbi initializeDatabase() {
        try {
            Files.deleteIfExists(config.database);
            Database.initializeDatabase(config.database);
        } catch (final IOException e) {
            log.error(e);
        }
        return Database.openDatabase(config.database);
    }

    private void process(final Path repositoryPath, final String from, final String to) {
        try (final RepositoryAccess ra = new RepositoryAccess(repositoryPath)) {
            log.info("Process {}", repositoryPath);
            final long repoId = dao.insertRepository(repositoryPath.toString());
            final ChunkExtractor extractor = new ChunkExtractor(ra);
            for (final RevCommit c : ra.walk(from, to)) {
                process(c, repoId, extractor);
            }
        }
    }

    private void process(final RevCommit c, final long repoId, final ChunkExtractor extractor) {
        final List<Chunk> chunks = extractor.extract(c);
        if (!chunks.isEmpty()) {
            final long commitId = dao.insertCommit(repoId, c.getId().name(), c.getFullMessage());
            for (final Chunk h : chunks) {
                final Pattern p = h.toPattern();
                if (log.isDebugEnabled()) {
                    log.debug("[{}] {} at {}:{} in {}",
                            p.getSummary(), p, h.getFile(), h.getNewBegin(), c.getId().name());
                }
                dao.insertFragment(p.getOldFragment());
                dao.insertFragment(p.getNewFragment());
                dao.insertPattern(p);
                dao.insertChunk(commitId, h, p);
            }
        }
    }
}
