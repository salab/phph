package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.db.Database;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;

@Slf4j
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
