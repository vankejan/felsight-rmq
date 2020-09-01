package cvut.fel.felsight.remote.messaging.message.value;

public class CsEnValueString implements MultilingualString {

    private final String csValue;
    private final String enValue;

    public CsEnValueString(String csValue, String enValue) {
        this.csValue = csValue;
        this.enValue = enValue;
    }

    @Override
    public String getValue(String language) {
        if (language.equalsIgnoreCase("en")) {
            return this.enValue;
        } else {
            return this.csValue;
        }
    }

    @Override
    public String toString() {
        return "{cs='" + csValue + "', en='" + enValue + "'}";
    }

}
