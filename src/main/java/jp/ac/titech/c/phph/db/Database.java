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
            e.printStackTrace();
        }
    }

    public static Jdbi openDatabase(final Path path) {
        final Jdbi jdbi = Jdbi.create("jdbc:sqlite:" + path.toString());
        jdbi.setSqlLogger(new Logger());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    public static void initializeDatabase(final Path path) throws IOException {
        Jdbi jdbi = openDatabase(path);
        final String schema = Files.readString(Paths.get(SCHEMA_PATH));
        try (final Handle h = jdbi.open()) {
            Stream.of(schema.split(";"))
                    .map(s -> s.trim().replaceAll("\\n\\s*", " ")) // TODO: -- comment
                    .filter(s -> !s.isEmpty())
                    .forEach(h::execute);
            log.info("{}: Tables created", path);
        }
    }

    public static class Logger implements SqlLogger {
        @Override
        public void logBeforeExecution(final StatementContext c) {
            if (log.isDebugEnabled()) {
                if (c.getBinding().isEmpty()) {
                    log.debug("[SQL] {}", c.getParsedSql().getSql());
                } else {
                    log.debug("[SQL] {} << {}", c.getParsedSql().getSql(), c.getBinding());
                }
            }
        }

        @Override
        public void logException(final StatementContext c, final SQLException e) {
            log.error(e);
        }
    }
}
