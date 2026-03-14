package edu.eci.labinfo.labtodo.unit.service;

import edu.eci.labinfo.labtodo.data.UserRepository;
import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.LabToDoExeption;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.TaskService;
import edu.eci.labinfo.labtodo.service.UserService;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import edu.eci.labinfo.labtodo.support.TestDataBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private UserService subject;

    @Test
    void shouldAddUserWhenUserNameDoesNotExist() throws LabToDoExeption {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        user.setPassword("raw-pass");
        when(userRepository.existsByUserName(user.getUserName())).thenReturn(false);
        when(passwordEncoder.encode("raw-pass")).thenReturn("encoded-pass");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User result = subject.addUser(user);

        // Assert
        assertThat(result.getPassword()).isEqualTo("encoded-pass");
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getUpdateDate()).isNotNull();
        assertThat(result.getLastLoginDate()).isNotNull();
    }

    @Test
    void shouldNotAddUserWhenUserNameAlreadyExists() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        when(userRepository.existsByUserName(user.getUserName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> subject.addUser(user))
                .isInstanceOf(LabToDoExeption.class)
                .hasMessageContaining(LabToDoExeption.USER_NAME_EXISTS);
    }

    @Test
    void shouldGetUserByUserNameWhenExists() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        when(userRepository.findByUserName("test.monitor")).thenReturn(Optional.of(user));

        // Act
        User result = subject.getUserByUserName("test.monitor");

        // Assert
        assertThat(result).isSameAs(user);
    }

    @Test
    void shouldReturnNullWhenUserByUserNameDoesNotExist() {
        // Arrange
        when(userRepository.findByUserName("missing.user")).thenReturn(Optional.empty());

        // Act
        User result = subject.getUserByUserName("missing.user");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldGetUserByFullNameWhenExists() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        when(userRepository.findByFullName("Test Monitor")).thenReturn(Optional.of(user));

        // Act
        User result = subject.getUserByFullName("Test Monitor");

        // Assert
        assertThat(result).isSameAs(user);
    }

    @Test
    void shouldReturnNullWhenUserByFullNameDoesNotExist() {
        // Arrange
        when(userRepository.findByFullName("Missing User")).thenReturn(Optional.empty());

        // Act
        User result = subject.getUserByFullName("Missing User");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldGetAllUsers() {
        // Arrange
        List<User> expected = List.of(TestDataBuilders.buildMonitorUser());
        when(userRepository.findAll()).thenReturn(expected);

        // Act
        List<User> result = subject.getUsers();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetUsersByRole() {
        // Arrange
        List<User> expected = List.of(TestDataBuilders.buildAdminUser());
        when(userRepository.findByRole(Role.ADMINISTRADOR.getValue())).thenReturn(expected);

        // Act
        List<User> result = subject.getUsersByRole(Role.ADMINISTRADOR.getValue());

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetActiveUsersByRole() {
        // Arrange
        List<User> expected = List.of(TestDataBuilders.buildAdminUser());
        when(userRepository.findByRoleAndAccountTypeNotIn(any(), any())).thenReturn(expected);

        // Act
        List<User> result = subject.getActiveUsersByRole(Role.ADMINISTRADOR.getValue());

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldUpdateUserAndAssignAdminTasksWhenRoleChangesToAdmin() {
        // Arrange
        User currentUser = TestDataBuilders.buildMonitorUser();
        currentUser.setUserId(200L);
        currentUser.setRole(Role.MONITOR.getValue());
        currentUser.setAccountType(AccountType.ACTIVO.getValue());

        User updatedCandidate = TestDataBuilders.buildAdminUser();
        updatedCandidate.setUserId(200L);
        updatedCandidate.setRole(Role.ADMINISTRADOR.getValue());
        updatedCandidate.setAccountType(AccountType.ACTIVO.getValue());

        Task adminTask = new Task("Admin task", "desc", TypeTask.ADMINISTRADOR, edu.eci.labinfo.labtodo.model.TopicTask.MANTENIMIENTO);
        adminTask.setTaskId(301L);
        adminTask.setStatus(Status.PENDING.getValue());
        adminTask.setUsers(new ArrayList<>());

        when(userRepository.existsById(200L)).thenReturn(true);
        when(userRepository.findById(200L)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(updatedCandidate)).thenReturn(updatedCandidate);
        when(taskService.getNonFinishedAdminTasks()).thenReturn(List.of(adminTask));
        when(taskService.updateTask(adminTask)).thenReturn(adminTask);

        // Act
        User result = subject.updateUser(updatedCandidate);

        // Assert
        assertThat(result).isSameAs(updatedCandidate);
        assertThat(adminTask.getUsers()).contains(updatedCandidate);
        verify(taskService).updateTask(adminTask);
    }

    @Test
    void shouldNotAssignAdminTaskWhenUserAlreadyAssigned() {
        // Arrange
        User currentUser = TestDataBuilders.buildMonitorUser();
        currentUser.setUserId(300L);

        User updatedCandidate = TestDataBuilders.buildAdminUser();
        updatedCandidate.setUserId(300L);

        Task adminTask = new Task("Admin task", "desc", TypeTask.ADMINISTRADOR, edu.eci.labinfo.labtodo.model.TopicTask.MANTENIMIENTO);
        adminTask.setTaskId(302L);
        adminTask.setUsers(new ArrayList<>(List.of(updatedCandidate)));

        when(userRepository.existsById(300L)).thenReturn(true);
        when(userRepository.findById(300L)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(updatedCandidate)).thenReturn(updatedCandidate);
        when(taskService.getNonFinishedAdminTasks()).thenReturn(List.of(adminTask));

        // Act
        User result = subject.updateUser(updatedCandidate);

        // Assert
        assertThat(result).isSameAs(updatedCandidate);
        verify(taskService, never()).updateTask(adminTask);
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistingUser() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        user.setUserId(444L);
        when(userRepository.existsById(444L)).thenReturn(false);

        // Act
        User result = subject.updateUser(user);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldDeleteUserByUserName() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));

        // Act
        subject.deleteUser(user.getUserName());

        // Assert
        verify(userRepository).delete(user);
    }

    @Test
    void shouldDeleteAllUsers() {
        // Arrange

        // Act
        subject.deleteAllUsers();

        // Assert
        verify(userRepository).deleteAll();
    }

}
