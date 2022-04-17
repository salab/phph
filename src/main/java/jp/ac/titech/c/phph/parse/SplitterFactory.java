package jp.ac.titech.c.phph.parse;

import com.github.durun.nitron.core.config.LangConfig;
import com.github.durun.nitron.core.config.NitronConfig;
import com.github.durun.nitron.core.config.loader.NitronConfigLoader;

import java.net.URL;
import java.util.Objects;

public class SplitterFactory {
    public enum Type {
        mpa,
        nitron,
        golang
    }

    public static Splitter create(final Type type) {
        switch (type) {
            case mpa:
                return new MPASplitter();

            case nitron:
                return createNitronSplitter("java-jdt");

            case golang:
                return createNitronSplitter("golang");

            default:
                assert false;
                return null;
        }
    }

    private static Splitter createNitronSplitter(final String configName) {
        final URL url = ClassLoader.getSystemResource("nitronConfig/nitron.json");
        final NitronConfig nitronConfig = NitronConfigLoader.INSTANCE.load(url);
        final LangConfig langConfig = Objects.requireNonNull(nitronConfig.getLangConfig().get(configName));
        return new NitronSplitter(langConfig);
    }
}
