package pl.stasiak.pytel.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rafal on 2016-12-17.
 */
public class Trap {
    String sourceAdress;
    int  type;
    List<VarBindings> variableBindings = new ArrayList<>();
    Date date = new Date();

    public Trap() {
    }

    public String getSourceAdress() {
        return sourceAdress;
    }

    public void setSourceAdress(String sourceAdress) {
        this.sourceAdress = sourceAdress;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<VarBindings> getVariableBindings() {
        return variableBindings;
    }

    public void setVariableBindings(List<VarBindings> variableBindings) {
        this.variableBindings = variableBindings;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
