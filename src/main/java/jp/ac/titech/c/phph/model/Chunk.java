package jp.ac.titech.c.phph.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.eclipse.jgit.diff.Edit;

import java.util.List;

/**
 * A deleted and added sequence of statements.
 */
@RequiredArgsConstructor
@ToString(of = {"file", "oldBegin", "oldEnd", "newBegin", "newEnd"})
public class Chunk {
    @Getter
    private final String file;

    @Getter
    private final int oldBegin, oldEnd;

    @Getter
    private final int oldPreSize, oldPostSize;

    @Getter
    private final List<Statement> oldStatements;

    @Getter
    private final int newBegin, newEnd;

    @Getter
    private final int newPreSize, newPostSize;

    @Getter
    private final List<Statement> newStatements;

    @Getter(lazy = true)
    private final Fragment oldFragment = Fragment.of(oldStatements, oldPreSize, oldPostSize);

    @Getter(lazy = true)
    private final Fragment newFragment = Fragment.of(newStatements, newPreSize, newPostSize);

    @Getter(lazy = true)
    private final Pattern pattern = Pattern.of(getOldFragment(), getNewFragment());

    public static Chunk of(final String file, final List<Statement> oldStatements, final List<Statement> newStatements, final Edit e, final int contextSize) {
        final List<Statement> oldSlice = oldStatements.subList(e.getBeginA(), e.getEndA());
        final List<Statement> newSlice = newStatements.subList(e.getBeginB(), e.getEndB());
        final int oldPreSize  = Math.min(contextSize, e.getBeginA());
        final int newPreSize  = Math.min(contextSize, e.getBeginB());
        final int oldPostSize = Math.min(contextSize, oldStatements.size() - e.getEndA());
        final int newPostSize = Math.min(contextSize, newStatements.size() - e.getEndB());
        final List<Statement> oldSliceWithContext = oldStatements.subList(e.getBeginA() - oldPreSize, e.getEndA() + oldPostSize);
        final List<Statement> newSliceWithContext = newStatements.subList(e.getBeginB() - newPreSize, e.getEndB() + newPostSize);

        int oldBegin, oldEnd, newBegin, newEnd;
        if (oldSlice.isEmpty()) {
            // ADD
            oldBegin = e.getBeginA() == oldStatements.size() ? oldStatements.get(oldStatements.size() - 1).getEndLine() : oldStatements.get(e.getBeginA()).getBeginLine();
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
        return new Chunk(file, oldBegin, oldEnd, oldPreSize, oldPostSize, oldSliceWithContext,
                               newBegin, newEnd, newPreSize, newPostSize, newSliceWithContext);
    }

    public ChangeType getType() {
        if (oldStatements.isEmpty()) {
            return newStatements.isEmpty() ? ChangeType.EMPTY : ChangeType.ADD;
        } else {
            return newStatements.isEmpty() ? ChangeType.DELETE : ChangeType.MODIFY;
        }
    }
}
