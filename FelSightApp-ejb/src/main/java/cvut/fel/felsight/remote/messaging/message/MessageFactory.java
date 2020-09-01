package cvut.fel.felsight.remote.messaging.message;

import cvut.fel.felsight.remote.messaging.message.builder.EventMessageBuilder;
import cvut.fel.felsight.remote.messaging.message.builder.MessageBuilder;
import cvut.fel.felsight.remote.messaging.message.builder.NotificationMessageBuilder;
import cvut.fel.felsight.remote.messaging.message.value.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageFactory {

    private static final Logger logger = Logger.getLogger(MessageFactory.class.getName());

    /**
     * Parse Json input string as Message Object
     * @param input String (Json)
     * @param senderId
     * @return Message object
     * @throws MessageFactoryException if type of object or content of the message is not present
     */
    public Message parseMessage(String input, String senderId) throws MessageFactoryException{
        MessageBuilder<?, ?> messageBuilder = null;
        JSONObject content = null;
        String type = null;

        // get type of message and content
        try {
            content = new JSONObject(input);
            type = content.getString("type");
        } catch (JSONException e) {
            throw new MessageFactoryException(input, e.getMessage());
        }

        // OPTIONAL PARAMETERS
        MultilingualString description = trimInvalidDataFromDescription(multilingualStringFromJson(content, "description"));
        JSONObject info = null;
        String course = null;
        String room = null;
        String url = null;
        String semester = null;
        TimeObject time = null;

        try {
            info = content.getJSONObject("info");
        } catch (JSONException e) { // just log which optional params are not present
            logger.log(Level.FINE, e.getLocalizedMessage());
        }

        if (info != null){
            time = timeObjectFromJson(info, "time");
            try {
                course = info.getString("course");
            } catch (JSONException e) { // just log which optional params are not present
                logger.log(Level.FINE, e.getLocalizedMessage());
            }
            try {
                url = info.getString("url");
            } catch (JSONException e) { // just log which optional params are not present
                logger.log(Level.FINE, e.getLocalizedMessage());
            }
            try {
                room = info.getString("room");
            } catch (JSONException e) { // just log which optional params are not present
                logger.log(Level.FINE, e.getLocalizedMessage());
            }
            try {
                semester = info.getString("semester");
            } catch (JSONException e) { // just log which optional params are not present
                logger.log(Level.FINE, e.getLocalizedMessage());
            }
        }

        // DECIDE the right builder and set MESSAGE SPECIFIC PARAMETERS
        switch (MessageTypes.valueOf(type.toUpperCase())){
            case NOTIFICATION:
                messageBuilder = new NotificationMessageBuilder()
                        .withDescription(description)
                        .withURL(url);
                break;
            case EXAM:
            case TASK:
            case TEST:
                if (time == null){ // There is a restriction for deadlines, that all deadlines need to have end date set. So we parse messages with no end date as plain notifications.
                    messageBuilder = new NotificationMessageBuilder()
                            .withDescription(description)
                            .withURL(url)
                            .withCourse(course);
                } else {
                    messageBuilder = new EventMessageBuilder()
                            .withCourse(course)
                            .withDescription(description)
                            .withTime(time)
                            .withURL(url);
                }
                break;
            case EVENT:
                if (time == null || time.getstartDate() == null || time.getendDate() == null){ // There is a restriction for event, that all events need to have start and end date set. So we parse messages with no end date as plain notifications.
                    messageBuilder = new NotificationMessageBuilder()
                            .withDescription(description)
                            .withURL(url);
                } else {
                    messageBuilder = new EventMessageBuilder()
                            .withCourse(course)
                            .withDescription(description)
                            .withTime(time)
                            .withURL(url)
                            .withRoom(room)
                            .withSemester(semester);
                }
                break;
            default:
                throw new UnsupportedOperationException("Message type: " + type + " is not supported or was not recognized.");
        }

        // ACTION is optional (set to undefined if not specified)
        ActionTypes action = ActionTypes.UNDEFINED;
        try {
            action = ActionTypes.valueOf(content.getString("action").toUpperCase());
        } catch (IllegalArgumentException | JSONException e) {
            logger.log(Level.WARNING, "Recieved unknown action parsing as undefined.");
        }

        // OBLIGATORY params (throw MessageFactoryException if missing)
        try {
            messageBuilder = messageBuilder.withActivityId(content.getString("aid"))
                                .withAction(action)
                                .withMessageType(MessageTypes.valueOf(type.toUpperCase()))
                                .withTitle(multilingualStringFromJson(content, "title"))
                                .withDefaultTitle(multilingualStringFromJson(content, "defaulttitle"))
                                .withCreated(ZonedDateTime.parse(content.getString("created")))
                                .withSenderId(senderId);

        } catch (JSONException e) {
            throw new MessageFactoryException(input, e.getMessage());
        }

        return messageBuilder.build();
    }

    /**
     * THIS IS HOTFIX METHOD ONLY!
     * clears description from invalid substring
     * TODO: ERASE IN VERSION 2.12
     * @param description
     * @return
     */
    private MultilingualString trimInvalidDataFromDescription(MultilingualString description){
        if (description == null){
            return null;
        }

        String czech = description.getValue("cs");
        String english = description.getValue("en");

        if (czech.equals(english)){ // mono
            return new MonolingualValueString(czech.replace("a termín jeho odevzdání je 01.01.1970 01:00.", "."));
        } else { // bi
            return new CsEnValueString(czech.replace("a termín jeho odevzdání je 01.01.1970 01:00.", "."), english);
        }
    }

     /**
     * Parses JSONObject as MultiLingual String, or Monolingual String if only one language is present
     * Also cleans Strings from HTML (excluding simple text tags)
     *
     * @param json
     * @param key
     * @return
     * @see org.jsoup.safety.Whitelist#simpleText
     */
    private static MultilingualString multilingualStringFromJson(JSONObject json, String key) {
        if (json == null){
            return null;
        }

        try {
            JSONObject multilingual = json.getJSONObject(key);

            return new CsEnValueString(Jsoup.clean(multilingual.getString("cs"), Whitelist.simpleText()), Jsoup.clean(multilingual.getString("en"), Whitelist.simpleText()));

        } catch (JSONException e) {
            try {
                return new MonolingualValueString(json.getString(key));
            } catch (JSONException ex){
                return null;
            }
        }
    }

    /**
     * Parses json object into TimeObject
     * @param json
     * @param key
     * @return parsed time object from JSONObject, BiTimeObject if from and to are both set, Single if only one of those params is set, null otherwise.
     */
    private static TimeObject timeObjectFromJson(JSONObject json, String key) {
        if (json == null){
            return null;
        }

        // try if time is set to false, if it is return null, if not ignore and continue
        try {
            if (json.getString(key).equals("false") || json.getString(key).contains("1970")){
                return null;
            }
        } catch (JSONException ignored) {}

        try {
            JSONObject timeObject = json.getJSONObject(key);
            String from = null;
            String to = null;
            // parse from param
            try {
                from = timeObject.getString("from");
                if (from.equals("false") || from.contains("1970")){
                    from = null;
                }

            } catch (JSONException ignored) {}
            // parse to param
            try {
                to = timeObject.getString("to");
                if (to.equals("false") || to.contains("1970")){
                    to = null;
                }

            } catch (JSONException ignored) {}

            // check if any of those two params are null or 1970
            if (to != null && from != null){
                return new BiTimeObject(ZonedDateTime.parse(from), ZonedDateTime.parse(to));
            } else if(to == null && from != null) {
                return new SingleTimeObject(ZonedDateTime.parse(from));
            } else if(to != null && from == null) {
                return new SingleTimeObject(ZonedDateTime.parse(to));
            } else {
                return null;
            }

        } catch (JSONException e) {
            try {
                return new SingleTimeObject(ZonedDateTime.parse(json.getString(key)));
            } catch (JSONException ex){
                return null;
            }
        }
    }

}
