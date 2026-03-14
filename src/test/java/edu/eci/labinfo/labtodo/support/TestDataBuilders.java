package edu.eci.labinfo.labtodo.support;

import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.Comment;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TopicTask;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Static factory methods for building domain objects inside tests.
 *
 * Why this class exists:
 *   AAA — keeps the Arrange section concise and intention-revealing.
 *   Independent — every method returns a new, fully-initialised object.
 *                 Tests never share a mutable instance produced here.
 */
public final class TestDataBuilders {

    private TestDataBuilders() {
        // Utility class — no instantiation.
    }

    public static Task buildPendingLabTask() {
        return new Task(
                "Sample lab task",
                "Sample lab task description",
                TypeTask.LABORATORIO,
                TopicTask.MANTENIMIENTO
        );
    }

    public static Task buildPendingLabTask(String title, String description) {
        return new Task(title, description, TypeTask.LABORATORIO, TopicTask.MANTENIMIENTO);
    }

    public static Task buildInProcessLabTask() {
        Task task = buildPendingLabTask();
        task.setStatus(Status.INPROCESS.getValue());
        return task;
    }

    public static User buildMonitorUser() {
        User user = new User();
        user.setUserId(100L);
        user.setFullName("Test Monitor");
        user.setUserName("test.monitor");
        user.setRole(Role.MONITOR.getValue());
        user.setAccountType(AccountType.ACTIVO.getValue());
        user.setPassword("plain_password");
        user.setCreationDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        user.setUpdateDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return user;
    }

    public static User buildAdminUser() {
        User user = new User();
        user.setUserId(101L);
        user.setFullName("Test Admin");
        user.setUserName("test.admin");
        user.setRole(Role.ADMINISTRADOR.getValue());
        user.setAccountType(AccountType.ACTIVO.getValue());
        user.setPassword("plain_password");
        user.setCreationDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        user.setUpdateDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return user;
    }

    public static Semester buildActiveSemester() {
        Semester semester = new Semester();
        semester.setPeriodId(10L);
        semester.setSemesterName("2026-1");
        semester.setStartDate(LocalDate.of(2026, 2, 1));
        semester.setEndDate(LocalDate.of(2026, 6, 30));
        return semester;
    }

    public static Comment buildCommentForTask(Task task, User author) {
        Comment comment = new Comment(task, "Test comment description");
        comment.setCommentId(900L);
        comment.setCreatorUser(author);
        return comment;
    }
}
