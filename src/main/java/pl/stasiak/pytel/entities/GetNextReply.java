package pl.stasiak.pytel.entities;

/**
 * Created by Kamil on 10.12.2016.
 */
public class GetNextReply {
    String value;
    String oid;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public GetNextReply(String value, String oid) {
        this.value = value;
        this.oid = oid;
    }
}
