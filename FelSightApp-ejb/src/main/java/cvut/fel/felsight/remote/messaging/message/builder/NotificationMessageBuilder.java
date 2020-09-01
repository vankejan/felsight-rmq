package cvut.fel.felsight.remote.messaging.message.builder;

import cvut.fel.felsight.remote.messaging.message.NotificationMessage;

public class NotificationMessageBuilder extends MessageBuilder<NotificationMessage, NotificationMessageBuilder> {

    private String url;
    private String course;

    public NotificationMessageBuilder withURL(String url) {
        this.url = url;
        return self();
    }

    public NotificationMessageBuilder withCourse(String course) {
        this.course = course;
        return self();
    }

    @Override
    public NotificationMessage build() {
        return new NotificationMessage(senderId, activityId, messageType, action,  title, defaultTitle, description, created, url, course);
    }

    @Override
    protected NotificationMessageBuilder self() {
        return this;
    }
}
