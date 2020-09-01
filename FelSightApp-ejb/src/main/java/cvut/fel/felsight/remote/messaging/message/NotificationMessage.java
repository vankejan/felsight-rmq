package cvut.fel.felsight.remote.messaging.message;

import cvut.fel.felsight.remote.messaging.message.value.MultilingualString;

import java.time.ZonedDateTime;

public class NotificationMessage extends Message {

    private final String url;
    private final String course;

    public NotificationMessage(String senderId, String activityId, MessageTypes messageType, ActionTypes action, MultilingualString title, MultilingualString defaultTitle, MultilingualString description, ZonedDateTime created, String url, String course) {
        super(senderId, activityId, messageType, action, title, defaultTitle, description, created);
        this.url = url;
        this.course = course;
    }

    public String getUrl() {
        return url;
    }

    public String getCourse() {
        return course;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "url='" + url + '\'' +
                ", senderId='" + senderId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", messageType=" + messageType +
                ", action=" + action +
                ", title=" + title +
                ", defaultTitle=" + defaultTitle +
                ", description=" + description +
                ", created=" + created +
                ", course=" + course +
                '}';
    }
}
