package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Statement;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The statement extractor used by MPAnalyzer.
 */
public class MPAStatementExtractor implements StatementExtractor {
    @Override
    public List<Statement> extractStatements(String source) {
        return StringUtility.splitToStatements(source, LANGUAGE.JAVA)
                .stream()
                .map(s -> convert(s))
                .collect(Collectors.toList());
    }

    private Statement convert(final yoshikihigo.cpanalyzer.data.Statement s) {
        return Statement.of(s.rText, s.nText, s.fromLine, s.toLine + 1);
    }
}
