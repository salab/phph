package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.model.Statement;
import lombok.extern.slf4j.Slf4j;
import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.LANGUAGE;
import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.lexer.token.IMPORT;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The splitter used by MPAnalyzer.
 */
@Slf4j
public class MPASplitter implements Splitter {

    private final boolean useNonEssential;

    public static final Pattern RE_IDENT = Pattern.compile("^[a-zA-Z_].*|.* [a-zA-Z_].*");

    public static final Pattern RE_NON_ESSENTIAL_ASSIGNMENT =
            Pattern.compile("^(\\w+( < \\w+ >)? |this \\. )?\\$V0 = (\"\\$L\"|\\$L|\\$V0|\\$V1|null) ;$");

    static {
        CPAConfig.initialize(new String[]{"-n"}); // normalize
    }

    @Override
    public String targetExtension() {
        return "java";
    }

    public MPASplitter(final boolean useNonEssential) {
        this.useNonEssential = useNonEssential;
    }

    @Override
    public List<Statement> split(String source) {
        return StringUtility.splitToStatements(source, LANGUAGE.JAVA)
                .stream()
                .filter(s -> !(s.tokens.get(0) instanceof IMPORT)) // drop import statements
                .filter(this::filterNonEssential) // drop non-essential statements
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private boolean filterNonEssential(final yoshikihigo.cpanalyzer.data.Statement s) {
        if (useNonEssential || isEssential(s.nText)) {
            return true;
        } else {
            log.trace("Filtering non-essential statement: {}", s.nText);
            return false;
        }
    }

    public static boolean isEssential(final String nText) {
        return RE_IDENT.matcher(nText).matches() &&
                !RE_NON_ESSENTIAL_ASSIGNMENT.matcher(nText).matches();
    }

    private Statement convert(final yoshikihigo.cpanalyzer.data.Statement s) {
        return Statement.of(s.rText, s.nText, Range.of(s.fromLine, s.toLine + 1));
    }
}
