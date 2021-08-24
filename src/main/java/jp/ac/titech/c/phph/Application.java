package jp.ac.titech.c.phph;

import jp.ac.titech.c.phph.cmd.AppCommand;
import picocli.CommandLine;
import picocli.CommandLine.RunAll;

public class Application {
    public static void main(String[] args) {
        final AppCommand app = new AppCommand();
        final CommandLine cmdline = new CommandLine(app);
        cmdline.setExecutionStrategy(new RunAll());
        cmdline.setExpandAtFiles(false);

        final int status = cmdline.execute(args);
        if (status != 0) {
            System.exit(status);
        }
    }
}
