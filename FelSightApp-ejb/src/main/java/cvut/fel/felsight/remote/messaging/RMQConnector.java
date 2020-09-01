package cvut.fel.felsight.remote.messaging;

import com.rabbitmq.client.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Creates a RMQ connection according to the configuration defined in JNDI resources.
 * Automatic recovery of the connections is ensured.
 */
@Startup
@Singleton
public class RMQConnector {

    private static final Logger logger = Logger.getLogger(RMQConnector.class.getName());

    @Resource(lookup = "resource/rmq", type = Properties.class)
    private Properties connectionProperties;

    @Inject
    private RMQMessageHandlersPool rmqMessageHandlersPool;

    private RecoverableConnection connection;

    @PostConstruct
    private void init() {
        // checks for dev enviroment option
        String enabled = connectionProperties.getProperty("enabled", "false");
        if (enabled.equals("true")){

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(connectionProperties.getProperty("host"));
            connectionFactory.setPort(Integer.parseInt(connectionProperties.getProperty("port")));
            connectionFactory.setVirtualHost(connectionProperties.getProperty("virtualHost"));
            connectionFactory.setUsername(connectionProperties.getProperty("user"));
            connectionFactory.setPassword(connectionProperties.getProperty("password"));

            final String exchangeName = connectionProperties.getProperty("exchangeName");
            final String queueName = connectionProperties.getProperty("queueName");
            final String queueBinding = connectionProperties.getProperty("queueBinding");
            final String consumerTag = connectionProperties.getProperty("consumerTag");
            final String deadLetterExchangeName = connectionProperties.getProperty("deadLetterExchangeName");

            //automatic recovery settings
            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.setNetworkRecoveryInterval(Long.parseLong(connectionProperties.getProperty("recoveryIntervalMs")));

            try {
                //the factory should return an instance of RecoverableConnection since we enable the automatic recovery feature
                connection = (RecoverableConnection) connectionFactory.newConnection();

                Channel channel = connection.createChannel();

                //declare the common exchange
                channel.exchangeDeclare(exchangeName, "topic", true, false, null);
                //declare the dead letter exchange
                channel.exchangeDeclare(deadLetterExchangeName, "direct", true, false, null);
                //declare the queue and specify the dead letter exchange
                Map<String, Object> queueArgs = new HashMap<>();
                queueArgs.put("x-dead-letter-exchange", deadLetterExchangeName);
                channel.queueDeclare(queueName, false, true, false, queueArgs);
                //bind the queue to an exchange
                channel.queueBind(queueName, exchangeName, queueBinding);

                //register a new consumer
                channel.basicConsume(queueName, false, consumerTag,
                        new RMQMessagesConsumer(channel, rmqMessageHandlersPool));

                connection.addRecoveryListener(new RMQLogRecoveryListener());

                logger.info("A consumer for the exchange '" + exchangeName + "' and the queue '" + queueName + "'" +
                        " registered successfully with binding '" + queueBinding + "'. ");
            } catch (TimeoutException | IOException e) {
                logger.severe("Failed to register a consumer for queue '" + queueName + "'. Message: '" + e.getMessage() + "'.");
                closeConnection();
            }
        }
    }

    @PreDestroy
    private void destroy() {
        closeConnection();
    }

    /**
     * Tries to close the current connection.
     */
    private void closeConnection() {
        logger.info("Closing MQ connection...");
        if (connection != null) {
            try {
                //by closing the connection, we close all its channels too
                connection.close();
            } catch (IOException e1) {
                //do nothing
            }
        }
    }

    private class RMQLogRecoveryListener implements RecoveryListener {

        @Override
        public void handleRecovery(Recoverable recoverable) {
            logger.info("Connection recovered.");
        }

        @Override
        public void handleRecoveryStarted(Recoverable recoverable) {
            logger.info("Starting to recover a connection...");
        }

    }

}
