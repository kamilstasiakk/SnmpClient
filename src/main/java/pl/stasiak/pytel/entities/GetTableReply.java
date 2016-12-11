package pl.stasiak.pytel.entities;

import java.util.List;

/**
 * Created by Kamil on 11.12.2016.
 */
public class GetTableReply {

    List<String> columnsNames;
    List<List<String>> values;

    public List<String> getColumnsNames() {
        return columnsNames;
    }

    public void setColumnsNames(List<String> columnsNames) {
        this.columnsNames = columnsNames;
    }

    public List<List<String>> getValues() {
        return values;
    }

    public void setValues(List<List<String>> values) {
        this.values = values;
    }

    public GetTableReply(List<String> columnsNames, List<List<String>> values) {
        this.columnsNames = columnsNames;
        this.values = values;
    }
}
