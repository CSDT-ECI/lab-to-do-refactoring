package edu.eci.labinfo.labtodo.unit.service;

import edu.eci.labinfo.labtodo.data.CommentRepository;
import edu.eci.labinfo.labtodo.model.Comment;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.service.CommentService;
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

class CommentServiceTest extends BaseUnitTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService subject;

    @Test
    void shouldAddCommentWhenCommentIsValid() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        Comment comment = new Comment(task, "New comment");
        when(commentRepository.save(comment)).thenReturn(comment);

        // Act
        Comment result = subject.addComment(comment);

        // Assert
        assertThat(result).isSameAs(comment);
    }

    @Test
    void shouldGetCommentWhenIdExists() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        Comment comment = new Comment(task, "Comment");
        comment.setCommentId(5L);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        // Act
        Comment result = subject.getComment(5L);

        // Assert
        assertThat(result.getCommentId()).isEqualTo(5L);
    }

    @Test
    void shouldNotGetCommentWhenIdDoesNotExist() {
        // Arrange
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> subject.getComment(99L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldGetCommentsByTask() {
        // Arrange
        Task task = TestDataBuilders.buildPendingLabTask();
        task.setTaskId(10L);
        List<Comment> expected = List.of(new Comment(task, "Comment 1"));
        when(commentRepository.findByTaskTaskId(10L)).thenReturn(expected);

        // Act
        List<Comment> result = subject.getComentsByTask(task);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetAllComments() {
        // Arrange
        List<Comment> expected = List.of(new Comment(TestDataBuilders.buildPendingLabTask(), "c1"));
        when(commentRepository.findAll()).thenReturn(expected);

        // Act
        List<Comment> result = subject.getAllComments();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldUpdateCommentWhenCommentExists() {
        // Arrange
        Comment comment = new Comment(TestDataBuilders.buildPendingLabTask(), "Update");
        comment.setCommentId(7L);
        when(commentRepository.existsById(7L)).thenReturn(true);
        when(commentRepository.save(comment)).thenReturn(comment);

        // Act
        Comment result = subject.updateComment(comment);

        // Assert
        assertThat(result).isSameAs(comment);
    }

    @Test
    void shouldNotUpdateCommentWhenCommentDoesNotExist() {
        // Arrange
        Comment comment = new Comment(TestDataBuilders.buildPendingLabTask(), "Update");
        comment.setCommentId(8L);
        when(commentRepository.existsById(8L)).thenReturn(false);

        // Act
        Comment result = subject.updateComment(comment);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldDeleteCommentById() {
        // Arrange
        Long commentId = 12L;

        // Act
        subject.deleteComment(commentId);

        // Assert
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void shouldDeleteAllComments() {
        // Arrange

        // Act
        subject.deleteAllComments();

        // Assert
        verify(commentRepository).deleteAll();
    }

}
