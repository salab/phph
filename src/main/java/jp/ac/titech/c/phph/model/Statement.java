package jp.ac.titech.c.phph.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@EqualsAndHashCode(of = {"normalizedText"})
@AllArgsConstructor
public class Statement {
    @Getter
    private final String rawText;

    @Getter
    private final String normalizedText;

    @Getter
    private final int line;

    public String toString() {
        return normalizedText;
    }
}
