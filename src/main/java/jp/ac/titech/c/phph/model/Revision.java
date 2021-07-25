package jp.ac.titech.c.phph.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.Date;

@Value
@AllArgsConstructor
public class Revision {
    String id;

    String message;

    String author;

    Date createdAt;
}
