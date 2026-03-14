package edu.eci.labinfo.labtodo.unit.service;

import edu.eci.labinfo.labtodo.data.TaskRepository;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.TaskService;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import edu.eci.labinfo.labtodo.support.TestDataBuilders;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskServiceTest extends BaseUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService subject;

    @Test
    void shouldAddTaskWhenTaskIsValid() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        when(taskRepository.save(task)).thenReturn(task);

        // Act
        Task result = subject.addTask(task);

        // Assert
        assertThat(result).isSameAs(task);
        verify(taskRepository).save(task);
    }

    @Test
    void shouldGetTaskWhenIdExists() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act
        Task result = subject.getTask(1L);

        // Assert
        assertThat(result.getTaskId()).isEqualTo(1L);
    }

    @Test
    void shouldNotGetTaskWhenIdDoesNotExist() {
        // Arrange
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> subject.getTask(99L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldGetTasksByUserAndStatusAndSemester() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        Semester semester = TestDataBuilders.buildActiveSemester();
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByUsersUserIdAndStatusAndSemesterPeriodId(
                user.getUserId(), Status.PENDING.getValue(), semester.getPeriodId()
        )).thenReturn(expected);

        // Act
        List<Task> result = subject.getTaskByUserAndStatusAndSemester(user, Status.PENDING.getValue(), semester);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTasksByUser() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByUsersUserId(user.getUserId())).thenReturn(expected);

        // Act
        List<Task> result = subject.getTasksByUser(user);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTasksByUserAndStatus() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByUserIdAndStatus(user.getUserId(), Status.PENDING.getValue())).thenReturn(expected);

        // Act
        List<Task> result = subject.getTasksByUserAndStatus(user, Status.PENDING.getValue());

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTaskByTypeAndStatus() {
        // Arrange
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByTypeAndStatus(TypeTask.LABORATORIO.getValue(), Status.PENDING.getValue())).thenReturn(expected);

        // Act
        List<Task> result = subject.getTaskByTypeAndStatus(TypeTask.LABORATORIO.getValue(), Status.PENDING.getValue());

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTasksByStatus() {
        // Arrange
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByStatus(Status.PENDING.getValue())).thenReturn(expected);

        // Act
        List<Task> result = subject.getTasksByStatus(Status.PENDING.getValue());

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTaskByType() {
        // Arrange
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByTypeTask(TypeTask.LABORATORIO.getValue())).thenReturn(expected);

        // Act
        List<Task> result = subject.getTaskByType(TypeTask.LABORATORIO.getValue());

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTasksByTypeAndStatusAndSemester() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByTypeTaskAndStatusAndSemesterPeriodId(
                TypeTask.LABORATORIO.getValue(), Status.PENDING.getValue(), semester.getPeriodId()
        )).thenReturn(expected);

        // Act
        List<Task> result = subject.getTasksByTypeAndStatusAndSemester(
                TypeTask.LABORATORIO.getValue(), Status.PENDING.getValue(), semester
        );

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTasksBySemester() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findBySemesterPeriodId(semester.getPeriodId())).thenReturn(expected);

        // Act
        List<Task> result = subject.getTasksBySemester(semester);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTasksByTypeAndSemester() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByTypeTaskAndSemesterPeriodId(TypeTask.LABORATORIO.getValue(), semester.getPeriodId()))
                .thenReturn(expected);

        // Act
        List<Task> result = subject.getTasksByTypeAndSemester(TypeTask.LABORATORIO.getValue(), semester);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetAllTasks() {
        // Arrange
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findAll()).thenReturn(expected);

        // Act
        List<Task> result = subject.getAllTask();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetNonFinishedAdminTasks() {
        // Arrange
        List<Task> expected = List.of(TestDataBuilders.buildPendingLabTask());
        when(taskRepository.findByTypeTaskAndStatusNot(
                TypeTask.ADMINISTRADOR.getValue(),
                Status.FINISH.getValue()
        )).thenReturn(expected);

        // Act
        List<Task> result = subject.getNonFinishedAdminTasks();

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldUpdateTaskWhenTaskExists() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(10L);
        when(taskRepository.existsById(10L)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        // Act
        Task result = subject.updateTask(task);

        // Assert
        assertThat(result).isSameAs(task);
    }

    @Test
    void shouldNotUpdateTaskWhenTaskDoesNotExist() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(55L);
        when(taskRepository.existsById(55L)).thenReturn(false);

        // Act
        Task result = subject.updateTask(task);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldDeleteTaskById() {
        // Arrange
        Long taskId = 40L;

        // Act
        subject.deleteTask(taskId);

        // Assert
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void shouldDeleteAllTasks() {
        // Arrange

        // Act
        subject.deleteAllTasks();

        // Assert
        verify(taskRepository).deleteAll();
    }

    @Test
    void shouldGetUsersWhoCommentedTask() {
        // Arrange
        List<User> expectedUsers = List.of(TestDataBuilders.buildMonitorUser());
        when(taskRepository.findUsersWhoCommented(1L)).thenReturn(expectedUsers);

        // Act
        List<User> result = subject.getUsersWhoCommentedTask(1L);

        // Assert
        assertThat(result).hasSize(1);
    }

}
