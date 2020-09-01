package cvut.fel.felsight.remote.messaging.targets;

import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;

import java.util.Objects;

/**
 * Sends a message to all individuals having a particular business role.
 */
public class RoleMessageTarget implements MessageTarget {

    private final String roleName;

    public RoleMessageTarget(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleMessageTarget that = (RoleMessageTarget) o;
        return roleName.equals(that.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleName);
    }

    @Override
    public <M extends Message> void accept(M message, MessageHandler<M> handler) throws HandleMessageException {
        handler.handleMessage(message, this);
    }

    @Override
    public String toString() {
        return "RoleMessageTarget{" +
                "roleName='" + roleName + '\'' +
                '}';
    }
}
