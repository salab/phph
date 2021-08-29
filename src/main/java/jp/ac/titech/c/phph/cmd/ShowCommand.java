package jp.ac.titech.c.phph.cmd;

import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.util.RepositoryAccess;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Command(name = "show", description = "Inspect objects")
public class ShowCommand extends BaseCommand {
    public static class Config {
        @Option(names = {"-f", "--fragment"}, paramLabel = "<hash>", description = "inspect a fragment")
        String fragment;

        @Option(names = {"-t", "--text"}, paramLabel = "<text>", description = "lookup a fragment")
        String text;

        @Option(names = {"-p", "--pattern"}, paramLabel = "<hash>", description = "inspect a pattern")
        String pattern;
    }

    @Mixin
    Config config = new Config();

    @Override
    protected void process() {
        if (config.fragment != null) {
            showFragment(config.fragment);
        } else if (config.pattern != null) {
            showPattern(config.pattern);
        } else if (config.text != null) {
            lookupFragment(config.text);
        }
    }

    protected void showFragment(final String prefix) {
        final String like = prefix + "%";
        final int count = dao.countFragments(like);
        log.debug("Search fragments for '{}', {} found", like, count);
        if (count == 1) {
            final Fragment f = dao.searchFragments(like).findFirst().get();
            System.out.printf("Hash: %s\n", f.getHash());
            System.out.printf("Text:\n------\n%s\n------\n", f.getText());
            System.out.println("Pattern:");
            for (final Pattern p : dao.listPatterns(f)) {
                System.out.println(p);
            }
        } else {
            for (final Fragment f : dao.searchFragments(like)) {
                System.out.println(f.getHash());
            }
        }
    }

    protected void lookupFragment(final String text) {
        System.out.println(text);
        final Optional<Fragment> fragment = dao.lookupFragment(text);
        fragment.ifPresent(f -> System.out.println(f.getHash()));
    }

    protected void showPattern(final String prefix) {
        final String like = prefix + "%";
        final int count = dao.countPatterns(like);
        log.debug("Search patterns for '{}', {} found", like, count);
        if (count == 1) {
            final Pattern p = dao.searchPatterns(like).findFirst().get();
            System.out.printf("Hash: %s\n", p.getHash());
            System.out.printf("------\n%s\n------\n%s\n------\n", dao.findFragment(p.getOldHash()).getText(), dao.findFragment(p.getNewHash()).getText());
            System.out.println("Chunk:");
            int cid = -1;
            Dao.Commit commit = null;
            final String repository = dao.findRepository();
            for (final Dao.DBChunk h : dao.listChunks(p.getHash())) {
                if (cid != h.getCommitId()) {
                    cid = h.getCommitId();
                    commit = dao.findCommit(cid);
                    System.out.printf("Commit %s - %s\n", commit.getHash(), commit.getMessage());
                }
                System.out.printf("--- %s:%s\n", h.getFile(), h.getOldLines());
                System.out.printf("+++ %s:%s\n", h.getFile(), h.getNewLines());
                System.out.println(inspectChunk(repository, commit.getHash(), h));
            }
        } else {
            for (final Fragment f : dao.searchFragments(like)) {
                System.out.println(f.getHash());
            }
        }
    }

    protected String inspectChunk(final String repository, final String commit, final Dao.DBChunk chunk) {
        final StringBuilder sb = new StringBuilder();
        try (final RepositoryAccess ra = new RepositoryAccess(Path.of(repository))) {
            final String oldSource = ra.readFile(commit + "^", chunk.getFile());
            extract(oldSource, chunk.getOldLines(), "-", sb);
            sb.append("---------\n");
            final String newSource = ra.readFile(commit, chunk.getFile());
            extract(newSource, chunk.getNewLines(), "+", sb);
        }
        return sb.toString();
    }

    protected void extract(final String source, final Range r, final String mark, final StringBuilder sb) {
        final List<String> lines = source.lines().collect(Collectors.toList());
        final int size = (int) Math.log10(lines.size());
        final int delta = 2;

        for (int i = Math.max(r.getBegin() - delta, 1); i < r.getBegin(); i++) {
            sb.append(String.format("  %6d: %s\n", i, lines.get(i - 1)));
        }
        for (int i = r.getBegin(); i < r.getEnd(); i++) {
            sb.append(String.format("%s %6d: %s\n", mark, i, lines.get(i - 1)));
        }
        for (int i = r.getEnd(); i <= Math.min(r.getEnd() + delta - 1, lines.size()); i++) {
            sb.append(String.format("  %6d: %s\n", i, lines.get(i - 1)));
        }
    }
}
