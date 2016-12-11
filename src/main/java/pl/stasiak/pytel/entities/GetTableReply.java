package pl.stasiak.pytel.entities;

import java.util.List;

/**
 * Created by Kamil on 11.12.2016.
 */
public class GetTableReply {

    List<String> columnsNames;
    List<TableRecord> values;

    public List<String> getColumnsNames() {
        return columnsNames;
    }

    public void setColumnsNames(List<String> columnsNames) {
        this.columnsNames = columnsNames;
    }

    public List<TableRecord> getValues() {
        return values;
    }

    public void setValues(List<TableRecord> values) {
        this.values = values;
    }

    public GetTableReply(List<String> columnsNames, List<TableRecord> values) {
        this.columnsNames = columnsNames;
        this.values = values;
    }
}
