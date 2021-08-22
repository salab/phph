package jp.ac.titech.c.phph;

import java.util.concurrent.Callable;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.RunAll;

@Log4j2
@Command(version = "phph", sortOptions = false,
         subcommands = {ExtractCommand.class, MeasureCommand.class})
public class Application implements Callable<Integer> {
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
    }

    @Mixin
    Config config = new Config();

    @Override
    public Integer call() {
        Configurator.setRootLevel(config.logLevel);
        if (config.logLevel == Level.DEBUG || config.logLevel == Level.TRACE) {
            // suppress jgit's log
            Configurator.setLevel("org.eclipse.jgit", Level.INFO);
        }

        log.debug("Set log level to {}", config.logLevel);
        return 0;
    }

    public static void main(String[] args) {
        final Application app = new Application();
        final CommandLine cmdline = new CommandLine(app);
        cmdline.setExecutionStrategy(new RunAll());
        cmdline.setExpandAtFiles(false);

        final int status = cmdline.execute(args);
        if (status != 0) {
            System.exit(status);
        }
    }
}
