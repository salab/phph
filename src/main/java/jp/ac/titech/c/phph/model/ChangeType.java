package jp.ac.titech.c.phph.model;

public enum ChangeType {
    MODIFY,
    ADD,
    DELETE,
    EMPTY;

    public int getId() {
        return ordinal();
    }
}
