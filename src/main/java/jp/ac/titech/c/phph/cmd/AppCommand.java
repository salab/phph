package jp.ac.titech.c.phph.cmd;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import ch.qos.logback.classic.Level;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Slf4j
@Command(version = "phph", sortOptions = false, subcommandsRepeatable = true,
         subcommands = {InitCommand.class, ExtractCommand.class, MeasureCommand.class, FindCommand.class,
                        ShowCommand.class, VerifyCommand.class, RegisterOnlyCommand.class, BrowseCommand.class})
public class AppCommand implements Callable<Integer> {
    public static class LevelConverter implements ITypeConverter<Level> {
        @Override
        public Level convert(final String value) {
            return Level.valueOf(value);
        }
    }

    public static class Config {
        @Option(names = "--log", paramLabel = "<level>", description = "log level (default: ${DEFAULT-VALUE})",
                converter = LevelConverter.class)
        Level logLevel = Level.INFO;

        @Option(names = { "-q", "--quiet" }, description = "quiet mode (same as --log=ERROR)")
        void setQuiet(final boolean isQuiet) {
            if (isQuiet) {
                logLevel = Level.ERROR;
            }
        }

        @Option(names = { "-v", "--verbose" }, description = "verbose mode (same as --log=DEBUG)")
        void setVerbose(final boolean isVerbose) {
            if (isVerbose) {
                logLevel = Level.DEBUG;
            }
        }

        @Option(names = "--help", description = "show this help message and exit",
                usageHelp = true)
        boolean helpRequested;

        @Option(names = "--version", description = "print version information and exit",
                versionHelp = true)
        boolean versionInfoRequested;

        @Option(names = {"-f", "--database"}, paramLabel = "<db>", description = "database file path")
        Path database = Path.of("phph.db");

        @Option(names = {"-m", "--matches-database"}, paramLabel = "<db>", description = "matches database file path")
        Path matchesDatabase = Path.of("phph-m.db");
    }

    @Mixin
    protected Config config = new Config();

    @Override
    public Integer call() {
        setLoggerLevel(Logger.ROOT_LOGGER_NAME, config.logLevel);
        if (config.logLevel == Level.DEBUG || config.logLevel == Level.TRACE) {
            // suppress logs of external libraries
            setLoggerLevel("org.eclipse.jgit", Level.INFO);
            setLoggerLevel("org.snt.inmemantlr", Level.INFO);
        }

        log.debug("Set log level to {}", config.logLevel);
        return 0;
    }

    public static void setLoggerLevel(final String name, final Level level) {
        final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
        logger.setLevel(level);
        log.debug("Set log level of {} to {}", name, level);
    }
}
