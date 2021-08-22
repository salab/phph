package jp.ac.titech.c.phph;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.db.Database;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Hash;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Query;
import jp.ac.titech.c.phph.parse.Splitter;
import jp.ac.titech.c.phph.parse.SplitterFactory;
import lombok.extern.log4j.Log4j2;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Log4j2
@Command(name = "find", description = "Find change opportunities")
public class FindCommand implements Callable<Integer> {
    @ParentCommand
    Application app;

    public static class Config {
        @Option(names = {"-r", "--repository"}, paramLabel = "<repo>", description = "repository path")
        Path repository = Path.of(".git");

        @Option(names = {"-a", "--at"}, paramLabel = "<revision>", description = "revision to retrieve (default: ${DEFAULT-VALUE})")
        String revision = "HEAD";

        @Option(names = {"-f", "--database"}, paramLabel = "<db>", description = "database file path")
        Path database = Path.of("phph.db");

        @Option(names = { "-p", "--parallel" }, paramLabel = "<nthreads>", description = "number of threads to use in parallel",
                arity = "0..1", fallbackValue = "0")
        int nthreads = 1;

        @Option(names = "--splitter", description = "Available: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
        SplitterFactory.Type splitter = SplitterFactory.Type.mpa;

        @Option(names = "--prefix", paramLabel = "<path>", description = "path prefix")
        String prefix = null;
    }

    @Mixin
    Config config = new Config();

    Handle handle;

    Dao dao;

    Map<String, String> sources;

    SetMultimap<Hash, SourceFile> finder;

    @Override
    public Integer call() {
        final Stopwatch w = Stopwatch.createStarted();
        try {
            final Jdbi jdbi = Database.openDatabase(config.database);
            try (final Handle h = this.handle = jdbi.open()) {
                this.dao = h.attach(Dao.class);
                h.useTransaction(h0 -> process());
            }
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("Finished -- {} ms", w.elapsed(TimeUnit.MILLISECONDS));
        }
        return 0;
    }

    protected void process() throws IOException {
        log.debug("Retrieving source code...");
        this.sources = FileTree.retrieveGitTree(config.repository, config.revision, config.prefix, ".java");
        this.finder = buildFinder();

        log.debug("Clearing matching table...");
        dao.clearMatches();

        log.debug("Matching queries...");
        final TaskQueue<Dao> queue = new TaskQueue<>(config.nthreads);
        for (final Fragment fragment : dao.listFragments()) {
            queue.register(() -> process(fragment));
        }
        queue.consumeAll(dao);
    }

    protected Consumer<Dao> process(final Fragment fragment) {
        final Query query = fragment.toQuery();
        if (query.isEmpty()) {
            return (dao) -> {};
        }
        if (log.isTraceEnabled()) {
            log.trace("Querying {} = [{}]", fragment.getHash().abbreviate(6), query);
        }

        final List<Match> result = new ArrayList<>();
        for (final SourceFile file : getCandidateFiles(query)) {
            for (final Match match : file.find(query)) {
                if (log.isDebugEnabled()) {
                    log.debug("{} matched at {}:{}", query.getFragment().getHash().abbreviate(6), file.getPath(), match.getBeginLine());
                }
                result.add(match);
            }
        }
        return (dao) -> {
            for (final Match match : result) {
                dao.insertMatch(match);
            }
        };
    }

    /**
     * Obtains a set of source files that at least contain all of the hashes in the given query.
     */
    protected SetMultimap<Hash, SourceFile> buildFinder() {
        final SetMultimap<Hash, SourceFile> result = HashMultimap.create();
        final Splitter splitter = SplitterFactory.create(config.splitter);

        for (final Map.Entry<String, String> e : sources.entrySet()) {
            final String path = e.getKey();
            final String source = e.getValue();
            log.debug("Splitting {}", path);
            final SourceFile file = new SourceFile(Path.of(path), splitter.split(source));
            for (final Hash h : file.getHashes()) {
                result.put(h, file);
            }
        }
        return result;
    }

    /**
     * Obtains a set of source files that at least contain all of the hashs in the given query.
     */
    protected Set<SourceFile> getCandidateFiles(final Query query) {
        if (query.size() == 1) {
            return finder.get(query.get(0));
        } else {
            final Set<SourceFile> result = new HashSet<>(finder.get(query.get(0)));
            for (int i = 1; i < query.size(); i++) {
                result.retainAll(finder.get(query.get(i)));
            }
            return result;
        }
    }
}
