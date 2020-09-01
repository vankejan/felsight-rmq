package cvut.fel.felsight.remote.messaging.message.value;

public class MonolingualValueString implements MultilingualString {

    private final String value;

    public MonolingualValueString(String value) {
        this.value = value;
    }

    @Override
    public String getValue(String language) {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
