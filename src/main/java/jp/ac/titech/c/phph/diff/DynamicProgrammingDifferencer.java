package jp.ac.titech.c.phph.diff;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Computes the difference of two lists using dynamic programming.
 *
 * @param <T> The type of the elements of the lists.
 */
public class DynamicProgrammingDifferencer<T> implements Differencer<T> {
    @Override
    public EditList compute(final List<T> ax, final List<T> ay) {
        final EditList result = new EditList();
        final Cell[][] mat = createEditGraph(ax, ay);
        final SortedSet<Integer> dx = new TreeSet<>();
        final SortedSet<Integer> dy = new TreeSet<>();
        Cell c = mat[ax.size()][ay.size()];
        while (c != null) {
            if (!c.match) {
                final Cell base = c.base;  // this must exist
                if (base.x < c.x) {
                    dx.add(c.x);
                }
                if (base.y < c.y) {
                    dy.add(c.y);
                }
            } else if (!dx.isEmpty() || !dy.isEmpty()) {
                int bx = dx.isEmpty() ? c.x : dx.first() - 1;
                int ex = dx.isEmpty() ? c.x : dx.last();
                int by = dy.isEmpty() ? c.y : dy.first() - 1;
                int ey = dy.isEmpty() ? c.y : dy.last();
                result.add(new Edit(bx, ex, by, ey));
                dx.clear();
                dy.clear();
            }
            c = c.base;
        }

        Collections.reverse(result);
        DiffUtils.mergeAdjacentEdits(result); // TODO: really necessary?
        return result;
    }

    /**
     * Creates the edit graph of the given lists.
     */
    private Cell[][] createEditGraph(final List<T> ax, final List<T> ay) {
        final Cell[][] result = new Cell[ax.size() + 1][ay.size() + 1];

        // (0, 0), (*, 0), (0, *)
        result[0][0] = new Cell(0, 0, 0, true, null);
        for (int x = 1; x <= ax.size(); x++) {
            result[x][0] = new Cell(x, 0, 0, false, result[x - 1][0]);
        }
        for (int y = 1; y <= ay.size(); y++) {
            result[0][y] = new Cell(0, y, 0, false, result[0][y - 1]);
        }

        // (*, *)
        for (int x = 1; x <= ax.size(); x++) {
            for (int y = 1; y <= ay.size(); y++) {
                final boolean match = Objects.equals(ax.get(x - 1), ay.get(y - 1));
                final Cell left = result[x - 1][y];
                final Cell up = result[x][y - 1];
                final Cell upleft = result[x - 1][y - 1];
                final Cell base = match ? upleft : left.value >= up.value ? left : up;
                result[x][y] = new Cell(x, y, base.value + (match ? 1 : 0), match, base);
            }
        }

        return result;
    }

    @Value
    @AllArgsConstructor
    static class Cell {
        public int x;
        public int y;

        /**
         * The number of already matched lines.
         */
        public int value;

        public boolean match;

        /**
         * The previous cell.
         */
        public Cell base;
    }
}
