package jp.ac.titech.c.phph.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = {"normalized"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Statement {
    @Getter
    private final String raw;

    @Getter
    private final String normalized;

    /**
     * The line number at witch this statement begins and ends. 1-origin.
     * The ending line should be excluded from the actual region; the region is "[begin, end)".
     * For example, if a statement is within a single line, <code>end == begin + 1</code> holds.
     */
    @Getter
    private final Range lines;

    public static Statement of(final String raw, final String normalized, final Range lines) {
        return new Statement(raw, normalized, lines);
    }

    public static Statement of(final String normalized, final Range lines) {
        return new Statement(null, normalized, lines);
    }

    @Override
    public String toString() {
        return normalized;
    }
}
