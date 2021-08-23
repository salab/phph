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
    ChangeType type;

    @Getter
    Hash hash;

    public static Pattern of(final Hash oldHash, final Hash newHash, final ChangeType type, final Hash hash) {
        return new Pattern(oldHash, newHash, type, hash);
    }

    public static Pattern of(final Hash oldHash, final Hash newHash, final ChangeType type) {
        return new Pattern(oldHash, newHash, type, digest(oldHash, newHash));
    }

    public static Pattern of(final Fragment oldFragment, final Fragment newFragment) {
        return of(oldFragment.getHash(), newFragment.getHash(), computeType(oldFragment, newFragment));
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
    public static Hash digest(final Hash oldHash, final Hash newHash) {
        return Hash.of(md -> {
            md.update(oldHash.getRaw());
            md.update(newHash.getRaw());
        });
    }

    public static ChangeType computeType(final Fragment oldFragment, final Fragment newFragment) {
        if (oldFragment.isEmpty()) {
            return newFragment.isEmpty() ? ChangeType.EMPTY : ChangeType.ADD;
        } else {
            return newFragment.isEmpty() ? ChangeType.DELETE : ChangeType.MODIFY;
        }
    }
}
