package edu.eci.labinfo.labtodo.unit.controller;

import edu.eci.labinfo.labtodo.controller.SemesterController;
import edu.eci.labinfo.labtodo.model.Semester;
import edu.eci.labinfo.labtodo.service.PrimeFacesWrapper;
import edu.eci.labinfo.labtodo.service.SemesterService;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import edu.eci.labinfo.labtodo.support.TestDataBuilders;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SemesterControllerTest extends BaseUnitTest {

    @Mock
    private SemesterService semesterService;

    @Mock
    private PrimeFacesWrapper primeFacesWrapper;

    @InjectMocks
    private SemesterController subject;

    @Test
    void shouldOpenNewYearAndResetCurrentSemester() {
        // Arrange
        subject.setSelectedUsers(new ArrayList<>(List.of("User A")));

        // Act
        subject.openNewYear();

        // Assert
        assertThat(subject.getSelectedUsers()).isEmpty();
        assertThat(subject.getCurrentSemester()).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenThereIsCurrentSemester() {
        // Arrange
        when(semesterService.getCurrentSemester()).thenReturn(TestDataBuilders.buildActiveSemester());

        // Act
        Boolean result = subject.isThereASemester();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenThereIsNoCurrentSemester() {
        // Arrange
        when(semesterService.getCurrentSemester()).thenReturn(null);

        // Act
        Boolean result = subject.isThereASemester();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnSemestersAsSelectItems() {
        // Arrange
        Semester semester = TestDataBuilders.buildActiveSemester();
        when(semesterService.getAllSemesters()).thenReturn(List.of(semester));

        // Act
        var result = subject.getSemestersLikeItems();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLabel()).isEqualTo("2026-1");
    }

    @Test
    void shouldNotSaveSemesterWhenStartDateIsAfterEndDate() {
        // Arrange
        subject.setCurrentSemester(new Semester());
        subject.setSemesterName("2026-1");
        subject.setStartDate(LocalDate.of(2026, 6, 30));
        subject.setEndDate(LocalDate.of(2026, 2, 1));
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveSemester();
        }

        // Assert
        verify(semesterService, never()).addSemester(org.mockito.ArgumentMatchers.any());
        verify(semesterService, never()).updateSemester(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSaveNewSemesterWhenPeriodIdIsNull() {
        // Arrange
        Semester current = new Semester();
        subject.setCurrentSemester(current);
        subject.setSemesterName("2026-1");
        subject.setStartDate(LocalDate.of(2026, 2, 1));
        subject.setEndDate(LocalDate.of(2026, 6, 30));
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveSemester();
        }

        // Assert
        verify(semesterService).addSemester(current);
        assertThat(subject.getSemesterName()).isNull();
        assertThat(subject.getStartDate()).isNull();
        assertThat(subject.getEndDate()).isNull();
    }

    @Test
    void shouldUpdateSemesterWhenPeriodIdExists() {
        // Arrange
        Semester current = TestDataBuilders.buildActiveSemester();
        subject.setCurrentSemester(current);
        subject.setSemesterName("2026-1");
        subject.setStartDate(LocalDate.of(2026, 2, 1));
        subject.setEndDate(LocalDate.of(2026, 6, 30));
        when(semesterService.updateSemester(current)).thenReturn(current);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        when(primeFacesWrapper.current()).thenReturn(primeFaces);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            subject.saveSemester();
        }

        // Assert
        verify(semesterService).updateSemester(current);
    }

    @Test
    void shouldExposeMutableStateThroughDataAccessors() {
        // Arrange
        Semester current = TestDataBuilders.buildActiveSemester();
        List<Semester> semesters = List.of(current);

        // Act
        subject.setSemesters(semesters);
        subject.setSemesterName("2026-1");
        subject.setStartDate(LocalDate.of(2026, 2, 1));
        subject.setCurrentSemester(current);
        subject.setEndDate(LocalDate.of(2026, 6, 30));
        subject.setSelectedUsers(new ArrayList<>(List.of("User A")));
        int hash = subject.hashCode();
        String text = subject.toString();

        // Assert
        assertThat(subject.getSemesters()).isEqualTo(semesters);
        assertThat(subject.getSemesterName()).isEqualTo("2026-1");
        assertThat(subject.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(subject.getCurrentSemester()).isEqualTo(current);
        assertThat(subject.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(subject.getSelectedUsers()).containsExactly("User A");
        assertThat(hash).isNotZero();
        assertThat(text).contains("SemesterController");
        assertThat(subject).isEqualTo(subject);
    }

}
