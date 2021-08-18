package jp.ac.titech.c.phph.parse;

import com.github.durun.nitron.core.ast.node.AstNode;
import com.github.durun.nitron.core.ast.processors.AstProcessor;
import com.github.durun.nitron.core.ast.processors.AstSplitter;
import com.github.durun.nitron.core.ast.type.NodeTypePool;
import com.github.durun.nitron.core.ast.visitor.AstLineGetVisitor;
import com.github.durun.nitron.core.ast.visitor.LineRange;
import com.github.durun.nitron.core.config.LangConfig;
import com.github.durun.nitron.core.parser.NitronParser;
import jp.ac.titech.c.phph.model.Statement;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The statement extractor using nitron.
 */
public class NitronStatementExtractor implements StatementExtractor {
    private final NitronParser parser;
    private final AstSplitter splitter;
    private final AstProcessor<AstNode> normalizer;

    public NitronStatementExtractor(LangConfig config) {
        parser = config.getParserConfig().getParser();
        final NodeTypePool types = parser.getNodeTypes();
        splitter = config.getProcessConfig().getSplitConfig().initSplitter(types);
        normalizer = config.getProcessConfig().getNormalizeConfig().initNormalizer(types);
    }

    @Override
    public List<Statement> extractStatements(String source) {
        final AstNode root = parser.parse(new StringReader(source));
        return splitter.process(root)
                .stream()
                .map(n -> normalizeAndConvert(n))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Statement normalizeAndConvert(final AstNode node) {
        final String rText = node.getText();
        final LineRange lines = node.accept(AstLineGetVisitor.INSTANCE);    // 0-origin
        final AstNode nNode = normalizer.process(node);
        if (nNode == null) return null;
        final String nText = nNode.getText();
        return Statement.of(rText, nText, lines.getFirst() + 1, lines.getLast() + 2);
    }
}
