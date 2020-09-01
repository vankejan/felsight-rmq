package cvut.fel.felsight.remote.messaging.targets;

import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;

import java.util.Objects;

/**
 * A message is meant for a single user.
 */
public class UsernameMessageTarget implements MessageTarget {

    private final String username;

    public UsernameMessageTarget(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsernameMessageTarget that = (UsernameMessageTarget) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public <M extends Message> void accept(M message, MessageHandler<M> handler) throws HandleMessageException {
        handler.handleMessage(message, this);
    }

    @Override
    public String toString() {
        return "UsernameMessageTarget{" +
                "username='" + username + '\'' +
                '}';
    }
}
