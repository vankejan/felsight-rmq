package cvut.fel.felsight.remote.messaging.targets;

import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;

import java.util.Objects;

/**
 * Sends a message to the students of a course.
 */
public class StudentsMessageTarget implements MessageTarget {

    private final String semester;
    private final String course;

    public StudentsMessageTarget(String semester, String course) {
        this.semester = semester;
        this.course = course;
    }

    public String getSemester() {
        return semester;
    }

    public String getCourse() {
        return course;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentsMessageTarget that = (StudentsMessageTarget) o;
        return semester.equals(that.semester) && course.equals(that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(semester, course);
    }

    @Override
    public <M extends Message> void accept(M message, MessageHandler<M> handler) throws HandleMessageException {
        handler.handleMessage(message, this);
    }

    @Override
    public String toString() {
        return "StudentsMessageTarget{" +
                "semester='" + semester + '\'' +
                ", course='" + course + '\'' +
                '}';
    }
}
