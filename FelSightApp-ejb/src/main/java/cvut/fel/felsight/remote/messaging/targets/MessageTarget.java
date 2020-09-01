package cvut.fel.felsight.remote.messaging.targets;

import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;

/**
 * Represents an individual or a group of individuals to which a message
 * is being addressed.
 */
public interface MessageTarget {

    <M extends Message> void accept(M message, MessageHandler<M> handler) throws HandleMessageException;

}
