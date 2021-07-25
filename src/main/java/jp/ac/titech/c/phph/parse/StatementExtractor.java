package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Statement;

import java.util.List;

public interface StatementExtractor {
    List<Statement> extractStatements(final String source);
}
