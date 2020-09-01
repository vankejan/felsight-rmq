package cvut.fel.felsight.remote.messaging.targets;

import cvut.fel.felsight.remote.messaging.message.Message;

/**
 * Raised when a message could not be delivered.
 */
public class HandleMessageException extends Exception {

    private static final long serialVersionUID = 6850288652474801190L;

    public HandleMessageException(Message message, String reason) {
        super("Could not handle a RMQ message: [" + message.getActivityId() + "]. The reason was: [" + reason + "]");
    }

    public HandleMessageException(Message message, MessageTarget target, String reason) {
        super("Could not handle a RMQ message: [" + message.getActivityId() + "] for target [" + target.toString() + "]. The reason was: [" + reason + "]");
    }

}
