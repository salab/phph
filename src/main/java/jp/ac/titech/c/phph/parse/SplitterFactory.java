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
        plain,
        golang,
        typescript,
        javascript,
        python3
    }

    public static Splitter create(final Type type, final boolean useNonEssential) {
        switch (type) {
            case mpa:
                return new MPASplitter(useNonEssential);

            case nitron:
                return createNitronSplitter("java-jdt");

            case golang:
                return createNitronSplitter("golang");

            case typescript:
                return createNitronSplitter("typescript");

            case javascript:
                return createNitronSplitter("javascript");

            case python3:
                return createNitronSplitter("python3");

            case plain:
                return new PlainSplitter(true, true);

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
