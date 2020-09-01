package cvut.fel.felsight.remote.messaging.message.builder;

import cvut.fel.felsight.remote.messaging.message.EventMessage;
import cvut.fel.felsight.remote.messaging.message.value.TimeObject;

public class EventMessageBuilder extends MessageBuilder<EventMessage, EventMessageBuilder> {

    private String url;
    private String course;
    private TimeObject time;
    private String room;
    private String semester;

    public EventMessageBuilder withURL(String url) {
        this.url = url;
        return self();
    }

    public EventMessageBuilder withCourse(String course){
        this.course = course;
        return self();
    }

    public EventMessageBuilder withTime(TimeObject time){
        this.time = time;
        return self();
    }

    public EventMessageBuilder withRoom(String room){
        this.room = room;
        return self();
    }

    public EventMessageBuilder withSemester(String semester){
        this.semester = semester;
        return self();
    }

    @Override
    public EventMessage build() {
        return new EventMessage(senderId, activityId, messageType, action, title, defaultTitle, description, created, course, room, semester, time, url);
    }

    @Override
    protected EventMessageBuilder self() {
        return this;
    }
}
