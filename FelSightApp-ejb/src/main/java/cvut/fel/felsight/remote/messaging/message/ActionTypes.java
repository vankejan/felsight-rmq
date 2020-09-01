package cvut.fel.felsight.remote.messaging.message;

public enum ActionTypes {
    UNDEFINED("UNDEFINED"),
    GRADED("GRADED"),
    SUBMITTED("SUBMITTED"),
    OPEN("OPEN"),
    DEADLINE("DEADLINE");

    private final String value;

    ActionTypes(String value) {
        this.value = value;
    }

    public String toLowerCase() {
        return this.getValue().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
