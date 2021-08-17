package jp.ac.titech.c.phph.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.sha1.SHA1;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A normalized fragment of source code.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "hash")
public class Fragment {
    @Getter
    String text;

    @Getter
    ObjectId hash;

    public static Fragment of(final String text) {
        return new Fragment(text, digest(text));
    }

    public static Fragment of(final List<Statement> statements) {
        final String text = statements.stream()
                .map(s -> s.getNormalized())
                .collect(Collectors.joining("\n"));
        return of(text);
    }

    @Override
    public String toString() {
        return text.isEmpty() ? "``" : "`" + text.replace("\n", " ") + "`";
    }

    /**
     * Computes SHA1 from a string.
     */
    public static ObjectId digest(final String text) {
        if (text.isEmpty()) {
            return ObjectId.zeroId();
        } else {
            final SHA1 sha1 = SHA1.newInstance();
            sha1.update(text.getBytes(StandardCharsets.UTF_8));
            return sha1.toObjectId();
        }
    }
}
