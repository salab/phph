package jp.ac.titech.c.phph.parse;

import jp.ac.titech.c.phph.model.Range;
import jp.ac.titech.c.phph.model.Statement;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * The splitter based for a plain text.
 */
@Slf4j
public class PlainSplitter implements Splitter {

    private final boolean trimSpaces;

    private final boolean skipBlankLines;

    public PlainSplitter(final boolean trimSpaces, final boolean skipBlankLines) {
        this.trimSpaces = trimSpaces;
        this.skipBlankLines = skipBlankLines;
    }

    @Override
    public List<Statement> split(String source) {
        final List<Statement> result = new ArrayList<>();
        int index = 0;
        for (String line : source.split("\\r\\n|\\r|\\n")) {
            index++;
            if (this.trimSpaces) {
                line = line.trim();
            }
            if (!this.skipBlankLines || !line.isEmpty()) {
                result.add(Statement.of(line, Range.of(index, index + 1)));
            }
        }
        return result;
    }
}
