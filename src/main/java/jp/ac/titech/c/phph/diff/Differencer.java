package jp.ac.titech.c.phph.diff;

import org.eclipse.jgit.diff.EditList;

import java.util.List;

public interface Differencer<T> {
    EditList compute(final List<T> left, final List<T> right);
}
