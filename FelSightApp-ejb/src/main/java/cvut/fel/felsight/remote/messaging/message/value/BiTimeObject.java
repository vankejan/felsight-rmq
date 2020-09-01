package cvut.fel.felsight.remote.messaging.message.value;

import java.time.ZonedDateTime;

public class BiTimeObject implements TimeObject {

    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;

    public BiTimeObject(ZonedDateTime startDate, ZonedDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public ZonedDateTime getstartDate() {
        return startDate;
    }

    @Override
    public ZonedDateTime getendDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return "BiTimeObject{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
