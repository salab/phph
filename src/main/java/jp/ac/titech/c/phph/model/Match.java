package jp.ac.titech.c.phph.model;

import lombok.Getter;
import lombok.Value;

/**
 * A normalized fragment of source code.
 */
@Value
public class Match {
    @Getter
    Hash query;

    @Getter
    String file;

    @Getter
    int beginLine;

    @Getter
    int endLine;
}
