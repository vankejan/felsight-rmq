package cvut.fel.felsight.remote.messaging.message;

public enum MessageTypes {
    EXAM("exam"),
    TEST("test"),
    TASK("task"),
    EVENT("event"),
    NOTIFICATION("notification");

    private final String value;

    MessageTypes(String value) {
        this.value = value;
    }

    public String toLowerCase() {
        return this.getValue().toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
