package cvut.fel.felsight.remote.messaging.message.value;

import java.time.ZonedDateTime;

public class SingleTimeObject implements TimeObject {

    private final ZonedDateTime value;

    public SingleTimeObject(ZonedDateTime value) {
        this.value = value;
    }

    @Override
    public ZonedDateTime getstartDate() {
        return value;
    }

    @Override
    public ZonedDateTime getendDate() {
        return value;
    }

    @Override
    public String toString() {
        return "SingleTimeObject{" +
                "value=" + value +
                '}';
    }
}
