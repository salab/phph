package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.util.RepositoryAccess;
import jp.ac.titech.c.phph.util.TaskQueue;
import jp.ac.titech.c.phph.diff.Differencer;
import jp.ac.titech.c.phph.diff.DifferencerFactory;
import jp.ac.titech.c.phph.model.Chunk;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.model.Statement;
import jp.ac.titech.c.phph.parse.ChunkExtractor;
import jp.ac.titech.c.phph.parse.Splitter;
import jp.ac.titech.c.phph.parse.SplitterFactory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Command(name = "extract", description = "Extract commits from a repository")
public class ExtractCommand extends BaseCommand {

    public static class Config {
        @Option(names = {"-r", "--repository"}, paramLabel = "<repo>", description = "repository path")
        Path repository = Path.of(".git");

        @Option(names = {"-p", "--parallel"}, paramLabel = "<nthreads>", description = "number of threads to use in parallel",
                arity = "0..1", fallbackValue = "0")
        int nthreads = 1;

        @Option(names = "--from", paramLabel = "<rev>", description = "Revision to skip go further (exclusive)")
        String from;

        @Option(names = "--end", paramLabel = "<rev>", description = "Revision to start traversing (default: ${DEFAULT-VALUE})")
        String to = "HEAD";

        @Option(names = "--differencer", paramLabel = "<type>", description = "Available: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
        DifferencerFactory.Type differencer = DifferencerFactory.Type.myers;

        @Option(names = "--splitter", paramLabel = "<type>", description = "Available: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
        SplitterFactory.Type splitter = SplitterFactory.Type.mpa;

        @Option(names = "--non-essential", description = "Use non-essential statements", negatable = true)
        boolean useNonEssential = false;

        @Option(names = "--min-size", paramLabel = "<n>", description = "Minimum chunk size (default: ${DEFAULT-VALUE})")
        int minChunkSize = 0;

        @Option(names = "--max-size", paramLabel = "<n>", description = "Maximum chunk size (default: Integer.MAX_VALUE)")
        int maxChunkSize = Integer.MAX_VALUE;
    }

    @Mixin
    Config config = new Config();

    ChunkExtractor extractor;

    @Override
    protected void setUp() {
        this.extractor = createChunkExtractor();
    }

    @Override
    protected void process() {
        try (final RepositoryAccess ra = new RepositoryAccess(config.repository)) {
            log.info("Process {}", config.repository);
            final long repoId = dao.insertRepository(config.repository.toString());

            final TaskQueue<Dao> queue = new TaskQueue<>(config.nthreads);
            for (final RevCommit c : ra.walk(config.from, config.to)) {
                queue.register(() -> process(c, repoId, ra.inherit()));
            }
            queue.consumeAll(dao);
        }
    }

    protected Consumer<Dao> process(final RevCommit c, final long repoId, final RepositoryAccess ra) {
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
                        h.getOldFragment(), h.getNewFragment(), h.getFile(), h.getNewLines().getBegin());
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

    private ChunkExtractor createChunkExtractor() {
        final Differencer<Statement> differencer = DifferencerFactory.create(config.differencer);
        final Splitter splitter = SplitterFactory.create(config.splitter, config.useNonEssential);
        return new ChunkExtractor(differencer, splitter, config.minChunkSize, config.maxChunkSize);
    }
}
