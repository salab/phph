package jp.ac.titech.c.phph.model;

import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A normalized fragment of source code.
 */
@Value
public class Query {
    @Getter
    Fragment fragment;

    @Getter
    List<Hash> hashes;

    public Query(final Fragment fragment) {
        this.fragment = fragment;
        this.hashes = computeHashes(fragment.getText());
    }

    private List<Hash> computeHashes(final String text) {
        return text.isEmpty() ? Collections.emptyList()
                              : Stream.of(text.split("\n")).map(Hash::of).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return hashes.stream().map(h -> h.abbreviate(6)).collect(Collectors.joining(","));
    }

    public boolean isEmpty() {
        return hashes.isEmpty();
    }

    public int size() {
        return hashes.size();
    }

    public Hash get(final int index) {
        return hashes.get(index);
    }
}
