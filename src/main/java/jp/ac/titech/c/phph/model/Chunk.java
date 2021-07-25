package jp.ac.titech.c.phph.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * A deleted and added sequence of statements.
 * @param <T> Type of statements.
 */
@EqualsAndHashCode
public class Chunk<T> {
    public enum Type {
        REPLACE,
        ADD,
        DELETE,
        EMPTY;
    }

    @Getter
    private final List<T> before;

    @Getter
    private final List<T> after;

    @Getter
    private final Type type;

    public Chunk(final List<T> before, final List<T> after) {
        this.before = before;
        this.after = after;
        this.type = computeType();
    }

    private Type computeType() {
        if (before.isEmpty()) {
            return after.isEmpty() ? Type.EMPTY : Type.ADD;
        } else {
            return after.isEmpty() ? Type.DELETE : Type.REPLACE;
        }
    }
}
