package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.db.Database;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;

@Log4j2
@Command(name = "init", description = "Initialize database")
public class InitCommand extends BaseCommand {
    @Override
    protected void setUp() throws IOException {
        Files.deleteIfExists(app.config.database);
    }

    @Override
    protected void process() throws IOException {
        Database.initializeDatabase(handle);
    }
}
