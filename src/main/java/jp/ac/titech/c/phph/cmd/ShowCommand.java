package jp.ac.titech.c.phph.cmd;

import com.google.common.collect.Lists;
import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Fragment;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Pattern;
import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.util.RepositoryAccess;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
        if (count > 1) {
            for (final Fragment f : dao.searchFragments(like)) {
                System.out.println(f.getHash());
            }
            return;
        }

        final Fragment f = dao.searchFragments(like).findFirst().get();
        System.out.printf("Hash: %s\n", f.getHash());
        System.out.printf("Text:\n%s\n\n", f.getText());
        final List<Pattern> patterns = Lists.newArrayList(dao.listPatterns(f));
        System.out.printf("Patterns (%d):\n", patterns.size());
        for (final Pattern p : patterns) {
            System.out.println(p);
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
        if (count > 1) {
            for (final Fragment f : dao.searchFragments(like)) {
                System.out.println(f.getHash());
            }
            return;
        }

        final Pattern p = dao.searchPatterns(like).findFirst().get();
        System.out.printf("Hash: %s\n", p.getHash());
        System.out.printf("Pattern:\n%s\n-->\n%s\n\n", dao.findFragment(p.getOldHash()).get().getText(),
                                                       dao.findFragment(p.getNewHash()).get().getText());

        final String repository = dao.findRepository().get();

        final List<Dao.DBChunk> chunks = Lists.newArrayList(dao.listChunks(p.getHash()));
        System.out.printf("Chunks (%d):\n", chunks.size());
        chunks.stream().collect(Collectors.groupingBy(Dao.DBChunk::getCommitId)).forEach((cid, hs) -> {
            final Dao.Commit commit = dao.findCommit(cid).get();
            System.out.printf("Commit %s: %s\n", commit.getHash(), commit.getMessage());
            for (final Dao.DBChunk h : hs) {
                System.out.printf("--- %s:%s @ %s\n", h.getFile(), h.getOldLines(), commit.getHash());
                System.out.printf("+++ %s:%s @ %s\n", h.getFile(), h.getNewLines(), commit.getHash());
                System.out.println(inspectChunk(repository, commit.getHash(), h));
            }
        });

        final List<Match> matches = Lists.newArrayList(dao.listMatches(p.getOldHash()));
        System.out.printf("\nMatches (%d):\n", matches.size());
        for (final Match m : dao.listMatches(p.getOldHash())) {
            System.out.println(inspectMatch(repository, "HEAD", m));
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

    protected String inspectMatch(final String repository, final String commit, final Match m) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s:%s\n", m.getFile(), m.getLines()));
        try (final RepositoryAccess ra = new RepositoryAccess(Path.of(repository))) {
            final String source = ra.readFile(commit, m.getFile());
            extract(source, m.getLines(), "*", sb);
        }
        return sb.toString();
    }

    protected void extract(final String source, final Range r, final String mark, final StringBuilder sb) {
        final List<String> lines = source.lines().toList();
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
