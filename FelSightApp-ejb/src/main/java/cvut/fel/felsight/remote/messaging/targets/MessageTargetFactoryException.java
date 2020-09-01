package cvut.fel.felsight.remote.messaging.targets;

public class MessageTargetFactoryException extends Exception {

    private static final long serialVersionUID = 5146907606469984484L;

    /**
     * @param routingKey routing key which could not be parsed
     */
    public MessageTargetFactoryException(String routingKey) {
        super("Could not determine a RMQ message target from incoming routing key: [" + routingKey + "]");
    }

}
