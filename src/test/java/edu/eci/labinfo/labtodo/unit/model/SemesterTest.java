package edu.eci.labinfo.labtodo.unit.model;

import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterTest extends BaseUnitTest {

	@Test
	void shouldStoreSemesterDatesAndName() {
		// Arrange
		Semester semester = new Semester();

		// Act
		semester.setSemesterName("2026-2");
		semester.setStartDate(LocalDate.of(2026, 7, 1));
		semester.setEndDate(LocalDate.of(2026, 11, 30));

		// Assert
		assertThat(semester.getSemesterName()).isEqualTo("2026-2");
		assertThat(semester.getStartDate()).isBefore(semester.getEndDate());
	}

	@Test
	void shouldConstructSemesterWithAllArgsConstructor() {
		// Arrange
		ArrayList<Task> tasks = new ArrayList<>();

		// Act
		Semester semester = new Semester(
				99L,
				"2027-1",
				LocalDate.of(2027, 1, 1),
				LocalDate.of(2027, 6, 30),
				tasks
		);

		// Assert
		assertThat(semester.getPeriodId()).isEqualTo(99L);
		assertThat(semester.getTasks()).isSameAs(tasks);
	}

	@Test
	void shouldCompareSemesterByEqualsAndHashCodeWhenSameData() {
		// Arrange
		Semester left = new Semester();
		left.setPeriodId(1L);
		left.setSemesterName("2026-2");
		left.setStartDate(LocalDate.of(2026, 7, 1));
		left.setEndDate(LocalDate.of(2026, 11, 30));
		left.setTasks(new ArrayList<>());

		Semester right = new Semester();
		right.setPeriodId(1L);
		right.setSemesterName("2026-2");
		right.setStartDate(LocalDate.of(2026, 7, 1));
		right.setEndDate(LocalDate.of(2026, 11, 30));
		right.setTasks(new ArrayList<>());

		// Act
		boolean equalsResult = left.equals(right);
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();

		// Assert
		assertThat(equalsResult).isTrue();
		assertThat(leftHash).isEqualTo(rightHash);
	}

	@Test
	void shouldGenerateToStringForSemester() {
		// Arrange
		Semester semester = new Semester();
		semester.setSemesterName("2026-2");

		// Act
		String result = semester.toString();

		// Assert
		assertThat(result).contains("Semester");
	}
}
