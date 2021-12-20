package jp.ac.titech.c.phph.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A normalized fragment of source code.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "hash")
public class Fragment {
    public static final Fragment EMPTY = new Fragment("", Hash.ZERO);

    @Getter
    String text;

    @Getter
    Hash hash;

    protected Fragment(final String text) {
        this(text, Hash.of(text));
    }

    public static Fragment of(final String text, final Hash hash) {
        return text.isEmpty() ? EMPTY : new Fragment(text, hash);
    }

    public static Fragment of(final String text) {
        return text.isEmpty() ? EMPTY : new Fragment(text);
    }

    public static Fragment of(final List<Statement> statements) {
        return statements.isEmpty() ? EMPTY : new Fragment(join(statements));
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

    public String getShortText() {
        final String text = getText();
        final List<String> lines = text.lines().collect(Collectors.toList());
        return lines.isEmpty() ? "(empty)" : lines.size() > 1 ? lines.get(0) + " ..." : text;
    }
}
