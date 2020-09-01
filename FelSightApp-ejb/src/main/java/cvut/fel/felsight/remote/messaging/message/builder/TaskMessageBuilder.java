package cvut.fel.felsight.remote.messaging.message.builder;

import cvut.fel.felsight.remote.messaging.message.TaskMessage;
import cvut.fel.felsight.remote.messaging.message.value.TimeObject;

public class TaskMessageBuilder extends MessageBuilder<TaskMessage, TaskMessageBuilder> {

    private String url;
    private String course;
    private TimeObject time;

    public TaskMessageBuilder withURL(String url) {
        this.url = url;
        return self();
    }

    public TaskMessageBuilder withCourse(String course){
        this.course = course;
        return self();
    }

    public TaskMessageBuilder withTime(TimeObject time){
        this.time = time;
        return self();
    }

    @Override
    public TaskMessage build() {
        return new TaskMessage(senderId, activityId, messageType, action, title, defaultTitle, description, created, time, course, url);
    }

    @Override
    protected TaskMessageBuilder self() {
        return this;
    }
}
