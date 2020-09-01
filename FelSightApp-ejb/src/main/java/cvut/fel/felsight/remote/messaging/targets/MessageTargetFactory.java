package cvut.fel.felsight.remote.messaging.targets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTargetFactory {

    private final Pattern usernameMessagePattern =
            Pattern.compile("^notifications\\.user\\.(.+)$");
    private final Pattern roleMessagePattern =
            Pattern.compile("^notifications\\.role\\.(.+)$");
    private final Pattern courseMessagePattern =
            Pattern.compile("^notifications\\.course\\.([a-zA-Z0-9]+)\\.(.+)$");
    private final Pattern onlyStudentsCourseMessagePattern =
            Pattern.compile("^notifications\\.course_students\\.([a-zA-Z0-9]+)\\.(.+)$");
    private final Pattern onlyTeachersCourseMessagePattern =
            Pattern.compile("^notifications\\.course_teachers\\.([a-zA-Z0-9]+)\\.(.+)$");
    private final Pattern parallelStudentMessagePattern =
            Pattern.compile("^notifications\\.course_students\\.([a-zA-Z0-9]+)\\.(.+)\\.([Z0-9]+)$");
    private final Pattern parallelTeacherMessagePattern =
            Pattern.compile("^notifications\\.course_teachers\\.([a-zA-Z0-9]+)\\.(.+)\\.([Z0-9]+)$");

    /**
     * List of available MessageTarget parsers.
     */
    private final List<Function<String, Optional<MessageTarget>>> parsers = new ArrayList<>();

    public MessageTargetFactory() {
        parsers.add(routingKey -> { // UsernameMessageTarget
            Matcher matcher = usernameMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new UsernameMessageTarget(matcher.group(1)))
                                     : Optional.empty();
        });
        parsers.add(routingKey -> { // RoleMessageTarget
            Matcher matcher = roleMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new RoleMessageTarget(matcher.group(1)))
                                     : Optional.empty();
        });
        parsers.add(routingKey -> { // CourseMessageTarget
            Matcher matcher = courseMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new CourseMessageTarget(matcher.group(1), matcher.group(2)))
                                     : Optional.empty();
        });
        parsers.add(routingKey -> { // ParallelStudentMessageTarget
            Matcher matcher = parallelStudentMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new ParallelStudentMessageTarget(matcher.group(1), matcher.group(2), matcher.group(3)))
                    : Optional.empty();
        });
        parsers.add(routingKey -> { // ParallelTeacherMessageTarget
            Matcher matcher = parallelTeacherMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new ParallelTeacherMessageTarget(matcher.group(1), matcher.group(2), matcher.group(3)))
                    : Optional.empty();
        });
        parsers.add(routingKey -> { // StudentsMessageTarget
            Matcher matcher = onlyStudentsCourseMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new StudentsMessageTarget(matcher.group(1), matcher.group(2)))
                                     : Optional.empty();
        });
        parsers.add(routingKey -> { // TeachersMessageTarget
            Matcher matcher = onlyTeachersCourseMessagePattern.matcher(routingKey);
            return matcher.matches() ? Optional.of(new TeachersMessageTarget(matcher.group(1), matcher.group(2)))
                                     : Optional.empty();
        });

    }

    /**
     * Creates a message target based on the given routing key.
     * To introduce a new message target, add a new Function to the {@link #parsers} list.
     *
     * @param routingKey MQ routing key
     * @return created message target
     * @throws MessageTargetFactoryException When the routing key could not be parsed.
     */
    public MessageTarget createMessageTarget(String routingKey) throws MessageTargetFactoryException {
        for (Function<String, Optional<MessageTarget>> parser : parsers) {
            Optional<MessageTarget> maybeMessageTarget = parser.apply(routingKey);
            if (maybeMessageTarget.isPresent()) return maybeMessageTarget.get();
        }
        throw new MessageTargetFactoryException(routingKey);
    }

}
