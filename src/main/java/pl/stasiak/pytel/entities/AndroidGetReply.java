package pl.stasiak.pytel.entities;

/**
 * Created by Kamil on 07.01.2017.
 */
public class AndroidGetReply {

    String value;
    String name;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AndroidGetReply(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
