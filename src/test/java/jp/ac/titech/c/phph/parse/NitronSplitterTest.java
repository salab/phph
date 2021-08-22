package jp.ac.titech.c.phph.parse;

import com.github.durun.nitron.core.config.LangConfig;
import com.github.durun.nitron.core.config.loader.NitronConfigLoader;
import jp.ac.titech.c.phph.model.Statement;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NitronSplitterTest {

    @Test
    void extractStatements_JDTParser() {
        final String source = "package com.example;\n" +
                "class Sample {\n" +
                "  public static void main(String[] args) {\n" +
                "    String message = \"Hello, world!\";\n" +
                "    System.out\n" +
                "      .println(message);\n" +
                "  }\n" +
                "}\n";

        final var config = loadLangConfig("java-jdt");
        final var splitter = new NitronSplitter(config);
        final var statements = splitter.split(source);
        System.out.println(statements);

        assertEqualsStatement(Statement.of("class Sample {", "class Sample {", 2, 3), statements.get(0));
        assertEqualsStatement(Statement.of("public static void main ( String [ ] args ) {", "void main ( String [ ] $V0 ) {", 3, 4), statements.get(1));
        assertEqualsStatement(Statement.of("String message = \"Hello, world!\" ;", "String $V0 = \"S\" ;", 4, 5), statements.get(2));
        assertEqualsStatement(Statement.of("System . out . println ( message ) ;", "$V1 . out . println ( $V0 ) ;", 5, 7), statements.get(3));
    }

    private static void assertEqualsStatement(Statement expected, Statement actual) {
        assertEquals(expected.getRaw(), actual.getRaw());
        assertEquals(expected.getNormalized(), actual.getNormalized());
        assertEquals(expected.getBeginLine(), actual.getBeginLine());
        assertEquals(expected.getEndLine(), actual.getEndLine());
    }

    private static LangConfig loadLangConfig(final String lang) {
        final var url = ClassLoader.getSystemResource("nitronConfig/nitron.json");
        final var path = Path.of(url.getPath());
        final var nitronConfig = NitronConfigLoader.INSTANCE.load(path);
        return Objects.requireNonNull(
                nitronConfig.getLangConfig().get(lang)
        );
    }
}