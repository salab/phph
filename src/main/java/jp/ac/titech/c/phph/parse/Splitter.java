package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Statement;

import java.util.List;

public interface Splitter {
    List<Statement> split(final String source);
}
