package cvut.fel.felsight.remote.messaging.message.handler;

import cvut.fel.felsight.datamodel.access.ApplicationSession;
import cvut.fel.felsight.datamodel.access.EventSession;
import cvut.fel.felsight.datamodel.access.ParallelSession;
import cvut.fel.felsight.datamodel.access.RecipientSession;
import cvut.fel.felsight.datamodel.access.services.NotificationService;
import cvut.fel.felsight.datamodel.aggregation.RMQEventBuilder;
import cvut.fel.felsight.datamodel.entities.*;
import cvut.fel.felsight.datamodel.entities.enums.RemoteType;
import cvut.fel.felsight.remote.expert.RecipientExpert;
import cvut.fel.felsight.remote.kosapi.exceptions.KosApiException;
import cvut.fel.felsight.remote.kosapi.read.KosApiParallelResource;
import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.targets.HandleMessageException;
import cvut.fel.felsight.remote.umapi.exceptions.UmApiException;
import cvut.fel.felsight.remote.umapi.read.UmapiUserResource;
import cvut.fel.felsight.services.EventUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Class that contains methods for RabbitMQ message handling,
 * used in all of the concrete handlers
 * @author vankejan
 */
public abstract class AbstractMessageHandler<M extends Message> implements MessageHandler<M> {

    protected final RMQEventBuilder rmqEventBuilder;
    protected final ApplicationSession applicationSession;
    protected final RecipientSession recipientSession;
    protected final ParallelSession parallelSession;
    protected final KosApiParallelResource kosApiParallelResource;
    protected final NotificationService notificationService;
    protected final EventSession eventSession;
    protected final EventUpdater eventUpdater;
    protected final RecipientExpert recipientExpert;
    protected final UmapiUserResource umapiUserResource;

    private static final Logger logger = Logger.getLogger(AbstractMessageHandler.class.getName());

    public AbstractMessageHandler(RMQEventBuilder rmqEventBuilder, ApplicationSession applicationSession, RecipientSession recipientSession, ParallelSession parallelSession, KosApiParallelResource kosApiParallelResource, EventSession eventSession, EventUpdater eventUpdater, NotificationService notificationService, RecipientExpert recipientExpert, UmapiUserResource umapiUserResource) {
        this.rmqEventBuilder = rmqEventBuilder;
        this.applicationSession = applicationSession;
        this.recipientSession = recipientSession;
        this.parallelSession = parallelSession;
        this.kosApiParallelResource = kosApiParallelResource;
        this.notificationService = notificationService;
        this.eventUpdater = eventUpdater;
        this.eventSession = eventSession;
        this.recipientExpert = recipientExpert;
        this.umapiUserResource = umapiUserResource;
    }

    /**
     * Returns Application from message sender ID
     * @param message
     * @return Application
     * @throws HandleMessageException
     */
    protected Application parseApplication(M message) throws HandleMessageException {
        Application app = applicationSession.findApplicationByAppId(message.getSenderId());
        if (app == null){
            throw new HandleMessageException(message, "Unknown source application.");
        }
        logger.log(Level.FINE, "Parsing appliacation from message: "+message.getActivityId()+" OK.");
        return app;
    }

    /**
     * Sets basic parameters to event
     * @param message
     * @param recipients
     * @return Event
     * @throws HandleMessageException
     */
    protected Event parseMessageAsEvent(M message, List<Recipient> recipients) throws HandleMessageException {
        Event event = new Event();

        event.setActivityId(message.getActivityId());
        event.setTitle(message.getTitle().getValue("cs"));
        event.setCreatedDate(message.getCreated().toLocalDateTime());

        // Recipients
        // add recipients
        for (Recipient recipient: recipients ) {
            event.getRecipients().add(recipient);
        }

        logger.log(Level.FINE, "Basic parsing of message params as event for message: " + message.getActivityId() + " OK");

        return event;
    }

    /**
     * Returns List of teacher recipients for course
     * @param course
     * @return
     * @throws KosApiException
     */
    protected List<Recipient> getTeacherRecipientsForCourse(String course) throws KosApiException {

        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = null;

        List<Parallel> parallels = kosApiParallelResource.getCoursesParallels(course);

        if (parallels != null){
            for (Parallel parallel:  parallels) { // get all parallels
                for (User user: parallel.getTeachers()) { // get all teachers

                    // ADD teacher recipients
                    recipient = recipientSession.findRecipientByLabelAndType(user.getUsername(), RemoteType.USER);

                    if (recipient == null){
                        recipient = new Recipient(user.getUsername(), RemoteType.USER);
                    }

                    if (!recipients.contains(recipient)){
                        recipients.add(recipient);
                    }
                }
            }
        }

        return recipients;
    }


    /**
     * Returns List of teacher recipients for parallel of a course
     * @param parallelSearch
     * @return
     * @throws KosApiException
     */
    protected List<Recipient> getTeacherRecipientsForParallel(String parallelSearch) throws KosApiException {
        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = null;

        Parallel parallel = parallelSession.findParallelByExternalId(parallelSearch);

        if (parallel != null){
            for (User user: parallel.getTeachers()) { // get all teachers
                // ADD teacher recipients
                recipient = recipientSession.findRecipientByLabelAndType(user.getUsername(), RemoteType.USER);
                if (recipient == null){
                    recipient = new Recipient(user.getUsername(), RemoteType.USER);
                }

                if (!recipients.contains(recipient)){
                    recipients.add(recipient);
                }
            }
        }

        return recipients;
    }

    /**
     * Returns List of student recipients for parallel of a course
     * @param parallelSearch
     * @return
     */
    protected List<Recipient> getStudentRecipientsForParallel(String parallelSearch) {

        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = null;

        Parallel parallel = parallelSession.findParallelByExternalId(parallelSearch);

        if (parallel != null){
            for (UserParallelRelation relation: parallel.getUserRelations()) { // get all teachers

                // ADD student recipients
                recipient = recipientSession.findRecipientByLabelAndType(
                        relation.getUser().getUsername(), RemoteType.USER);

                if (recipient == null){
                    // create new recipient
                    recipient = new Recipient(
                            relation.getUser().getUsername(), RemoteType.USER);
                }

                if (!recipients.contains(recipient)){
                    recipients.add(recipient);
                }
            }
        }

        return recipients;
    }


    /**
     * Returns List of user recipients with given business role
     * @param businessRole
     * @return
     * @throws KosApiException
     */
    protected List<Recipient> getUserRecipientsForCourseBusinessRole(String businessRole) throws UmApiException {

        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = null;

        List<User> users = umapiUserResource.getUsers(businessRole);

        for (User user: users) {

            // find user recipients by username
            recipient = recipientSession.findRecipientByLabelAndType(user.getUsername(), RemoteType.USER);

            if (recipient == null){
                recipient = new Recipient(user.getUsername(), RemoteType.USER);
            }

            if (!recipients.contains(recipient)){
                recipients.add(recipient);
            }
        }

        return recipients;
    }

    /**
     * Updates event if is is found by activityId, if not creates new event
     * in some cases (event, exam, test)
     * @param message
     * @param recipients
     * @throws HandleMessageException
     */
    abstract void processMessage(M message, List<Recipient> recipients) throws HandleMessageException;

    /**
     * Adds Message specific parameters to the Event
     * including the EventType
     * @param message
     * @param recipients
     * @return Event
     */
    abstract Event parseMessageWithMessageSpecificsAsEvent(M message, List<Recipient> recipients) throws HandleMessageException;

    /**
     * Processes the message as notification,
     * for event
     * @param message
     * @param recipients
     * @return
     * @throws HandleMessageException
     */
    abstract boolean processMessageAsFSNotification(M message, List<Recipient> recipients, Event event) throws HandleMessageException;

}
