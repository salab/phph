package jp.ac.titech.c.phph.model;

import com.google.common.io.BaseEncoding;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * A hash code.
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Hash implements Comparable<Hash> {
    /**
     * Zero Id.
     */
    public static final Hash ZERO = new Hash(new byte[16]);

    protected static final BaseEncoding BASE16L = BaseEncoding.base16().lowerCase();

    /**
     * Raw bytes of this hash.
     */
    @Getter
    byte[] raw;

    public static Hash of(final byte[] raw) {
        return Arrays.equals(raw, ZERO.raw) ? ZERO : new Hash(raw);
    }

    public static Hash of(final String text) {
        return new Hash(digest(text));
    }

    public static Hash of(final Consumer<MessageDigest> fn) {
        return new Hash(digest(fn));
    }

    public static Hash parse(final String name) {
        return Hash.of(BASE16L.decode(name));
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
     * Computes a hash from a string.
     */
    public static byte[] digest(final String text) {
        return digest(md -> md.update(text.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Computes a hash.
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

    @Override
    public int compareTo(final Hash other) {
        return Arrays.compare(raw, other.raw);
    }

    public boolean isZero() {
        return this == ZERO;
    }
}
