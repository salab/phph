package jp.ac.titech.c.phph.model;

import lombok.Value;

/**
 * A match.
 */
@Value
public class Match {
    Hash query;
    String file;
    Range lines;
}
