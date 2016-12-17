package pl.stasiak.pytel.entities;

/**
 * Created by Kamil on 12.12.2016.
 */
public class GetWithTimeReply {

    String value;
    String oid;
    String time;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public GetWithTimeReply(String value, String oid, String time) {
        this.value = value;
        this.oid = oid;
        this.time = time;
    }
}
