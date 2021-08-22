package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Statement;
import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;

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
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private Statement convert(final yoshikihigo.cpanalyzer.data.Statement s) {
        return Statement.of(s.rText, s.nText, s.fromLine, s.toLine + 1);
    }
}
