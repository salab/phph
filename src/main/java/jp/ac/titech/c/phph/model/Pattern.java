package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.List;

/**
 * A normalized fragment of source code.
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(of = {"hash"})
public class Pattern {
    @Getter
    Fragment oldFragment;

    @Getter
    Fragment newFragment;

    @Getter
    Fragment preContext; // NYI

    @Getter
    Fragment postContext; // NYI

    @Getter
    Hash hash;

    public static Pattern of(final Fragment oldFragment, final Fragment newFragment, final Fragment preContext, final Fragment postContext) {
        return new Pattern(oldFragment, newFragment, preContext, postContext, digest(oldFragment, newFragment, preContext, postContext));
    }

    public static Pattern of(final Fragment oldFragment, final Fragment newFragment) {
        return of(oldFragment, newFragment, null, null);
    }

    public static Pattern of(final List<Statement> oldStatements, final List<Statement> newStatements) {
        return of(Fragment.of(oldStatements), Fragment.of(newStatements));
    }

    @Override
    public String toString() {
        return oldFragment.toString() + " --> " + newFragment.toString();
    }

    public String getSummary() {
        return hash.abbreviate(6)
                + ":" + oldFragment.getHash().abbreviate(6)
                + "->" + newFragment.getHash().abbreviate(6);
    }

    /**
     * Computes SHA1 from a string.
     */
    public static Hash digest(final Fragment... fragments) {
        return Hash.of(md -> {
            for (final Fragment f : fragments) {
                if (f != null) {
                    md.update(f.getHash().getRaw());
                } else {
                    md.update((byte) 0);
                }
            }
        });
    }
}
