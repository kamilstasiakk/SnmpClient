package pl.stasiak.pytel.entities;

import java.util.List;

/**
 * Created by Kamil on 11.12.2016.
 */
public class TableRecord {
    List<String> fields;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public TableRecord(List<String> fields) {
        this.fields = fields;
    }
}
