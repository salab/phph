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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "hash")
public class Fragment {
    @Getter
    String text;

    @Getter
    Hash hash;

    public static Fragment of(final String text) {
        return new Fragment(text, text.isEmpty() ? Hash.ZERO : Hash.of(text));
    }

    public static Fragment of(final List<Statement> statements) {
        final String text = statements.stream()
                .map(Statement::getNormalized)
                .collect(Collectors.joining("\n"));
        return of(text);
    }

    @Override
    public String toString() {
        return text.isEmpty() ? "``" : "`" + text.replace("\n", " ") + "`";
    }
}
