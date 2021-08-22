package jp.ac.titech.c.phph.diff;

import lombok.Getter;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;

import java.util.List;

public class JGitDifferencer<T> implements Differencer<T> {

    public static class ListSequence<T> extends Sequence {
        @Getter
        private final List<T> elements;

        public ListSequence(final List<T> elements) {
            this.elements = elements;
        }

        public T get(final int index) {
            return elements.get(index);
        }

        public List<T> slice(final int fromIndex, final int toIndex) {
            return elements.subList(fromIndex, toIndex);
        }

        @Override
        public int size() {
            return elements.size();
        }
    }

    public static class ListSequenceComparator<T> extends SequenceComparator<ListSequence<T>> {
        @Override
        public boolean equals(final ListSequence<T> a, final int ai, final ListSequence<T> b, final int bi) {
            return a.get(ai).equals(b.get(bi));
        }

        @Override
        public int hash(final ListSequence<T> seq, int ptr) {
            return seq.get(ptr).hashCode();
        }
    }

    private final DiffAlgorithm algorithm;

    private final ListSequenceComparator<T> comparator = new ListSequenceComparator<>();

    public JGitDifferencer(final DiffAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public JGitDifferencer(final DiffAlgorithm.SupportedAlgorithm alg) {
        this(DiffAlgorithm.getAlgorithm(alg));
    }

    public static <T> JGitDifferencer<T> newMyers() {
        return new JGitDifferencer<>(DiffAlgorithm.SupportedAlgorithm.MYERS);
    }

    public static <T> JGitDifferencer<T> newHistorgram() {
        return new JGitDifferencer<>(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
    }

    public EditList compute(final List<T> left, final List<T> right) {
        final EditList result = algorithm.diff(comparator, new ListSequence<>(left), new ListSequence<>(right));
        DiffUtils.mergeAdjacentEdits(result);
        return result;
    }
}
