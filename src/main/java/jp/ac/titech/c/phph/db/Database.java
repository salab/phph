package jp.ac.titech.c.phph.db;

import lombok.extern.log4j.Log4j2;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.stream.Stream;

@Log4j2
public class Database {
    public static final String SCHEMA_FILENAME = "/schema.sql";

    public static final String SCHEMA_PATH = Database.class.getResource(SCHEMA_FILENAME).getPath();

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (final ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Jdbi openDatabase(final Path path) {
        final String uri = "jdbc:sqlite:" + path.toString();
        log.debug("Opening JDBC URI: {}", uri);
        final Jdbi jdbi = Jdbi.create(uri);
        jdbi.setSqlLogger(new Logger());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    public static void initializeDatabase(final Handle h) throws IOException {
        final String schema = Files.readString(Paths.get(SCHEMA_PATH));
        Stream.of(schema.split(";"))
                .map(s -> s.trim().replaceAll("\\n\\s*", " "))
                .filter(s -> !s.isEmpty())
                .forEach(h::execute);
        log.info("Tables created");
    }

    public static class Logger implements SqlLogger {
        @Override
        public void logBeforeExecution(final StatementContext c) {
            if (log.isTraceEnabled()) {
                if (c.getBinding().isEmpty()) {
                    log.trace("[SQL] {}", c.getParsedSql().getSql());
                } else {
                    log.trace("[SQL] {} << {}", c.getParsedSql().getSql(), c.getBinding());
                }
            }
        }

        @Override
        public void logException(final StatementContext c, final SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
