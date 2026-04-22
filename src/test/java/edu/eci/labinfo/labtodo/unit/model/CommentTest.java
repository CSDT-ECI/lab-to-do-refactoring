package edu.eci.labinfo.labtodo.unit.model;

import edu.eci.labinfo.labtodo.model.Comment;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest extends BaseUnitTest {

	@Test
	void shouldCreateCommentWithCurrentDateWhenUsingEmptyConstructor() {
		// Arrange

		// Act
		Comment comment = new Comment();

		// Assert
		assertThat(comment.getCreationDate()).isNotNull();
	}

	@Test
	void shouldCreateCommentLinkedToTaskWhenUsingConstructorWithTask() {
		// Arrange
		Task task = new Task();

		// Act
		Comment comment = new Comment(task, "Description");

		// Assert
		assertThat(comment.getTask()).isSameAs(task);
		assertThat(comment.getDescription()).isEqualTo("Description");
	}

	@Test
	void shouldReturnFormattedDateTextWhenCreationDateExists() {
		// Arrange
		Comment comment = new Comment();

		// Act
		String dateText = comment.getDateText();

		// Assert
		assertThat(dateText).isNotBlank();
	}

	@Test
	void shouldCompareCommentByEqualsAndHashCodeWhenSameData() {
		// Arrange
		Task task = new Task();
		task.setTaskId(1L);
		User author = new User();
		author.setUserId(2L);

		Comment left = new Comment(task, "Description");
		left.setCommentId(7L);
		left.setCreatorUser(author);

		Comment right = new Comment(task, "Description");
		right.setCommentId(7L);
		right.setCreatorUser(author);

		// Act
		boolean equalsResult = left.equals(right);
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();

		// Assert
		assertThat(equalsResult).isTrue();
		assertThat(leftHash).isEqualTo(rightHash);
	}

	@Test
	void shouldGenerateToStringForComment() {
		// Arrange
		Comment comment = new Comment(new Task(), "text");

		// Act
		String result = comment.toString();

		// Assert
		assertThat(result).contains("Comment");
	}
}
