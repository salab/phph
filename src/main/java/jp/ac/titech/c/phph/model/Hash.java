package jp.ac.titech.c.phph.model;

import com.google.common.io.BaseEncoding;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

/**
 * A hash code.
 */
@Value
@EqualsAndHashCode(of = "raw")
@Log4j2
public class Hash {
    public static final Hash ZERO = Hash.of(new byte[16]);

    protected static final BaseEncoding BASE16L = BaseEncoding.base16().lowerCase();

    @Getter
    byte[] raw;

    public static Hash of(final byte[] raw) {
        return new Hash(raw);
    }

    public static Hash of(final String text) {
        return new Hash(digest(text));
    }

    public static Hash of(final Consumer<MessageDigest> fn) {
        return new Hash(digest(fn));
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return BASE16L.encode(raw);
    }

    public String abbreviate(final int length) {
        return BASE16L.encode(raw, 0, (length + 1)/2).substring(0, length);
    }

    /**
     * Computes a hashcode from a string.
     */
    public static byte[] digest(final String text) {
        return digest(md -> md.update(text.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Computes a hashcode.
     */
    public static byte[] digest(final Consumer<MessageDigest> fn) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            fn.accept(md);
            return md.digest();
        } catch (final NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
