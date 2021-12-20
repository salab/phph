package jp.ac.titech.c.phph.browse;

import jp.ac.titech.c.phph.db.Dao;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.util.RepositoryAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Helper {
    public String inspectChunk(final String repository, final String commit, final Dao.DBChunk chunk) {
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

    public String inspectMatch(final String repository, final String commit, final Match m) {
        log.info("commit = {}, m = {}", commit, m);
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s:%s\n", m.getFile(), m.getLines()));
        try (final RepositoryAccess ra = new RepositoryAccess(Path.of(repository))) {
            final String source = ra.readFile(commit, m.getFile());
            extract(source, m.getLines(), "*", sb);
        }
        return sb.toString();
    }

    public void extract(final String source, final Range r, final String mark, final StringBuilder sb) {
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
