package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * A range.
 */
@Value
@AllArgsConstructor(staticName = "of")
public class Range {
    int begin;
    int end;

    public static Range at(final int at) {
        return of(at, at + 1);
    }

    public int getLength() {
        return end - begin;
    }

    public boolean isEmpty() {
        return begin == end;
    }

    public boolean isSingle() {
        return begin + 1 == end;
    }

    @Override
    public String toString() {
        return isEmpty()  ? String.format("(%d)", begin)
             : isSingle() ? String.valueOf(begin)
                          : String.format("%d-%d", begin, end - 1);
    }
}
