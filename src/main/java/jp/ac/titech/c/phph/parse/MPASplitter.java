package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.model.Statement;
import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.lexer.token.IMPORT;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The splitter used by MPAnalyzer.
 */
public class MPASplitter implements Splitter {
    static {
        CPAConfig.initialize(new String[] {"-n"}); // normalize
    }

    @Override
    public List<Statement> split(String source) {
        return StringUtility.splitToStatements(source, LANGUAGE.JAVA)
                .stream()
                .filter(s -> !(s.tokens.get(0) instanceof IMPORT)) // drop import statements
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private Statement convert(final yoshikihigo.cpanalyzer.data.Statement s) {
        return Statement.of(s.rText, s.nText, Range.of(s.fromLine, s.toLine + 1));
    }
}
