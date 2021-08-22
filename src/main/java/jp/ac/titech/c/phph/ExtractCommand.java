package jp.ac.titech.c.phph;

import com.github.durun.nitron.core.config.LangConfig;
import com.github.durun.nitron.core.config.NitronConfig;
import com.github.durun.nitron.core.config.loader.NitronConfigLoader;
import com.google.common.base.Stopwatch;
import jp.ac.titech.c.phph.db.Database;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.parse.ChunkExtractor;
import jp.ac.titech.c.phph.parse.MPASplitter;
import jp.ac.titech.c.phph.parse.NitronSplitter;
import jp.ac.titech.c.phph.parse.Splitter;
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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Log4j2
@Command(name = "extract", description = "Extract commits from a repository")
public class ExtractCommand implements Callable<Integer> {
    @ParentCommand
    Application app;

    public enum SplitterType { mpa, nitron }

    public static class Config {
        @Option(names = {"-r", "--repository"}, paramLabel = "<repo>", description = "repository path")
        Path repository = Path.of(".git");

        @Option(names = {"-f", "--database"}, paramLabel = "<db>", description = "database file path")
        Path database = Path.of("phph.db");

        @Option(names = { "-p", "--parallel" }, paramLabel = "<nthreads>", description = "number of threads to use in parallel",
                arity = "0..1", fallbackValue = "0")
        int nthreads = 1;

        @Option(names = "--from", paramLabel = "<rev>", description = "Revision to skip go further (exclusive)")
        String from;

        @Option(names = "--end", paramLabel = "<rev>", description = "Revision to start traversing (default: ${DEFAULT-VALUE})")
        String to = "HEAD";

        @Option(names= "--splitter", description = "Available: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
        SplitterType splitter = SplitterType.mpa;
    }

    @Mixin
    Config config = new Config();

    Splitter splitter;

    Handle handle;

    Dao dao;

    @Override
    public Integer call() {
        final Stopwatch w = Stopwatch.createStarted();
        try {
            this.splitter = createSplitter(config.splitter);

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

    private Splitter createSplitter(final SplitterType type) {
        switch (type) {
            case mpa:
                return new MPASplitter();
            case nitron:
                final Path path = Path.of(ClassLoader.getSystemResource("nitronConfig/nitron.json").getPath());
                final NitronConfig nitronConfig = NitronConfigLoader.INSTANCE.load(path);
                final LangConfig langConfig = Objects.requireNonNull(nitronConfig.getLangConfig().get("java-jdt"));
                return new NitronSplitter(langConfig);
            default:
                assert false;
                return null;
        }
    }

    private void process(final Path repositoryPath, final String from, final String to) {
        try (final RepositoryAccess ra = new RepositoryAccess(repositoryPath)) {
            log.info("Process {}", repositoryPath);
            final long repoId = dao.insertRepository(repositoryPath.toString());

            final TaskQueue<Dao> queue = new TaskQueue<>(config.nthreads);
            for (final RevCommit c : ra.walk(from, to)) {
                final ChunkExtractor extractor = new ChunkExtractor(splitter, ra.inherit());
                queue.register(() -> process(c, repoId, extractor));
            }
            queue.consumeAll(dao);
        }
    }

    private Consumer<Dao> process(final RevCommit c, final long repoId, final ChunkExtractor extractor) {
        final List<Chunk> chunks = extractor.extract(c);
        if (chunks.isEmpty()) {
            return (dao) -> {};
        }

        // pre-computes patterns
        for (final Chunk h : chunks) {
            final Pattern p = h.getPattern();
            if (log.isDebugEnabled()) {
                log.debug("[{}@{}] {} --> {} at {}:{} in {}",
                        p.toShortString(), c.getId().abbreviate(6).name(),
                        h.getOldFragment(), h.getNewFragment(), h.getFile(), h.getNewBegin(), c.getId().name());
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
}
