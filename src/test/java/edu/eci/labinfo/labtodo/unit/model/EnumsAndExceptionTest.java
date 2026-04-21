package edu.eci.labinfo.labtodo.unit.model;

import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.LabToDoExeption;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.Status;
import edu.eci.labinfo.labtodo.model.TopicTask;
import edu.eci.labinfo.labtodo.model.TypeTask;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnumsAndExceptionTest extends BaseUnitTest {

    @Test
    void shouldFindStatusByValueIgnoringCase() {
        // Arrange

        // Act
        Status result = Status.findByValue("en proceso");

        // Assert
        assertThat(result).isEqualTo(Status.INPROCESS);
    }

    @Test
    void shouldReturnNullWhenStatusValueIsUnknown() {
        // Arrange

        // Act
        Status result = Status.findByValue("does-not-exist");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNextStatusWhenCallingNext() {
        // Arrange

        // Act
        Status next = Status.PENDING.next();

        // Assert
        assertThat(next).isEqualTo(Status.INPROCESS);
    }

    @Test
    void shouldFindTypeTaskByValueIgnoringCase() {
        // Arrange

        // Act
        TypeTask result = TypeTask.findByValue("laboratorio");

        // Assert
        assertThat(result).isEqualTo(TypeTask.LABORATORIO);
    }

    @Test
    void shouldFindTopicTaskByValueIgnoringCase() {
        // Arrange

        // Act
        TopicTask result = TopicTask.findByValue("redes");

        // Assert
        assertThat(result).isEqualTo(TopicTask.REDES);
    }

    @Test
    void shouldFindRoleByValueIgnoringCase() {
        // Arrange

        // Act
        Role result = Role.findByValue("administrador");

        // Assert
        assertThat(result).isEqualTo(Role.ADMINISTRADOR);
    }

    @Test
    void shouldFindAccountTypeByValueIgnoringCase() {
        // Arrange

        // Act
        AccountType result = AccountType.findByValue("activo");

        // Assert
        assertThat(result).isEqualTo(AccountType.ACTIVO);
    }

    @Test
    void shouldCreateLabToDoExceptionWithMessage() {
        // Arrange
        String message = LabToDoExeption.INCOMPLETE_FIELDS;

        // Act
        LabToDoExeption exception = new LabToDoExeption(message);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}