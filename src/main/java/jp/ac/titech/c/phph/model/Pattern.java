package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

/**
 * A normalized fragment of source code.
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(of = {"hash"})
public class Pattern {
    @Getter
    Hash oldHash;

    @Getter
    Hash newHash;

    @Getter
    Hash hash;

    public static Pattern of(final Hash oldHash, final Hash newHash, final Hash hash) {
        return new Pattern(oldHash, newHash, hash);
    }

    public static Pattern of(final Hash oldHash, final Hash newHash) {
        return new Pattern(oldHash, newHash, digest(oldHash, newHash));
    }

    public static Pattern of(final Fragment oldFragment, final Fragment newFragment) {
        return of(oldFragment.getHash(), newFragment.getHash());
    }

    @Override
    public String toString() {
        return hash + ":" + getType().name() + "|" + oldHash + "->" + newHash;
    }

    public String toShortString() {
        return hash.abbreviate(6) + ":" + getType().name().charAt(0) + "|" + oldHash.abbreviate(6) + "->" + newHash.abbreviate(6);
    }

    /**
     * Computes SHA1 from a string.
     */
    public static Hash digest(final Hash... hashes) {
        return Hash.of(md -> {
            for (final Hash h : hashes) {
                if (h != null) {
                    md.update(h.getRaw());
                } else {
                    md.update((byte) 0);
                }
            }
        });
    }

    public ChangeType getType() {
        if (oldHash.isZero()) {
            return newHash.isZero() ? ChangeType.EMPTY : ChangeType.ADD;
        } else {
            return newHash.isZero() ? ChangeType.DELETE : ChangeType.MODIFY;
        }
    }
}
