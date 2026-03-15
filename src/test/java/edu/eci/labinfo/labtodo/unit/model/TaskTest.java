package edu.eci.labinfo.labtodo.unit.model;

import edu.eci.labinfo.labtodo.model.Comment;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.model.TopicTask;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest extends BaseUnitTest {

	@Test
	void shouldCreateTaskWithDefaultStatusWhenUsingEmptyConstructor() {
		// Arrange

		// Act
		Task task = new Task();

		// Assert
		assertThat(task.getStatus()).isEqualTo("Por Hacer");
		assertThat(task.getUsers()).isNotNull();
		assertThat(task.getComments()).isNotNull();
	}

	@Test
	void shouldAddUserToTask() {
		// Arrange
		Task task = new Task();
		User user = new User();
		user.setFullName("User One");

		// Act
		task.addUser(user);

		// Assert
		assertThat(task.getUsers()).contains(user);
	}

	@Test
	void shouldAddCommentToTask() {
		// Arrange
		Task task = new Task();
		Comment comment = new Comment(task, "First comment");

		// Act
		task.addComment(comment);

		// Assert
		assertThat(task.getComments()).contains(comment);
	}

	@Test
	void shouldReturnAllUsersTextWhenUsersExist() {
		// Arrange
		Task task = new Task();
		User userOne = new User();
		userOne.setFullName("Alice");
		User userTwo = new User();
		userTwo.setFullName("Bob");
		task.addUser(userOne);
		task.addUser(userTwo);

		// Act
		String allUsers = task.getAllUsers();

		// Assert
		assertThat(allUsers).contains("Alice");
		assertThat(allUsers).contains("Bob");
	}

	@Test
	void shouldReturnFormattedDateTextWhenCreationDateExists() {
		// Arrange
		Task task = new Task();

		// Act
		String dateText = task.getDateText();

		// Assert
		assertThat(dateText).isNotBlank();
	}

	@Test
	void shouldCompareTaskByEqualsAndHashCodeWhenSameData() {
		// Arrange
		Task left = new Task("T1", "D1", TypeTask.LABORATORIO, TopicTask.REDES);
		left.setTaskId(1L);
		left.setCreationDate(LocalDate.of(2026, 1, 1));
		left.setUsers(new ArrayList<>());
		left.setComments(new ArrayList<>());
		Semester semester = new Semester();
		semester.setPeriodId(10L);
		left.setSemester(semester);

		Task right = new Task("T1", "D1", TypeTask.LABORATORIO, TopicTask.REDES);
		right.setTaskId(1L);
		right.setCreationDate(LocalDate.of(2026, 1, 1));
		right.setUsers(new ArrayList<>());
		right.setComments(new ArrayList<>());
		Semester semester2 = new Semester();
		semester2.setPeriodId(10L);
		right.setSemester(semester2);

		// Act
		boolean equalsResult = left.equals(right);
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();

		// Assert
		assertThat(equalsResult).isTrue();
		assertThat(leftHash).isEqualTo(rightHash);
	}

	@Test
	void shouldGenerateToStringForTask() {
		// Arrange
		Task task = new Task("T2", "D2", TypeTask.MONITOR, TopicTask.MANTENIMIENTO);

		// Act
		String result = task.toString();

		// Assert
		assertThat(result).contains("Task");
	}

	@Test
	void shouldSetBasicFieldsWithSetters() {
		// Arrange
		Task task = new Task();

		// Act
		task.setTitle("Title");
		task.setDescription("Desc");
		task.setTopicTask(TopicTask.MANTENIMIENTO.getValue());
		task.setTypeTask(TypeTask.MONITOR.getValue());

		// Assert
		assertThat(task.getTitle()).isEqualTo("Title");
		assertThat(task.getDescription()).isEqualTo("Desc");
		assertThat(task.getTopicTask()).isEqualTo(TopicTask.MANTENIMIENTO.getValue());
		assertThat(task.getTypeTask()).isEqualTo(TypeTask.MONITOR.getValue());
	}

	@Test
	void shouldNotBeEqualWhenIdsDiffer() {
		// Arrange
		Task left = new Task();
		left.setTaskId(1L);
		Task right = new Task();
		right.setTaskId(2L);

		// Act & Assert
		assertThat(left).isNotEqualTo(right);
		assertThat(left.hashCode()).isNotEqualTo(right.hashCode());
	}

	@Test
	void shouldNotBeEqualToDifferentType() {
		// Arrange
		Task task = new Task();

		// Act & Assert
		assertThat(task.equals("not-a-task")).isFalse();
	}
}
