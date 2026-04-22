package edu.eci.labinfo.labtodo.unit.controller;

import edu.eci.labinfo.labtodo.controller.LoginController;
import edu.eci.labinfo.labtodo.controller.TaskController;
import edu.eci.labinfo.labtodo.model.Comment;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.CommentService;
import edu.eci.labinfo.labtodo.service.PrimeFacesWrapper;
import edu.eci.labinfo.labtodo.service.SemesterService;
import edu.eci.labinfo.labtodo.service.TaskService;
import edu.eci.labinfo.labtodo.service.UserService;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import edu.eci.labinfo.labtodo.support.TestDataBuilders;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskControllerTest extends BaseUnitTest {

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private CommentService commentService;

    @Mock
    private SemesterService semesterService;

    @Mock
    private PrimeFacesWrapper primeFacesWrapper;

    @Mock
    private LoginController loginController;

    @InjectMocks
    private TaskController subject;

    @Test
    void shouldReturnAdminTaskTypeWhenUserIsAdmin() {
        // Arrange
        when(loginController.getUserName()).thenReturn("test.admin");
        when(loginController.isAdmin("test.admin")).thenReturn(true);

        // Act
        List<String> result = subject.getAvailableTaskTypes();

        // Assert
        assertThat(result).contains("Administradores");
    }

    @Test
    void shouldNotReturnAdminTaskTypeWhenUserIsNotAdmin() {
        // Arrange
        when(loginController.getUserName()).thenReturn("test.monitor");
        when(loginController.isAdmin("test.monitor")).thenReturn(false);

        // Act
        List<String> result = subject.getAvailableTaskTypes();

        // Assert
        assertThat(result).doesNotContain("Administradores");
        assertThat(result).containsExactly(TypeTask.MONITOR.getValue(), TypeTask.LABORATORIO.getValue());
    }

    @Test
    void shouldReturnIniciarMessageWhenTaskIsPending() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.PENDING.getValue());

        // Act
        String result = subject.getMessageToTaskButton(task);

        // Assert
        assertThat(result).isEqualTo("Iniciar");
    }

    @Test
    void shouldReturnARevisionMessageWhenTaskIsInProcess() {
        // Arrange
        Task task = TestDataBuilders.buildInProcessLabTask();

        // Act
        String result = subject.getMessageToTaskButton(task);

        // Assert
        assertThat(result).isEqualTo("A revisión");
    }

    @Test
    void shouldReturnCompletarMessageWhenTaskIsReview() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.REVIEW.getValue());

        // Act
        String result = subject.getMessageToTaskButton(task);

        // Assert
        assertThat(result).isEqualTo("Completar");
    }

    @Test
    void shouldNotRenderTaskButtonWhenTaskIsFinished() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.FINISH.getValue());
        User user = TestDataBuilders.buildMonitorUser();
        when(userService.getUserByUserName("monitor")).thenReturn(user);

        // Act
        Boolean result = subject.getRenderedToTaskButton("monitor", task);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldNotRenderTaskButtonWhenTaskIsReviewAndUserIsMonitor() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.REVIEW.getValue());
        User user = TestDataBuilders.buildMonitorUser();
        user.setRole(Role.MONITOR.getValue());
        when(userService.getUserByUserName("monitor")).thenReturn(user);

        // Act
        Boolean result = subject.getRenderedToTaskButton("monitor", task);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldRenderTaskButtonWhenTaskIsReviewAndUserIsAdmin() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.REVIEW.getValue());
        User user = TestDataBuilders.buildAdminUser();
        user.setRole(Role.ADMINISTRADOR.getValue());
        when(userService.getUserByUserName("admin")).thenReturn(user);

        // Act
        Boolean result = subject.getRenderedToTaskButton("admin", task);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetCurrentTaskComments() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        subject.setCurrentTask(task);
        List<Comment> comments = List.of(new Comment(task, "c1"));
        when(commentService.getComentsByTask(task)).thenReturn(comments);

        // Act
        List<Comment> result = subject.getCurrentTaskComments();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldOpenNewAndResetCurrentTaskAndSelectedUsers() {
        // Arrange
        subject.setSelectedUsers(new ArrayList<>(List.of("User A")));

        // Act
        subject.openNew();

        // Assert
        assertThat(subject.getCurrentTask()).isNotNull();
        assertThat(subject.getSelectedUsers()).isEmpty();
    }

    @Test
    void shouldOpenCommentAndCreateNewCommentInstance() {
        // Arrange

        // Act
        subject.openComment();

        // Assert
        assertThat(subject.getComment()).isNotNull();
    }

    @Test
    void shouldLoadTasksOnDatabaseLoadedWhenCurrentSemesterExists() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        Semester semester = TestDataBuilders.buildActiveSemester();
        List<Task> userTasks = List.of(TestDataBuilders.buildPendingLabTask());
        List<Task> labTasks = List.of(TestDataBuilders.buildPendingLabTask());

        subject.setStatus(Status.PENDING.getValue());
        when(userService.getUserByUserName("monitor")).thenReturn(user);
        when(semesterService.getCurrentSemester()).thenReturn(semester);
        when(taskService.getTaskByUserAndStatusAndSemester(user, Status.PENDING.getValue(), semester))
                .thenReturn(userTasks);
        when(taskService.getTasksByTypeAndStatusAndSemester(TypeTask.LABORATORIO.getValue(), Status.PENDING.getValue(),
                semester))
                .thenReturn(labTasks);

        // Act
        subject.onDatabaseLoaded("monitor");

        // Assert
        assertThat(subject.getTasks()).hasSize(1);
        assertThat(subject.getTasksLab()).hasSize(1);
    }

    @Test
    void shouldLoadControlTasksUsingSelectedSemesterWhenProvided() {
        // Arrange
        Semester selectedSemester = TestDataBuilders.buildActiveSemester();
        List<Task> semesterTasks = List.of(TestDataBuilders.buildPendingLabTask());
        List<Task> semesterLabTasks = List.of(TestDataBuilders.buildPendingLabTask());
        subject.setSelectedSemester("2026-1");

        when(semesterService.getCurrentSemester()).thenReturn(null);
        when(semesterService.getSemesterByName("2026-1")).thenReturn(selectedSemester);
        when(taskService.getTasksBySemester(selectedSemester)).thenReturn(semesterTasks);
        when(taskService.getTasksByTypeAndSemester(TypeTask.LABORATORIO.getValue(), selectedSemester))
                .thenReturn(semesterLabTasks);

        // Act
        subject.onControlLoaded();

        // Assert
        assertThat(subject.getTasks()).hasSize(1);
        assertThat(subject.getTasksLab()).hasSize(1);
    }

    @Test
    void shouldLoadControlTasksUsingCurrentSemesterWhenNoSelectedSemester() {
        // Arrange
        Semester currentSemester = TestDataBuilders.buildActiveSemester();
        List<Task> semesterTasks = List.of(TestDataBuilders.buildPendingLabTask());
        List<Task> semesterLabTasks = List.of(TestDataBuilders.buildPendingLabTask());
        subject.setSelectedSemester(null);

        when(semesterService.getCurrentSemester()).thenReturn(currentSemester);
        when(taskService.getTasksBySemester(currentSemester)).thenReturn(semesterTasks);
        when(taskService.getTasksByTypeAndSemester(TypeTask.LABORATORIO.getValue(), currentSemester))
                .thenReturn(semesterLabTasks);

        // Act
        subject.onControlLoaded();

        // Assert
        assertThat(subject.getTasks()).hasSize(1);
        assertThat(subject.getTasksLab()).hasSize(1);
    }

    @Test
    void shouldLoadUsersFromCurrentTask() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        user.setFullName("User Full Name");
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setUsers(List.of(user));
        subject.setCurrentTask(task);

        // Act
        subject.loadUsers();

        // Assert
        assertThat(subject.getSelectedUsers()).containsExactly("User Full Name");
    }

    @Test
    void shouldSaveNewAdminTaskForAdminUser() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTypeTask("Administradores");
        task.setTaskId(null);
        subject.setCurrentTask(task);
        subject.setSelectedUsers(new ArrayList<>(List.of("User Full Name")));

        User admin = TestDataBuilders.buildAdminUser();
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(loginController.getUserName()).thenReturn("admin");
        when(loginController.isAdmin("admin")).thenReturn(true);
        when(userService.getActiveUsersByRole(Role.ADMINISTRADOR.getValue())).thenReturn(List.of(admin));
        when(semesterService.getCurrentSemester()).thenReturn(semester);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveTask();
        }

        // Assert
        verify(taskService).addTask(task);
        assertThat(task.getUsers()).contains(admin);
    }

    @Test
    void shouldNotSaveAdminTaskWhenCurrentUserIsNotAdmin() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTypeTask("Administradores");
        subject.setCurrentTask(task);
        when(loginController.getUserName()).thenReturn("monitor");
        when(loginController.isAdmin("monitor")).thenReturn(false);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveTask();
        }

        // Assert
        verify(taskService, org.mockito.Mockito.never()).addTask(any());
        verify(taskService, org.mockito.Mockito.never()).updateTask(any());
    }

    @Test
    void shouldUpdateTaskWhenTaskHasId() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(200L);
        task.setTypeTask(TypeTask.MONITOR.getValue());
        subject.setCurrentTask(task);
        subject.setSelectedUsers(new ArrayList<>(List.of("Assigned User")));

        User assignedUser = TestDataBuilders.buildMonitorUser();
        when(userService.getUserByFullName("Assigned User")).thenReturn(assignedUser);
        when(taskService.updateTask(task)).thenReturn(task);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveTask();
        }

        // Assert
        verify(taskService).updateTask(task);
        assertThat(task.getUsers()).contains(assignedUser);
    }

    @Test
    void shouldCompleteTaskAndMoveToNextState() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.PENDING.getValue());
        task.setTaskId(500L);
        subject.setCurrentTask(task);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.completedMessage();
        }

        // Assert
        assertThat(task.getStatus()).isEqualTo(Status.INPROCESS.getValue());
        verify(taskService).updateTask(task);
    }

    @Test
    void shouldCompleteTaskAndAssignCommentUsersWhenFinishing() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setStatus(Status.REVIEW.getValue());
        task.setTaskId(501L);
        subject.setCurrentTask(task);
        List<User> commenters = List.of(TestDataBuilders.buildMonitorUser());
        when(taskService.getUsersWhoCommentedTask(501L)).thenReturn(commenters);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.completedMessage();
        }

        // Assert
        assertThat(task.getStatus()).isEqualTo(Status.FINISH.getValue());
        assertThat(task.getUsers()).isEqualTo(commenters);
    }

    @Test
    void shouldSaveCommentForCurrentTask() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        User user = TestDataBuilders.buildMonitorUser();
        Comment comment = new Comment();
        subject.setCurrentTask(task);
        subject.setComment(comment);
        subject.setCommentary("A comment");
        when(userService.getUserByUserName("monitor")).thenReturn(user);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);
        FacesContext facesContext = mock(FacesContext.class);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveComment("monitor");
        }

        // Assert
        verify(commentService).addComment(comment);
        assertThat(comment.getDescription()).isEqualTo("A comment");
        assertThat(comment.getTask()).isSameAs(task);
        assertThat(comment.getCreatorUser()).isSameAs(user);
        assertThat(subject.getCommentary()).isEmpty();
    }

    @Test
    void shouldChangeLoggedTaskViewAndUpdateComponents() {
        // Arrange
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        subject.changeLoggedTaskView();

        // Assert
        verify(primeFacesWrapper).current();
    }

    @Test
    void shouldQueryControlBySemesterAndUpdateComponents() {
        // Arrange
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        subject.onControlQuerySemester();

        // Assert
        verify(primeFacesWrapper).current();
    }

    @Test
    void shouldThrowWhenSavingTaskAndSelectedUsersIsNull() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(null);
        task.setTypeTask(TypeTask.MONITOR.getValue());
        subject.setCurrentTask(task);
        subject.setSelectedUsers(null);

        FacesContext facesContext = mock(FacesContext.class);

        // Act / Assert
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            assertThatThrownBy(() -> subject.saveTask()).isInstanceOf(NullPointerException.class);
            verify(taskService, never()).addTask(any());
        }
    }

    @Test
    void shouldSaveTaskIgnoringUnknownUserNames() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(10L);
        task.setTypeTask(TypeTask.MONITOR.getValue());
        subject.setCurrentTask(task);
        subject.setSelectedUsers(new ArrayList<>(List.of("Unknown", "Known")));

        User known = TestDataBuilders.buildMonitorUser();
        when(userService.getUserByFullName("Unknown")).thenReturn(null);
        when(userService.getUserByFullName("Known")).thenReturn(known);
        when(taskService.updateTask(task)).thenReturn(task);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveTask();
        }

        // Assert
        assertThat(task.getUsers()).containsExactly(known);
    }

    @Test
    void shouldNotLoadDatabaseTasksWhenCurrentSemesterIsNull() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        subject.setStatus(Status.PENDING.getValue());
        when(userService.getUserByUserName("monitor")).thenReturn(user);
        when(semesterService.getCurrentSemester()).thenReturn(null);

        // Act
        subject.onDatabaseLoaded("monitor");

        // Assert
        verify(taskService, never()).getTaskByUserAndStatusAndSemester(any(), any(), any());
        verify(taskService, never()).getTasksByTypeAndStatusAndSemester(any(), any(), any());
    }

    @Test
    void shouldNotLoadControlTasksWhenResolvedSemesterIsNull() {
        // Arrange
        subject.setSelectedSemester("2024-0");
        when(semesterService.getCurrentSemester()).thenReturn(null);
        when(semesterService.getSemesterByName("2024-0")).thenReturn(null);

        // Act
        subject.onControlLoaded();

        // Assert
        verify(taskService, never()).getTasksBySemester(any());
        verify(taskService, never()).getTasksByTypeAndSemester(any(), any());
    }

    @Test
    void shouldReturnEmptyMessageWhenTaskIsNull() {
        // Arrange

        // Act
        String result = subject.getMessageToTaskButton(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRenderTaskButtonWhenTaskIsNull() {
        // Arrange

        // Act
        Boolean result = subject.getRenderedToTaskButton("monitor", null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldExposeMutableStateThroughDataAccessors() {
        // Arrange
        List<Task> tasks = new ArrayList<>(List.of(TestDataBuilders.buildPendingLabTask()));
        List<Task> labTasks = new ArrayList<>(List.of(TestDataBuilders.buildPendingLabTask()));
        Task task = TestDataBuilders.buildPendingLabTask();
        Comment controllerComment = new Comment();

        // Act
        subject.setTasks(tasks);
        subject.setTasksLab(labTasks);
        subject.setSelectedUsers(new ArrayList<>(List.of("A", "B")));
        subject.setCurrentTask(task);
        subject.setComment(controllerComment);
        subject.setCommentary("note");
        subject.setStatus(Status.PENDING.getValue());
        subject.setSelectedSemester("2026-1");
        int hash = subject.hashCode();
        String text = subject.toString();

        // Assert
        assertThat(subject.getTasks()).isEqualTo(tasks);
        assertThat(subject.getTasksLab()).isEqualTo(labTasks);
        assertThat(subject.getSelectedUsers()).containsExactly("A", "B");
        assertThat(subject.getCurrentTask()).isEqualTo(task);
        assertThat(subject.getComment()).isEqualTo(controllerComment);
        assertThat(subject.getCommentary()).isEqualTo("note");
        assertThat(subject.getStatus()).isEqualTo(Status.PENDING.getValue());
        assertThat(subject.getSelectedSemester()).isEqualTo("2026-1");
        assertThat(hash).isNotZero();
        assertThat(text).contains("TaskController");
        assertThat(subject).isEqualTo(subject);
    }

    @Test
    void shouldNotBeEqualToDifferentControllerState() {
        // Arrange
        subject.setSelectedUsers(new ArrayList<>(List.of("X")));

        TaskController other = new TaskController(taskService, userService, commentService, semesterService,
                primeFacesWrapper, loginController);

        // Act & Assert
        assertThat(subject).isNotEqualTo(other);
        assertThat(subject.hashCode()).isNotEqualTo(other.hashCode());
    }

}
