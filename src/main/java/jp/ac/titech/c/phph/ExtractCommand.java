package jp.ac.titech.c.phph;

import com.google.common.base.Stopwatch;
import jp.ac.titech.c.phph.db.Database;
import jp.ac.titech.c.phph.diff.Differencer;
import jp.ac.titech.c.phph.diff.DifferencerFactory;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.model.Statement;
import jp.ac.titech.c.phph.parse.ChunkExtractor;
import jp.ac.titech.c.phph.parse.Splitter;
import jp.ac.titech.c.phph.parse.SplitterFactory;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

        @Option(names = {"-p", "--parallel"}, paramLabel = "<nthreads>", description = "number of threads to use in parallel",
                arity = "0..1", fallbackValue = "0")
        int nthreads = 1;

        @Option(names = "--from", paramLabel = "<rev>", description = "Revision to skip go further (exclusive)")
        String from;

        @Option(names = "--end", paramLabel = "<rev>", description = "Revision to start traversing (default: ${DEFAULT-VALUE})")
        String to = "HEAD";

        @Option(names = "--differencer", description = "Available: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
        DifferencerFactory.Type differencer = DifferencerFactory.Type.dp;

        @Option(names = "--splitter", description = "Available: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
        SplitterFactory.Type splitter = SplitterFactory.Type.mpa;

        @Option(names = "--min-size", description = "Minimum chunk size (default: ${DEFAULT-VALUE})")
        int minChunkSize = 0;

        @Option(names = "--max-size", description = "Maximum chunk size (default: Integer.MAX_VALUE)")
        int maxChunkSize = Integer.MAX_VALUE;
    }

    @Mixin
    Config config = new Config();

    Handle handle;

    Dao dao;

    ChunkExtractor extractor;

    @Override
    public Integer call() {
        final Stopwatch w = Stopwatch.createStarted();
        try {
            setupChunkExtractor();
            Files.deleteIfExists(config.database);
            final Jdbi jdbi = Database.openDatabase(config.database);
            try (final Handle h = this.handle = jdbi.open()) {
                Database.initializeDatabase(h);
                this.dao = h.attach(Dao.class);
                h.useTransaction(h0 -> process(config.repository, config.from, config.to));
            }
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("Finished -- {} ms", w.elapsed(TimeUnit.MILLISECONDS));
        }
        return 0;
    }

    private void process(final Path repositoryPath, final String from, final String to) {
        try (final RepositoryAccess ra = new RepositoryAccess(repositoryPath)) {
            log.info("Process {}", repositoryPath);
            final long repoId = dao.insertRepository(repositoryPath.toString());

            final TaskQueue<Dao> queue = new TaskQueue<>(config.nthreads);
            for (final RevCommit c : ra.walk(from, to)) {
                queue.register(() -> process(c, repoId, ra.inherit()));
            }
            queue.consumeAll(dao);
        }
    }

    private Consumer<Dao> process(final RevCommit c, final long repoId, final RepositoryAccess ra) {
        final List<Chunk> chunks = extractor.extract(c, ra);
        if (chunks.isEmpty()) {
            return (dao) -> {};
        }

        // pre-computes patterns
        for (final Chunk h : chunks) {
            final Pattern p = h.getPattern();
            if (log.isDebugEnabled()) {
                log.debug("[{}@{}] {} --> {} at {}:{}",
                        p.toShortString(), c.getId().abbreviate(6).name(),
                        h.getOldFragment(), h.getNewFragment(), h.getFile(), h.getNewBegin());
            }
        }
        return (dao) -> {
            final long commitId = dao.insertCommit(repoId, c.getId().name(), c.getFullMessage());
            for (final Chunk h : chunks) {
                dao.insertFragment(h.getOldFragment());
                dao.insertFragment(h.getNewFragment());
                dao.insertPattern(h.getPattern());
                dao.insertChunk(commitId, h);
            }
        };
    }

    private void setupChunkExtractor() {
        final Differencer<Statement> differencer = DifferencerFactory.create(config.differencer);
        final Splitter splitter = SplitterFactory.create(config.splitter);
        this.extractor = new ChunkExtractor(differencer, splitter, config.minChunkSize, config.maxChunkSize);
    }
}
