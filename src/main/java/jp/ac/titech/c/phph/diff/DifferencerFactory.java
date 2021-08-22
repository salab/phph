package jp.ac.titech.c.phph.diff;

public class DifferencerFactory {
    public enum Type {
        dp,
        myers,
        histogram
    }

    public static <T> Differencer<T> create(final Type type) {
        switch (type) {
            case dp:
                return new DynamicProgrammingDifferencer<>();
            case myers:
                return JGitDifferencer.newMyers();
            case histogram:
                return JGitDifferencer.newHistorgram();
            default:
                assert false;
                return null;
        }
    }
}
