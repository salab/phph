package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Pattern;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.*;
import picocli.CommandLine.Command;

@Log4j2
@Command(name = "show", description = "Inspect objects")
public class ShowCommand extends BaseCommand {
    public static class Config {
        @Option(names = {"-f", "--fragment"}, paramLabel = "<hash>", description = "inspect a fragment")
        String fragment;

        @Option(names = {"-p", "--pattern"}, paramLabel = "<hash>", description = "inspect a pattern")
        String pattern;
    }

    @Mixin
    Config config = new Config();

    @Override
    protected void process() {
        if (config.fragment != null) {
            showFragment(config.fragment);
        }
    }

    protected void showFragment(final String prefix) {
        final String pattern = config.fragment + "%";
        final int count = dao.countFragments(pattern);
        if (count == 1) {
            final Fragment f = dao.searchFragments(pattern).findFirst().get();
            System.out.printf("Hash: %s\n", f.getHash());
            System.out.printf("Text:\n------\n%s\n------\n", f.getText());
            System.out.println("Pattern:");
            for (final Pattern p : dao.listPatterns(f)) {
                System.out.println(p);
            }
        } else {
            for (final Fragment f : dao.searchFragments(pattern)) {
                System.out.println(f.getHash());
            }
        }
    }

    protected void showPattern(final String prefix) {
        final String pattern = config.fragment + "%";
        final int count = dao.countFragments(pattern);
        if (count == 1) {
            final Fragment f = dao.searchFragments(pattern).findFirst().get();
            System.out.printf("Hash: %s\nBody:\n%s\n", f.getHash(), f.getText());
        } else {
            for (final Fragment f : dao.searchFragments(pattern)) {
                System.out.println(f.getHash());
            }
        }
    }
}
