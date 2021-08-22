package jp.ac.titech.c.phph;

import com.google.common.base.Stopwatch;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.db.Database;
import lombok.extern.log4j.Log4j2;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Log4j2
@Command(name = "measure", description = "Compute metric values")
public class MeasureCommand implements Callable<Integer> {
    public static class Config {
        @Option(names = {"-f", "--database"}, paramLabel = "<db>", description = "database file path")
        Path database = Path.of("phph.db");
    }

    @Mixin
    Config config = new Config();

    @Override
    public Integer call() {
        final Stopwatch w = Stopwatch.createStarted();
        try {
            final Jdbi jdbi = Database.openDatabase(config.database);
            try (final Handle h = jdbi.open()) {
                h.useTransaction(h0 -> process(h.attach(Dao.class)));
            }
        } finally {
            log.info("Finished -- {} ms", w.elapsed(TimeUnit.MILLISECONDS));
        }
        return 0;
    }

    private void process(final Dao dao) {
        log.debug("Compute chunk-based support...");
        dao.computeSupportH();
        log.debug("Compute commit-based support...");
        dao.computeSupportC();
        log.debug("Compute chunk-based confidence...");
        dao.computeConfidenceH();
        log.debug("Compute commit-based confidence...");
        dao.computeConfidenceC();
    }
}
