package cvut.fel.felsight.remote.messaging.message.handler;

import cvut.fel.felsight.remote.messaging.message.Message;
import cvut.fel.felsight.remote.messaging.targets.*;

public interface MessageHandler<M extends Message> {

    void handleMessage(M message, CourseMessageTarget target) throws HandleMessageException;

    void handleMessage(M message, RoleMessageTarget target) throws HandleMessageException;

    void handleMessage(M message, StudentsMessageTarget target) throws HandleMessageException;

    void handleMessage(M message, TeachersMessageTarget target) throws HandleMessageException;

    void handleMessage(M message, UsernameMessageTarget target) throws HandleMessageException;

    void handleMessage(M message, ParallelTeacherMessageTarget parallelTeacherMessageTarget) throws HandleMessageException;

    void handleMessage(M message, ParallelStudentMessageTarget parallelStudentMessageTarget) throws HandleMessageException;
}
