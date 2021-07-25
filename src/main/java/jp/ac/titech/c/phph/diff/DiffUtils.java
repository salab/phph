package jp.ac.titech.c.phph.diff;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.List;

public class DiffUtils {
    public static <T> String toString(final EditList edits, final List<T> ax, final List<T> ay) {
        final StringBuilder sb = new StringBuilder();
        for (final Edit e : edits) {
            sb.append("@@@ ").append(e).append("\n");
            sb.append(toString(e, ax, ay));
        }
        return sb.toString();
    }

    public static <T> String toString(final Edit edit, final List<T> ax, final List<T> ay) {
        final StringBuilder sb = new StringBuilder();
        for (int x = edit.getBeginA(); x < edit.getEndA(); x++) {
            sb.append("- ").append(ax.get(x).toString()).append("\n");
        }
        for (int y = edit.getBeginB(); y < edit.getEndB(); y++) {
            sb.append("+ ").append(ay.get(y).toString()).append("\n");
        }
        return sb.toString();
    }

    public static void mergeAdjacentEdits(final EditList edits) {
        int i = 0;
        while (i + 1 < edits.size()) {
            final Edit e1 = edits.get(i);
            final Edit e2 = edits.get(i + 1);
            if (e1.getEndA() == e2.getBeginA() && e1.getEndB() == e2.getBeginB()) {
                edits.remove(i); // e1
                edits.remove(i); // e2
                edits.add(i, new Edit(e1.getBeginA(), e2.getEndA(), e1.getBeginB(), e2.getEndB()));
            } else {
                i++;
            }
        }
    }
}
