package cvut.fel.felsight.remote.messaging.message.handler;

import cvut.fel.felsight.application.exceptions.FSAException;
import cvut.fel.felsight.application.exceptions.FSANotAllUsernamesFoundException;
import cvut.fel.felsight.datamodel.access.ApplicationSession;
import cvut.fel.felsight.datamodel.access.EventSession;
import cvut.fel.felsight.datamodel.access.ParallelSession;
import cvut.fel.felsight.datamodel.access.RecipientSession;
import cvut.fel.felsight.datamodel.access.exception.DatabaseException;
import cvut.fel.felsight.datamodel.access.exception.NotCreatedException;
import cvut.fel.felsight.datamodel.access.exception.NotDeletedException;
import cvut.fel.felsight.datamodel.access.exception.NotUpdatedException;
import cvut.fel.felsight.datamodel.access.services.NotificationService;
import cvut.fel.felsight.datamodel.aggregation.RMQEventBuilder;
import cvut.fel.felsight.datamodel.entities.Event;
import cvut.fel.felsight.datamodel.entities.LanguageField;
import cvut.fel.felsight.datamodel.entities.Recipient;
import cvut.fel.felsight.datamodel.entities.Tag;
import cvut.fel.felsight.datamodel.entities.enums.EventType;
import cvut.fel.felsight.datamodel.entities.enums.NotificationType;
import cvut.fel.felsight.datamodel.entities.enums.RemoteType;
import cvut.fel.felsight.remote.expert.RecipientExpert;
import cvut.fel.felsight.remote.kosapi.exceptions.KosApiException;
import cvut.fel.felsight.remote.kosapi.read.KosApiParallelResource;
import cvut.fel.felsight.remote.messaging.message.ActionTypes;
import cvut.fel.felsight.remote.messaging.message.MessageTypes;
import cvut.fel.felsight.remote.messaging.message.TaskMessage;
import cvut.fel.felsight.remote.messaging.targets.*;
import cvut.fel.felsight.remote.umapi.exceptions.UmApiException;
import cvut.fel.felsight.remote.umapi.read.UmapiUserResource;
import cvut.fel.felsight.services.EventUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TasksMessageHandler extends AbstractMessageHandler<TaskMessage> {

    private static final Logger logger = Logger.getLogger(TasksMessageHandler.class.getName());

    public TasksMessageHandler(RMQEventBuilder rmqEventBuilder, ApplicationSession applicationSession, RecipientSession recipientSession, ParallelSession parallelSession, KosApiParallelResource kosApiParallelResource, EventSession eventSession, EventUpdater eventUpdater, NotificationService notificationService, RecipientExpert recipientExpert, UmapiUserResource umapiUserResource) {
        super(rmqEventBuilder, applicationSession, recipientSession, parallelSession, kosApiParallelResource, eventSession, eventUpdater, notificationService, recipientExpert, umapiUserResource);
    }

    /**
     * Process message of type Event:
     * 1. tries to find event with given activity ID
     * 2.a if no event was found - create new Event
     * 2.b if event was found - update old Event by comparison
     * 3. load event for notification
     * 4. create FS notifications for given actions (GRADED, SUBMITTED)
     * @param message
     * @param recipients
     * @throws HandleMessageException
     */
    @Override
    protected void processMessage(TaskMessage message, List<Recipient> recipients) throws HandleMessageException {

        if (recipients.isEmpty()){
            logger.log(Level.WARNING, "Could not handle message: " + message.getActivityId() + ", because no recipients were found.");
            throw new HandleMessageException(message, "No recipients found.");
        }

        // try searching for the event
        Event event = eventSession.getEventByActivityId(message.getActivityId(), parseApplication(message)); // if the event already exists get it

        if (event == null){
            try {
                // create event
                rmqEventBuilder.eventCreation(parseMessageWithMessageSpecificsAsEvent(message, recipients),
                        parseApplication(message));
            }  catch (FSANotAllUsernamesFoundException e){
                logger.log(Level.WARNING, e.getDescription(), e.getNonValidUsernames());
            } catch (KosApiException | UmApiException | FSAException | DatabaseException e) {
                throw new HandleMessageException(message, e.getLocalizedMessage());
            }
        } else{ // else update event
            try {
                eventUpdater.updateEventByComparison(event, parseMessageWithMessageSpecificsAsEvent(message, recipients));
            } catch (FSANotAllUsernamesFoundException e){
                logger.log(Level.WARNING, e.getDescription(), e.getNonValidUsernames());
            } catch (NotUpdatedException | NotCreatedException | FSAException | NotDeletedException e) {
                throw new HandleMessageException(message, e.getLocalizedMessage());
            }
        }

        // load event for notifications
        event = eventSession.getEventByActivityId(message.getActivityId(), parseApplication(message)); // if the event already exists get it

        if (event == null){
            throw new HandleMessageException(message, "Error creating or updating event.");
        } else {
            // messages with some actions require creating notifications for event
            processMessageAsFSNotification(message, recipients, event);
        }
    }

    @Override
    Event parseMessageWithMessageSpecificsAsEvent(TaskMessage message, List<Recipient> recipients) throws HandleMessageException {
        Event event = parseMessageAsEvent(message, recipients);

        event.setBody(message.getTitle().getValue("cs")); // we do not recieve description, so use title as a description of event

        // TASK is exam, test or task message
        event.setType(EventType.TASK);

        if (message.getMessageType().equals(MessageTypes.EXAM)){ // EXAM
            event.setType(EventType.EXAM);
        } else if (message.getMessageType().equals(MessageTypes.TEST)){ // TEST
            event.setType(EventType.TEST);
        }

        logger.log(Level.FINE, "Message recognized as event type: " + event.getType().getText());

        // task dont have start dates
        if (!event.isTask()){
            event.setStartDate(message.getTime().getstartDate().toLocalDateTime());
        }
        event.setEndDate(message.getTime().getendDate().toLocalDateTime());

        event.setUrl(message.getUrl());

        Tag courseTag = new Tag(message.getCourse(), RemoteType.COURSE);

        event.addTag(courseTag);

        logger.log(Level.FINE, "Parsing message additional params for message: " + message.getActivityId() + " OK");

        return event;
    }

    @Override
    protected boolean processMessageAsFSNotification(TaskMessage message, List<Recipient> recipients, Event event) throws HandleMessageException {
        NotificationType type = null;

        // decide the type of notification
        if (message.getAction().equals(ActionTypes.GRADED)){
           type = NotificationType.GRADED;
        } else if (message.getAction().equals(ActionTypes.SUBMITTED)){
            type = NotificationType.SUBMITTED;
        }

        if (message.getAction().equals(ActionTypes.GRADED) || message.getAction().equals(ActionTypes.SUBMITTED)){
            try {
                // create notification for recipients, binded to the event
                return notificationService.createEventNotifications(recipientExpert.assembleAllUsernames(recipients), event, true, type);

            } catch (FSANotAllUsernamesFoundException e){
                logger.log(Level.WARNING, e.getDescription(), e.getNonValidUsernames());
            } catch (FSAException | KosApiException | UmApiException e) {
                throw new HandleMessageException(message, e.getLocalizedMessage());
            }
        }

        return false;
    }

    @Override
    public void handleMessage(TaskMessage message, CourseMessageTarget target) throws HandleMessageException {

        logger.log(Level.INFO, "Handling message:" + message.getActivityId() + "for target: " + target);

        // Add recipients
        List<Recipient> recipients = new ArrayList<>();

        try {
            recipients = getTeacherRecipientsForCourse(target.getCourse());
        } catch (KosApiException e) {
            logger.log(Level.WARNING, "Could find teacher recipients for course: " + target.getCourse() + "Exception thrown: " + e.getDescription());
        }

        Recipient recipient = recipientSession.findRecipientByLabelAndType(target.getCourse(), RemoteType.COURSE);

        if (recipient == null){
            recipient = new Recipient(target.getCourse(), RemoteType.COURSE);
        }

        recipients.add(recipient);

        // RMQ Event validation and creation/update/
        processMessage(message, recipients);
    }

    @Override
    public void handleMessage(TaskMessage message, StudentsMessageTarget target) throws HandleMessageException {

        logger.log(Level.INFO, "Handling message:" + message.getActivityId() + "for target: " + target);

        // Add recipients
        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = recipientSession.findRecipientByLabelAndType(target.getCourse(), RemoteType.COURSE);

        if (recipient == null){
            recipient = new Recipient(target.getCourse(), RemoteType.COURSE);
        }

        recipients.add(recipient);

        // RMQ Event validation and creation/update
        processMessage(message, recipients);
    }

    @Override
    public void handleMessage(TaskMessage message, TeachersMessageTarget target) throws HandleMessageException {

        logger.log(Level.INFO, "Handling message:" + message.getActivityId() + "for target: " + target);

        // FIND recipients
        List<Recipient> recipients = null;
        try {
            recipients = getTeacherRecipientsForCourse(target.getCourse());
        } catch (KosApiException e) {
            throw new HandleMessageException(message, target, e.getDescription());
        }

        // RMQ Event validation and creation/update
        processMessage(message, recipients);
    }

    @Override
    public void handleMessage(TaskMessage message, UsernameMessageTarget target) throws HandleMessageException {

        logger.log(Level.INFO, "Handling message:" + message.getActivityId() + "for target: " + target);

        // Add recipients
        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = recipientSession.findRecipientByLabelAndType(target.getUsername(), RemoteType.USER);

        if (recipient == null){
            recipient = new Recipient(target.getUsername(), RemoteType.USER);
        }

        recipients.add(recipient);

        // RMQ Event validation and creation/update
        processMessage(message, recipients);
    }

    @Override
    public void handleMessage(TaskMessage message, ParallelTeacherMessageTarget target) throws HandleMessageException {

        logger.log(Level.INFO, "Handling message:" + message.getActivityId() + "for target: " + target);

        // FIND recipients
        List<Recipient> recipients = null;
        try {
            recipients = getTeacherRecipientsForParallel(target.getParallel());
        } catch (KosApiException e) {
            throw new HandleMessageException(message, target, e.getDescription());
        }

        // RMQ Event validation and creation/update
        processMessage(message, recipients);
    }

    @Override
    public void handleMessage(TaskMessage message, ParallelStudentMessageTarget target) throws HandleMessageException {
        // FIND recipients
        List<Recipient> recipients = null;

        recipients = getStudentRecipientsForParallel(target.getParallel());

        // RMQ Event validation and creation/update
        processMessage(message, recipients);
    }

    @Override
    public void handleMessage(TaskMessage message, RoleMessageTarget target) throws HandleMessageException {

        logger.log(Level.INFO, "Handling message:" + message.getActivityId() + "for target: " + target);

        // Add recipients
        List<Recipient> recipients = null;
        try {
            recipients = getUserRecipientsForCourseBusinessRole(target.getRoleName());
        } catch (UmApiException e) {
            throw new HandleMessageException(message, target, e.getDescription());
        }

        // RMQ Event validation and creation/update
        processMessage(message, recipients);
    }
}
