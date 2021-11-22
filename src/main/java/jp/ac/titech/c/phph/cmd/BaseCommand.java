package jp.ac.titech.c.phph.cmd;

import com.google.common.base.Stopwatch;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.db.Database;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BaseCommand implements Callable<Integer> {
    @ParentCommand
    protected AppCommand app;

    protected Handle handle;

    protected Dao dao;

    @Override
    public Integer call() {
        final Stopwatch w = Stopwatch.createStarted();
        try {
            setUp();
            final Jdbi jdbi = Database.openDatabase(app.config.database);
            try (final Handle h = jdbi.open()) {
                h.useTransaction(h0 -> {
                    this.handle = h0;
                    this.dao = h0.attach(Dao.class);
                    try {
                        process();
                    } catch (final Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.debug("Finished - {} ms", w.elapsed(TimeUnit.MILLISECONDS));
        }
        return 0;
    }

    protected void setUp() throws Exception {}

    protected abstract void process() throws Exception;
}
