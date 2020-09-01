package cvut.fel.felsight.remote.messaging.message;

import cvut.fel.felsight.remote.messaging.message.value.MultilingualString;
import cvut.fel.felsight.remote.messaging.message.value.TimeObject;

import java.time.ZonedDateTime;

public class TaskMessage extends Message {

    private final String url;
    private final String course;
    private final TimeObject time;

    public TaskMessage(String senderId, String activityId, MessageTypes messageType, ActionTypes action, MultilingualString title, MultilingualString defaultTitle, MultilingualString description, ZonedDateTime created, TimeObject time, String course, String url) {
        super(senderId, activityId, messageType, action, title, defaultTitle, description, created);
        this.url = url;
        this.course = course;
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public String getCourse() {
        return course;
    }

    public TimeObject getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "TaskMessage{" +
                "url='" + url + '\'' +
                ", course='" + course + '\'' +
                ", time=" + time +
                ", senderId='" + senderId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", messageType=" + messageType +
                ", action=" + action +
                ", title=" + title +
                ", defaultTitle=" + defaultTitle +
                ", description=" + description +
                ", created=" + created +
                '}';
    }
}
