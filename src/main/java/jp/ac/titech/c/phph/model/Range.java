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
}
