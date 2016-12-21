package pl.stasiak.pytel.entities;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

/**
 * Created by Rafal on 2016-12-17.
 */
public class VarBindings {
    String oid;
    String variable;

    public VarBindings() {
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
}
