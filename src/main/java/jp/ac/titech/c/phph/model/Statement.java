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
     * The line number at witch this statement begins.
     */
    @Getter
    private final int beginLine;

    /**
     * The line number at which this statement ends.
     * The ending line should be excluded from the actual region; the region is "[begin, end)".
     * For example, if a statement is within a single line, <code>endLine == beginLine + 1</code> holds.
     */
    @Getter
    private final int endLine;

    public static Statement of(final String raw, final String normalized, final int beginLine, final int endLine) {
        return new Statement(raw, normalized, beginLine, endLine);
    }

    public static Statement of(final String normalized, final int beginLine, final int endLine) {
        return new Statement(null, normalized, beginLine, endLine);
    }

    @Override
    public String toString() {
        return normalized;
    }

    public int getLength() {
        return endLine - beginLine;
    }
}
