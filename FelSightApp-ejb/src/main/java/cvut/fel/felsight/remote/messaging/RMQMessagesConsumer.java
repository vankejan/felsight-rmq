package cvut.fel.felsight.remote.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.MessageFactory;
import cvut.fel.felsight.remote.messaging.message.MessageFactoryException;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;
import cvut.fel.felsight.remote.messaging.targets.HandleMessageException;
import cvut.fel.felsight.remote.messaging.targets.MessageTarget;
import cvut.fel.felsight.remote.messaging.targets.MessageTargetFactory;
import cvut.fel.felsight.remote.messaging.targets.MessageTargetFactoryException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Consumes messages from a message queue. The consumer handles message confirmation so it is advised not to use
 * the automatic confirmations.
 */
public class RMQMessagesConsumer extends DefaultConsumer {

    private static final Logger logger = Logger.getLogger(RMQMessagesConsumer.class.getName());

    private final MessageFactory messageFactory;
    private final MessageTargetFactory messageTargetFactory;
    private final RMQMessageHandlersPool rmqMessageHandlersPool;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public RMQMessagesConsumer(Channel channel, RMQMessageHandlersPool rmqMessageHandlersPool) {
        super(channel);
        this.messageFactory = new MessageFactory();
        this.messageTargetFactory = new MessageTargetFactory();
        this.rmqMessageHandlersPool = rmqMessageHandlersPool;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Channel channel = getChannel();
        String senderId = properties.getUserId();
        String content = new String(body, StandardCharsets.UTF_8);
        try {
            //parse the message target (i.e., the recipient)
            MessageTarget target = messageTargetFactory.createMessageTarget(envelope.getRoutingKey());
            //parse the message content
            Message message = messageFactory.parseMessage(content, senderId);

            MessageHandler<Message> messageHandler = rmqMessageHandlersPool.getMessageHandler(message);

            //handle the message given the message target
            target.accept(message, messageHandler);

            //send acknowledgment
            channel.basicAck(envelope.getDeliveryTag(), false);
            logger.fine("A RMQ message successfully handled.");
        } catch (MessageFactoryException | MessageTargetFactoryException | HandleMessageException e) {
            channel.basicReject(envelope.getDeliveryTag(), false);
            logger.severe(e.getMessage());
        }
    }

}
