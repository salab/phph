package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.eclipse.jgit.diff.Edit;

import java.util.List;

/**
 * A deleted and added sequence of statements.
 */
@EqualsAndHashCode
@AllArgsConstructor
@ToString(of = {"file", "oldBegin", "oldEnd", "newBegin", "newEnd"})
public class Chunk {
    public enum Type {
        REPLACE,
        ADD,
        DELETE,
        EMPTY
    }

    @Getter
    private final String file;

    @Getter
    private final int oldBegin, oldEnd;

    @Getter
    private final List<Statement> oldStatements;

    @Getter
    private final int newBegin, newEnd;

    @Getter
    private final List<Statement> newStatements;

    public static Chunk of(final String file, final List<Statement> oldStatements, final List<Statement> newStatements, final Edit e) {
        final List<Statement> oldSlice = oldStatements.subList(e.getBeginA(), e.getEndA());
        final List<Statement> newSlice = newStatements.subList(e.getBeginB(), e.getEndB());

        int oldBegin, oldEnd, newBegin, newEnd;
        if (oldSlice.isEmpty()) {
            // ADD
            oldBegin = e.getBeginA() == oldStatements.size() ? oldStatements.get(oldStatements.size() - 1).getEndLine() : newStatements.get(e.getBeginA()).getBeginLine();
            oldEnd = oldBegin;
        } else {
            oldBegin = oldSlice.get(0).getBeginLine();
            oldEnd = oldSlice.get(oldSlice.size() - 1).getEndLine();
        }
        if (newSlice.isEmpty()) {
            // DEL
            newBegin = e.getBeginB() == newStatements.size() ? newStatements.get(newStatements.size() - 1).getEndLine() : newStatements.get(e.getBeginB()).getBeginLine();
            newEnd = newBegin;
        } else {
            newBegin = newSlice.get(0).getBeginLine();
            newEnd = newSlice.get(newSlice.size() - 1).getEndLine();
        }
        return new Chunk(file, oldBegin, oldEnd, oldSlice, newBegin, newEnd, newSlice);
    }

    public Type getType() {
        if (oldStatements.isEmpty()) {
            return newStatements.isEmpty() ? Type.EMPTY : Type.ADD;
        } else {
            return newStatements.isEmpty() ? Type.DELETE : Type.REPLACE;
        }
    }

    public Pattern toPattern() {
        return Pattern.of(oldStatements, newStatements);
    }
}
