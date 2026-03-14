package edu.eci.labinfo.labtodo.unit.model;

import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest extends BaseUnitTest {

	@Test
	void shouldAddTaskToUser() {
		// Arrange
		User user = new User();
		user.setTasks(new ArrayList<>());
		Task task = new Task();

		// Act
		user.addTask(task);

		// Assert
		assertThat(user.getTasks()).contains(task);
	}

	@Test
	void shouldConstructUserWithAllArgsConstructor() {
		// Arrange
		List<Task> tasks = new ArrayList<>();

		// Act
		User user = new User(
				5L,
				"John Doe",
				"john",
				"Administrador",
				"Activo",
				"pwd",
				LocalDateTime.of(2026, 1, 1, 10, 0),
				LocalDateTime.of(2026, 1, 1, 10, 0),
				LocalDateTime.of(2026, 1, 1, 10, 0),
				tasks,
				new ArrayList<>()
		);

		// Assert
		assertThat(user.getUserId()).isEqualTo(5L);
		assertThat(user.getUserName()).isEqualTo("john");
		assertThat(user.getTasks()).isSameAs(tasks);
	}

	@Test
	void shouldCompareUserByEqualsAndHashCodeWhenSameData() {
		// Arrange
		User left = new User();
		left.setUserId(1L);
		left.setFullName("Alice");
		left.setUserName("alice");
		left.setRole("Monitor");
		left.setAccountType("Activo");
		left.setPassword("pwd");
		left.setCreationDate(LocalDateTime.of(2026, 1, 1, 1, 1));
		left.setUpdateDate(LocalDateTime.of(2026, 1, 1, 1, 1));
		left.setLastLoginDate(LocalDateTime.of(2026, 1, 1, 1, 1));
		left.setTasks(new ArrayList<>());
		left.setComments(new ArrayList<>());

		User right = new User();
		right.setUserId(1L);
		right.setFullName("Alice");
		right.setUserName("alice");
		right.setRole("Monitor");
		right.setAccountType("Activo");
		right.setPassword("pwd");
		right.setCreationDate(LocalDateTime.of(2026, 1, 1, 1, 1));
		right.setUpdateDate(LocalDateTime.of(2026, 1, 1, 1, 1));
		right.setLastLoginDate(LocalDateTime.of(2026, 1, 1, 1, 1));
		right.setTasks(new ArrayList<>());
		right.setComments(new ArrayList<>());

		// Act
		boolean equalsResult = left.equals(right);
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();

		// Assert
		assertThat(equalsResult).isTrue();
		assertThat(leftHash).isEqualTo(rightHash);
	}

	@Test
	void shouldGenerateToStringForUser() {
		// Arrange
		User user = new User();
		user.setUserId(1L);
		user.setUserName("u1");

		// Act
		String result = user.toString();

		// Assert
		assertThat(result).contains("User");
	}
}
