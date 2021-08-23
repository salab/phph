package jp.ac.titech.c.phph.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A normalized fragment of source code.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "hash")
public class Fragment {
    public static final Fragment EMPTY = new Fragment("", 0, 0, Hash.ZERO);

    @Getter
    String text;

    @Getter
    int preSize;

    @Getter
    int postSize;

    @Getter
    Hash hash;

    protected Fragment(final String text, final int preSize, final int postSize) {
        this(text, preSize, postSize, digest(text, preSize, postSize));
    }

    public static Fragment of(final String text, final int preSize, final int postSize, final Hash hash) {
        return text.isEmpty() ? EMPTY : new Fragment(text, preSize, postSize, hash);
    }

    public static Fragment of(final String text, final int preSize, final int postSize) {
        return text.isEmpty() ? EMPTY : new Fragment(text, preSize, postSize);
    }

    public static Fragment of(final List<Statement> statements, final int preSize, final int postSize) {
        return statements.isEmpty() ? EMPTY : new Fragment(join(statements), preSize, postSize);
    }

    public Query toQuery() {
        return new Query(this);
    }

    @Override
    public String toString() {
        return text.isEmpty() ? "``" : "`" + text.replace("\n", " ") + "`";
    }

    public static String join(final List<Statement> statements) {
        return statements.stream()
                .map(Statement::getNormalized)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Computes a hash for a fragment.
     */
    public static Hash digest(final String text, final int preSize, final int postSize) {
        return Hash.of(md -> {
            md.update(text.getBytes(StandardCharsets.UTF_8));
            if (preSize != 0 || postSize != 0) {
                // Only when contexts are available;
                // then, the resulting hash becomes the same as that of the text in case of no contest.
                md.update((byte) preSize);
                md.update((byte) postSize);
            }
        });
    }
}
