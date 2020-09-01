package cvut.fel.felsight.remote.messaging.targets;

import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.message.handler.MessageHandler;

import java.util.Objects;

public class ParallelTeacherMessageTarget implements MessageTarget {
    private final String semester;
    private final String course;
    private final String parallel;

    public ParallelTeacherMessageTarget(String semester, String course, String parallel) {
        this.semester = semester;
        this.course = course;
        this.parallel = parallel;
    }

    public String getParallel() {
        return parallel;
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
        ParallelTeacherMessageTarget that = (ParallelTeacherMessageTarget) o;
        return parallel.equals(that.parallel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parallel);
    }

    @Override
    public <M extends Message> void accept(M message, MessageHandler<M> handler) throws HandleMessageException {
        handler.handleMessage(message, this);
    }

    @Override
    public String toString() {
        return "ParallelMessageTarget{" +
                "semester='" + semester + '\'' +
                ", course='" + course + '\'' +
                ", parallel='" + parallel + '\'' +
                '}';
    }
}
