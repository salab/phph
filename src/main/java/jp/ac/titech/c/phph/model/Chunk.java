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
@ToString(of = {"file", "oldLines", "newLines"})
public class Chunk {
    @Getter
    private final String file;

    @Getter
    private final Range oldLines;

    @Getter
    private final List<Statement> oldStatements;

    @Getter
    private final Range newLines;

    @Getter
    private final List<Statement> newStatements;

    @Getter(lazy = true)
    private final Fragment oldFragment = Fragment.of(oldStatements);

    @Getter(lazy = true)
    private final Fragment newFragment = Fragment.of(newStatements);

    @Getter(lazy = true)
    private final Pattern pattern = Pattern.of(getOldFragment(), getNewFragment());

    public static Chunk of(final String file, final List<Statement> oldStatements, final List<Statement> newStatements, final Edit e) {
        final List<Statement> oldSlice = oldStatements.subList(e.getBeginA(), e.getEndA());
        final List<Statement> newSlice = newStatements.subList(e.getBeginB(), e.getEndB());

        int oldBegin, oldEnd, newBegin, newEnd;
        if (oldSlice.isEmpty()) {
            // ADD
            oldBegin = e.getBeginA() == oldStatements.size() ? oldStatements.get(oldStatements.size() - 1).getLines().getEnd() : oldStatements.get(e.getBeginA()).getLines().getBegin();
            oldEnd = oldBegin;
        } else {
            oldBegin = oldSlice.get(0).getLines().getBegin();
            oldEnd = oldSlice.get(oldSlice.size() - 1).getLines().getEnd();
        }
        if (newSlice.isEmpty()) {
            // DEL
            newBegin = e.getBeginB() == newStatements.size() ? newStatements.get(newStatements.size() - 1).getLines().getEnd() : newStatements.get(e.getBeginB()).getLines().getBegin();
            newEnd = newBegin;
        } else {
            newBegin = newSlice.get(0).getLines().getBegin();
            newEnd = newSlice.get(newSlice.size() - 1).getLines().getEnd();
        }
        return new Chunk(file, Range.of(oldBegin, oldEnd), oldSlice, Range.of(newBegin, newEnd), newSlice);
    }

    public ChangeType getType() {
        if (oldStatements.isEmpty()) {
            return newStatements.isEmpty() ? ChangeType.EMPTY : ChangeType.ADD;
        } else {
            return newStatements.isEmpty() ? ChangeType.DELETE : ChangeType.MODIFY;
        }
    }
}
