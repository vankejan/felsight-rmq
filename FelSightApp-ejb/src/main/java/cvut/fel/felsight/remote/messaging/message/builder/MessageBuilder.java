package cvut.fel.felsight.remote.messaging.message.builder;

import cvut.fel.felsight.remote.messaging.message.ActionTypes;
import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.MessageTypes;
import cvut.fel.felsight.remote.messaging.message.value.MultilingualString;

import java.time.ZonedDateTime;

/**
 * Each builder of a Message should inherit from this abstract class.
 *
 * @param <M>    Type of the message this builder builds.
 * @param <This> Type of the builder that inherits from this abstract builder.
 */
public abstract class MessageBuilder<M extends Message, This extends MessageBuilder<M, This>> {

    protected String senderId;
    protected String activityId;
    protected ActionTypes action;
    protected MessageTypes messageType;
    protected MultilingualString title;
    protected MultilingualString defaultTitle;
    protected MultilingualString description;
    protected ZonedDateTime created;

    public This withSenderId(String senderId) {
        this.senderId = senderId;
        return self();
    }

    public This withActivityId(String activityId) {
        this.activityId = activityId;
        return self();
    }

    public This withAction(ActionTypes action) {
        this.action = action;
        return self();
    }

    public This withTitle(MultilingualString title) {
        this.title = title;
        return self();
    }

    public This withDefaultTitle(MultilingualString defaultTitle) {
        this.defaultTitle = defaultTitle;
        return self();
    }

    public This withDescription(MultilingualString description) {
        this.description = description;
        return self();
    }

    public This withCreated(ZonedDateTime created) {
        this.created = created;
        return self();
    }

    public This withMessageType(MessageTypes messageType){
        this.messageType = messageType;
        return self();
    }

    /**
     * @return a new instance of message that is being built by the builder
     */
    public abstract M build();

    protected abstract This self();

}
