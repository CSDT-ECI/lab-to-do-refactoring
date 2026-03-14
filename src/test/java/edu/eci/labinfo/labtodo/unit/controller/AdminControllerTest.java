package edu.eci.labinfo.labtodo.unit.controller;

import edu.eci.labinfo.labtodo.controller.AdminController;
import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.PrimeFacesWrapper;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerTest extends BaseUnitTest {

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private PrimeFacesWrapper primeFacesWrapper;

    @InjectMocks
    private AdminController subject;

    @Test
    void shouldNotModifyStateWhenNewStateIsEmpty() {
        // Arrange
        subject.setNewState("");
        subject.setSelectedTasks(List.of(TestDataBuilders.buildPendingLabTask()));
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.modifyStateTaks();

            // Assert
            assertThat(result).isFalse();
            verify(taskService, org.mockito.Mockito.never()).updateTask(any());
        }
    }

    @Test
    void shouldModifySelectedTaskState() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(99L);
        task.setTypeTask(TypeTask.MONITOR.getValue());
        subject.setSelectedTasks(new java.util.ArrayList<>(List.of(task)));
        subject.setNewState(Status.INPROCESS.getValue());
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.modifyStateTaks();

            // Assert
            assertThat(result).isTrue();
            assertThat(task.getStatus()).isEqualTo(Status.INPROCESS.getValue());
            verify(taskService).updateTask(task);
        }
    }

    @Test
    void shouldModifyLabTaskToFinishAndAssignCommentUsers() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(10L);
        task.setTypeTask(TypeTask.LABORATORIO.getValue());
        subject.setSelectedTasks(new java.util.ArrayList<>(List.of(task)));
        subject.setNewState(Status.FINISH.getValue());
        List<User> commenters = List.of(TestDataBuilders.buildMonitorUser());
        when(taskService.getUsersWhoCommentedTask(10L)).thenReturn(commenters);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.modifyStateTaks();
        }

        // Assert
        assertThat(task.getStatus()).isEqualTo(Status.FINISH.getValue());
        assertThat(task.getUsers()).isEqualTo(commenters);
    }

    @Test
    void shouldModifySelectedUsersRole() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        subject.setSelectedUsers(new java.util.ArrayList<>(List.of(user)));
        subject.setNewRole("Administrador");
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.modifyUserRole();

            // Assert
            assertThat(result).isTrue();
            verify(userService).updateUser(user);
        }
    }

    @Test
    void shouldReturnDefaultUpdateButtonMessageWhenNoTasksSelected() {
        // Arrange
        subject.setSelectedTasks(null);

        // Act
        String result = subject.getUpdateButtonMessage();

        // Assert
        assertThat(result).isEqualTo("Cambiar estado de");
    }

    @Test
    void shouldReturnPluralUpdateButtonMessageWhenMultipleTasksSelected() {
        // Arrange
        List<Task> tasks = List.of(new Task(), new Task());
        subject.setSelectedTasks(tasks);

        // Act
        String result = subject.getUpdateButtonMessage();

        // Assert
        assertThat(result).contains("2 tareas seleccionadas");
    }

    @Test
    void shouldReturnTrueWhenSelectedTasksExists() {
        // Arrange
        subject.setSelectedTasks(List.of(new Task()));

        // Act
        boolean result = subject.hasSelectedTasks();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSelectedTasksDoesNotExist() {
        // Arrange
        subject.setSelectedTasks(List.of());

        // Act
        boolean result = subject.hasSelectedTasks();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnRoleButtonMessageWhenSingleUserSelected() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        subject.setSelectedUsers(List.of(user));

        // Act
        String result = subject.getRoleButtonMessage();

        // Assert
        assertThat(result).contains("1 usuario seleccionado");
    }

    @Test
    void shouldReturnAccountButtonMessageWhenMultipleUsersSelected() {
        // Arrange
        User u1 = TestDataBuilders.buildMonitorUser();
        User u2 = TestDataBuilders.buildAdminUser();
        subject.setSelectedUsers(List.of(u1, u2));

        // Act
        String result = subject.getaccountButtonMessage();

        // Assert
        assertThat(result).contains("2 usuarios seleccionados");
    }

    @Test
    void shouldReturnDeleteButtonMessageWhenNoUsersSelected() {
        // Arrange
        subject.setSelectedUsers(null);

        // Act
        String result = subject.getDeleteButtonMessage();

        // Assert
        assertThat(result).isEqualTo("Eliminar ");
    }

    @Test
    void shouldReturnTrueWhenSelectedUsersExists() {
        // Arrange
        subject.setSelectedUsers(List.of(TestDataBuilders.buildAdminUser()));

        // Act
        boolean result = subject.hasSelectedUsers();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldNotModifyUserRoleWhenRoleIsEmpty() {
        // Arrange
        subject.setSelectedUsers(List.of(TestDataBuilders.buildMonitorUser()));
        subject.setNewRole("");
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.modifyUserRole();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldNotModifyAccountTypeWhenNewAccountTypeIsEmpty() {
        // Arrange
        subject.setSelectedUsers(List.of(TestDataBuilders.buildMonitorUser()));
        subject.setNewAccountType("");
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.modifyUserAccountType();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldModifyUsersAccountTypeWhenTransitionIsValid() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue());
        subject.setSelectedUsers(new java.util.ArrayList<>(List.of(user)));
        subject.setNewAccountType(AccountType.ACEPTADO.getValue());
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.modifyUserAccountType();

            // Assert
            assertThat(result).isTrue();
            assertThat(user.getAccountType()).isEqualTo(AccountType.ACEPTADO.getValue());
            verify(userService).updateUser(user);
        }
    }

    @Test
    void shouldSkipUserWhenAcceptingPasswordChangeWithoutRequestState() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACTIVO.getValue());
        subject.setSelectedUsers(new java.util.ArrayList<>(List.of(user)));
        subject.setNewAccountType(AccountType.ACEPTADO.getValue());
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.modifyUserAccountType();
        }

        // Assert
        verify(userService, never()).updateUser(any());
    }

    @Test
    void shouldDeleteAllSelectedUsersSuccessfully() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        subject.setSelectedUsers(new java.util.ArrayList<>(List.of(user)));
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.deleteUsers();

            // Assert
            assertThat(result).isTrue();
            verify(userService).deleteUser(user.getUserName());
        }
    }

    @Test
    void shouldContinueDeletingWhenOneUserFails() {
        // Arrange
        User u1 = TestDataBuilders.buildMonitorUser();
        User u2 = TestDataBuilders.buildAdminUser();
        subject.setSelectedUsers(new java.util.ArrayList<>(List.of(u1, u2)));
        doThrow(new RuntimeException("fk constraint")).when(userService).deleteUser(u1.getUserName());
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.deleteUsers();

            // Assert
            assertThat(result).isTrue();
            verify(userService).deleteUser(u1.getUserName());
            verify(userService).deleteUser(u2.getUserName());
        }
    }

    @Test
    void shouldHandleDeleteUsersWhenSelectedUsersIsNull() {
        // Arrange
        subject.setSelectedUsers(null);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Boolean result = subject.deleteUsers();

            // Assert
            assertThat(result).isTrue();
            verify(userService, never()).deleteUser(any());
            verify(primeFacesWrapper, times(2)).current();
        }
    }

    @Test
    void shouldReturnSingleTaskUpdateMessageWhenOneTaskSelected() {
        // Arrange
        subject.setSelectedTasks(List.of(new Task()));

        // Act
        String result = subject.getUpdateButtonMessage();

        // Assert
        assertThat(result).contains("1 tarea seleccionada");
    }

    @Test
    void shouldExposeMutableStateThroughDataAccessors() {
        // Arrange
        List<Task> tasks = List.of(TestDataBuilders.buildPendingLabTask());
        List<User> users = List.of(TestDataBuilders.buildMonitorUser());

        // Act
        subject.setSelectedTasks(tasks);
        subject.setSelectedUsers(users);
        subject.setNewState(Status.PENDING.getValue());
        subject.setNewRole("Monitor");
        subject.setNewAccountType(AccountType.ACTIVO.getValue());
        int hash = subject.hashCode();
        String text = subject.toString();

        // Assert
        assertThat(subject.getSelectedTasks()).isEqualTo(tasks);
        assertThat(subject.getSelectedUsers()).isEqualTo(users);
        assertThat(subject.getNewState()).isEqualTo(Status.PENDING.getValue());
        assertThat(subject.getNewRole()).isEqualTo("Monitor");
        assertThat(subject.getNewAccountType()).isEqualTo(AccountType.ACTIVO.getValue());
        assertThat(hash).isNotZero();
        assertThat(text).contains("AdminController");
        assertThat(subject).isEqualTo(subject);
    }

}
