package cvut.fel.felsight.remote.messaging.message;

public class MessageFactoryException extends Exception {

    private static final long serialVersionUID = 3039512876849288336L;

    /**
     * @param messageBody message that we tried to parse
     * @param cause       what is wrong with the message
     */
    public MessageFactoryException(String messageBody, String cause) {
        super(createMessage(messageBody, cause));
    }

    private static String createMessage(String messageBody, String cause) {
        return "Could not compose a RMQ message from incoming data: [" + messageBody + "]. Cause: [" + cause + "].";
    }

}
