package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.sha1.SHA1;

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
    ObjectId hash;

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

    /**
     * Computes SHA1 from a string.
     */
    public static ObjectId digest(final Fragment... fragments) {
        final SHA1 sha1 = SHA1.newInstance();
        final byte[] buf = new byte[20];
        for (final Fragment f : fragments) {
            if (f != null) {
                f.getHash().copyRawTo(buf, 0);
                sha1.update(buf);
            } else {
                sha1.update((byte) 0);
            }
        }
        return sha1.toObjectId();
    }
}
