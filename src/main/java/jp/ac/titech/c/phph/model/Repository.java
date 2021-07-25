package jp.ac.titech.c.phph.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Date;

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@Accessors(fluent = true)
public class Repository {
    private final int id;

    private final String path;

    private final String from;

    private final String to;
}
