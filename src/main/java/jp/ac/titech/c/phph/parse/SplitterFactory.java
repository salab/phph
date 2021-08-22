package jp.ac.titech.c.phph.parse;

import com.github.durun.nitron.core.config.LangConfig;
import com.github.durun.nitron.core.config.NitronConfig;
import com.github.durun.nitron.core.config.loader.NitronConfigLoader;

import java.nio.file.Path;
import java.util.Objects;

public class SplitterFactory {
    public enum Type {
        mpa,
        nitron
    }

    public static Splitter create(final Type type) {
        switch (type) {
            case mpa:
                return new MPASplitter();

            case nitron:
                final Path path = Path.of(ClassLoader.getSystemResource("nitronConfig/nitron.json").getPath());
                final NitronConfig nitronConfig = NitronConfigLoader.INSTANCE.load(path);
                final LangConfig langConfig = Objects.requireNonNull(nitronConfig.getLangConfig().get("java-jdt"));
                return new NitronSplitter(langConfig);

            default:
                assert false;
                return null;
        }
    }
}
