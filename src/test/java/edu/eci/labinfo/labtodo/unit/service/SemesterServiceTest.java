package edu.eci.labinfo.labtodo.unit.service;

import edu.eci.labinfo.labtodo.data.SemesterRepository;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.service.SemesterService;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import edu.eci.labinfo.labtodo.support.TestDataBuilders;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SemesterServiceTest extends BaseUnitTest {

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private SemesterService subject;

    @Test
    void shouldAddSemesterWhenSemesterIsValid() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterRepository.save(semester)).thenReturn(semester);

        // Act
        Semester result = subject.addSemester(semester);

        // Assert
        assertThat(result).isSameAs(semester);
    }

    @Test
    void shouldGetSemesterWhenIdExists() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterRepository.findById(10L)).thenReturn(Optional.of(semester));

        // Act
        Semester result = subject.getSemester(10L);

        // Assert
        assertThat(result.getSemesterName()).isEqualTo("2026-1");
    }

    @Test
    void shouldNotGetSemesterWhenIdDoesNotExist() {
        // Arrange
        when(semesterRepository.findById(77L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> subject.getSemester(77L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldGetSemesterByNameWhenExists() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterRepository.findBySemesterName("2026-1")).thenReturn(Optional.of(semester));

        // Act
        Semester result = subject.getSemesterByName("2026-1");

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnNullWhenSemesterByNameDoesNotExist() {
        // Arrange
        when(semesterRepository.findBySemesterName("missing")).thenReturn(Optional.empty());

        // Act
        Semester result = subject.getSemesterByName("missing");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldGetCurrentSemesterWhenDateMatchesRange() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterRepository.findByStartDateAndEndDate(LocalDate.now())).thenReturn(Optional.of(semester));

        // Act
        Semester result = subject.getCurrentSemester();

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnNullWhenCurrentSemesterDoesNotExist() {
        // Arrange
        when(semesterRepository.findByStartDateAndEndDate(LocalDate.now())).thenReturn(Optional.empty());

        // Act
        Semester result = subject.getCurrentSemester();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldUpdateSemesterWhenSemesterExists() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterRepository.existsById(semester.getPeriodId())).thenReturn(true);
        when(semesterRepository.save(semester)).thenReturn(semester);

        // Act
        Semester result = subject.updateSemester(semester);

        // Assert
        assertThat(result).isSameAs(semester);
    }

    @Test
    void shouldNotUpdateSemesterWhenSemesterDoesNotExist() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterRepository.existsById(semester.getPeriodId())).thenReturn(false);

        // Act
        Semester result = subject.updateSemester(semester);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldDeleteSemesterById() {
        // Arrange

        // Act
        subject.deleteTask(10L);

        // Assert
        verify(semesterRepository).deleteById(10L);
    }

    @Test
    void shouldGetAllSemesters() {
        // Arrange
        List<Semester> semesters = List.of(TestDataBuilders.buildActiveSemester());
        when(semesterRepository.findAll()).thenReturn(semesters);

        // Act
        List<Semester> result = subject.getAllSemesters();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldDeleteAllSemesters() {
        // Arrange

        // Act
        subject.deleteAllSemesters();

        // Assert
        verify(semesterRepository).deleteAll();
    }

}
