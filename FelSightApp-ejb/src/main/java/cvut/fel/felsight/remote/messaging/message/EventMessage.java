package cvut.fel.felsight.remote.messaging.message;

import cvut.fel.felsight.remote.messaging.message.value.MultilingualString;
import cvut.fel.felsight.remote.messaging.message.value.TimeObject;

import java.time.ZonedDateTime;

public class EventMessage extends Message {

    private final String course;
    private final String room;
    private final String semester;
    private final TimeObject time;
    private final String url;

    public EventMessage(String senderId, String activityId, MessageTypes messageType, ActionTypes action, MultilingualString title, MultilingualString defaultTitle, MultilingualString description, ZonedDateTime created, String course, String room, String semester, TimeObject time, String url) {
        super(senderId, activityId, messageType, action, title, defaultTitle, description, created);
        this.course = course;
        this.room = room;
        this.semester = semester;
        this.time = time;
        this.url = url;
    }

    public String getCourse() {
        return course;
    }

    public String getRoom() {
        return room;
    }

    public String getSemester() {
        return semester;
    }

    public TimeObject getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "course='" + course + '\'' +
                ", room='" + room + '\'' +
                ", semester='" + semester + '\'' +
                ", time=" + time +
                ", url='" + url + '\'' +
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
