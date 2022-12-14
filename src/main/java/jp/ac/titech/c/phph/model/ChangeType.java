package jp.ac.titech.c.phph.model;

public enum ChangeType {
    MODIFY,
    ADD,
    DELETE,
    EMPTY;

    public int getId() {
        return ordinal();
    }

    @Override
    public String toString() {
        switch (this) {
            case MODIFY:
                return "MOD";
            case ADD:
                return "ADD";
            case DELETE:
                return "DEL";
            case EMPTY:
                return "EMP";
            default:
                throw new AssertionError("Illegal enum");
        }
    }
}
