package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.sha1.SHA1;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A normalized fragment of source code.
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(of = "hash")
public class Code {
    @Getter
    String text;

    @Getter
    ObjectId hash;

    public Code(final List<Statement> statements) {
        this.text = statements.stream()
                .map(s -> s.getNormalizedText())
                .collect(Collectors.joining("\n"));
        this.hash = toSHA1(text);
    }

    /**
     * Computes SHA1 from a string.
     */
    private static ObjectId toSHA1(final String text) {
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final SHA1 sha1 = SHA1.newInstance();
        sha1.update(bytes);
        return sha1.toObjectId();
    }
}
