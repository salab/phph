package jp.ac.titech.c.phph.diff;

import lombok.Getter;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;

import java.util.List;

public interface Differencer<T> {
    EditList compute(final List<T> left, final List<T> right);
}
