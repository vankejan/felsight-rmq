package cvut.fel.felsight.remote.messaging;

import cvut.fel.felsight.datamodel.access.ApplicationSession;
import cvut.fel.felsight.datamodel.access.EventSession;
import cvut.fel.felsight.datamodel.access.ParallelSession;
import cvut.fel.felsight.datamodel.access.RecipientSession;
import cvut.fel.felsight.datamodel.access.services.NotificationService;
import cvut.fel.felsight.datamodel.aggregation.RMQEventBuilder;
import cvut.fel.felsight.remote.expert.RecipientExpert;
import cvut.fel.felsight.remote.kosapi.read.KosApiParallelResource;
import cvut.fel.felsight.remote.messaging.message.EventMessage;
import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.NotificationMessage;
import cvut.fel.felsight.remote.messaging.message.TaskMessage;
import cvut.fel.felsight.remote.messaging.message.handler.EventMessageHandler;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;
import cvut.fel.felsight.remote.messaging.message.handler.NotificationMessageHandler;
import cvut.fel.felsight.remote.messaging.message.handler.TasksMessageHandler;
import cvut.fel.felsight.remote.umapi.read.UmapiUserResource;
import cvut.fel.felsight.services.EventUpdater;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class RMQMessageHandlersPool {

    @Inject
    private RMQEventBuilder rmqEventBuilder;

    @Inject
    private ApplicationSession applicationSession;

    @Inject
    private EventSession eventSession;

    @Inject
    private RecipientSession recipientSession;

    @Inject
    private EventUpdater eventUpdater;

    @Inject
    private KosApiParallelResource kosApiParallelResource;

    @Inject
    private ParallelSession parallelSession;

    @Inject
    private NotificationService notificationService;

    @Inject
    private RecipientExpert recipientExpert;

    @Inject
    private UmapiUserResource umapiUserResource;

    private Map<Class<? extends Message>, MessageHandler<? extends Message>> messageHandlerMap;

    @PostConstruct
    private void init(){
        Map<Class<? extends Message>, MessageHandler<? extends Message>> messageHandlers = new HashMap<>();
        messageHandlers.put(NotificationMessage.class, new NotificationMessageHandler(rmqEventBuilder, applicationSession, recipientSession, parallelSession, kosApiParallelResource, eventSession, eventUpdater, notificationService, recipientExpert, umapiUserResource));
        messageHandlers.put(TaskMessage.class, new TasksMessageHandler(rmqEventBuilder, applicationSession, recipientSession, parallelSession, kosApiParallelResource, eventSession, eventUpdater, notificationService, recipientExpert, umapiUserResource));
        messageHandlers.put(EventMessage.class, new EventMessageHandler(rmqEventBuilder, applicationSession, recipientSession, parallelSession, kosApiParallelResource, eventSession, eventUpdater, notificationService, recipientExpert, umapiUserResource));

        this.messageHandlerMap = Collections.unmodifiableMap(messageHandlers);
    }

    @SuppressWarnings("unchecked")
    public MessageHandler<Message> getMessageHandler(Message messageClass) {
        return (MessageHandler<Message>) this.messageHandlerMap.get(messageClass.getClass());
    }

}
