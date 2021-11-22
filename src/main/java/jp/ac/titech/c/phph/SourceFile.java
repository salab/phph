package jp.ac.titech.c.phph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jp.ac.titech.c.phph.model.Hash;
import jp.ac.titech.c.phph.model.Match;
import jp.ac.titech.c.phph.model.Query;
import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.model.Statement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SourceFile implements Comparable<SourceFile> {
    @Getter
    private final Path path;

    @Getter
    private final List<Statement> statements;

    @Getter
    private final List<Hash> hashes;

    private final Multimap<Hash, Integer> resolver;

    public SourceFile(final Path path, final List<Statement> statements) {
        this.path = path;
        this.statements = statements;
        this.hashes = statements.stream().map(s -> Hash.of(s.getNormalized())).collect(Collectors.toList());
        this.resolver = computeReverseResolver(hashes);
    }

    /**
     * Computers a reverse resolver from a content of a list to its indices.
     */
    protected <T> Multimap<T, Integer> computeReverseResolver(final List<T> elements) {
        final Multimap<T, Integer> result = ArrayListMultimap.create();
        for (int i = 0; i < elements.size(); i++) {
            result.put(elements.get(i), i);
        }
        return result;
    }

    public List<Match> find(final Query query) {
        if (query.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Match> result = new ArrayList<>();
        for (final int index : resolver.get(query.get(0))) {
            if (match(query, index)) {
                // matched
                final int beginLine = statements.get(index).getLines().getBegin();
                final int endLine = statements.get(index + query.size() - 1).getLines().getEnd();
                result.add(new Match(query.getFragment().getHash(), path.toString(), Range.of(beginLine, endLine)));
            }
        }
        return result;
    }

    private boolean match(final Query query, final int index) {
        if (index + query.size() >= hashes.size()) {
            return false;
        }
        // This method assumes that the case i == 0 holds
        assert hashes.get(index).equals(query.get(0));

        for (int i = 1; i < query.size(); i++) {
            if (!hashes.get(index + i).equals(query.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(final SourceFile other) {
        return path.compareTo(other.path);
    }
}
