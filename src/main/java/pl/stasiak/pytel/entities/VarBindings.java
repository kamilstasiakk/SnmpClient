package pl.stasiak.pytel.entities;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

/**
 * Created by Rafal on 2016-12-17.
 */
public class VarBindings {
    OID oid;
    Variable variable;

    public VarBindings() {
    }

    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }
}
