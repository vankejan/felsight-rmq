package cvut.fel.felsight.remote.messaging.message;

import cvut.fel.felsight.remote.messaging.message.value.MultilingualString;

import java.time.ZonedDateTime;

public abstract class Message {

    protected final String senderId;
    protected final String activityId;
    protected final MessageTypes messageType;
    protected final ActionTypes action;
    protected final MultilingualString title;
    protected final MultilingualString defaultTitle;
    protected final MultilingualString description;
    protected final ZonedDateTime created;

    public Message(String senderId, String activityId, MessageTypes messageType, ActionTypes action, MultilingualString title, MultilingualString defaultTitle, MultilingualString description, ZonedDateTime created) {
        this.senderId = senderId;
        this.activityId = activityId;
        this.messageType = messageType;
        this.action = action;
        this.title = title;
        this.defaultTitle = defaultTitle;
        this.description = description;
        this.created = created;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getActivityId() {
        return activityId;
    }

    public MultilingualString getTitle() {
        return title;
    }

    public MultilingualString getDescription() {
        return description;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ActionTypes getAction() {
        return action;
    }

    public MultilingualString getDefaultTitle() {
        return defaultTitle;
    }

    public MessageTypes getMessageType() {
        return messageType;
    }
}