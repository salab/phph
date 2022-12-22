package jp.ac.titech.c.phph.parse;

import com.github.durun.nitron.core.ParsingException;
import com.github.durun.nitron.core.ast.node.AstNode;
import com.github.durun.nitron.core.ast.processors.AstProcessor;
import com.github.durun.nitron.core.ast.processors.AstSplitter;
import com.github.durun.nitron.core.ast.type.NodeTypePool;
import com.github.durun.nitron.core.ast.visitor.AstLineGetVisitor;
import com.github.durun.nitron.core.ast.visitor.LineRange;
import com.github.durun.nitron.core.config.LangConfig;
import com.github.durun.nitron.core.parser.NitronParser;
import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.model.Statement;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The splitter using nitron.
 */
@Slf4j
public class NitronSplitter implements Splitter {
    private final NitronParser parser;
    private final AstSplitter splitter;
    private final AstProcessor<AstNode> normalizer;
    private final String extension;

    public NitronSplitter(final LangConfig config) {
        parser = config.getParserConfig().getParser();
        final NodeTypePool types = parser.getNodeTypes();
        splitter = config.getProcessConfig().getSplitConfig().initSplitter(types);
        normalizer = config.getProcessConfig().getNormalizeConfig().initNormalizer(types);
        extension = config.getExtensions().get(0)
                .substring(1); // ".ext" -> "ext"
    }

    @Override
    public String targetExtension() {
        return extension;
    }

    @Override
    public List<Statement> split(final String source) {
        try {
            final AstNode root = parser.parse(new StringReader(source));
            return splitter.process(root)
                    .stream()
                    .map(this::normalizeAndConvert)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (final ParsingException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private Statement normalizeAndConvert(final AstNode node) {
        final String rText = node.getText();
        final LineRange lines = node.accept(AstLineGetVisitor.INSTANCE);
        final AstNode nNode = normalizer.process(node);
        if (nNode == null) return null;
        final String nText = nNode.getText();
        return Statement.of(rText, nText, Range.of(lines.getFirst(), lines.getLast() + 1));
    }
}
