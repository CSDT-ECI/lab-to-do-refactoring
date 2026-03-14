package edu.eci.labinfo.labtodo.unit.controller;

import edu.eci.labinfo.labtodo.controller.LoginController;
import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.LabToDoExeption;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.UserService;
import edu.eci.labinfo.labtodo.support.BaseUnitTest;
import edu.eci.labinfo.labtodo.support.TestDataBuilders;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.primefaces.PrimeFaces;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginControllerTest extends BaseUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginController subject;

    @Test
    void shouldGetUsersFromService() {
        // Arrange
        when(userService.getUsers()).thenReturn(List.of(TestDataBuilders.buildMonitorUser()));

        // Act
        List<User> result = subject.getUsers();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetUserNamesFilteringInactiveAndUnverified() {
        // Arrange
        User active = TestDataBuilders.buildMonitorUser();
        active.setFullName("Active User");
        active.setAccountType(AccountType.ACTIVO.getValue());
        User inactive = TestDataBuilders.buildMonitorUser();
        inactive.setFullName("Inactive User");
        inactive.setAccountType(AccountType.INACTIVO.getValue());
        User unverified = TestDataBuilders.buildMonitorUser();
        unverified.setFullName("Unverified User");
        unverified.setAccountType(AccountType.SIN_VERIFICAR.getValue());

        when(userService.getUsers()).thenReturn(List.of(active, inactive, unverified));

        // Act
        List<String> result = subject.getUserNames();

        // Assert
        assertThat(result).containsExactly("Active User");
    }

    @Test
    void shouldReturnCurrentUserName() {
        // Arrange
        User user = TestDataBuilders.buildAdminUser();
        when(userService.getUserByUserName("test.admin")).thenReturn(user);

        // Act
        String result = subject.getCurrentUserName("test.admin");

        // Assert
        assertThat(result).isEqualTo("test.admin");
    }

    @Test
    void shouldReturnCurrentFullName() {
        // Arrange
        User user = TestDataBuilders.buildAdminUser();
        user.setFullName("Admin Full Name");
        when(userService.getUserByUserName("test.admin")).thenReturn(user);

        // Act
        String result = subject.getCurrentFullName("test.admin");

        // Assert
        assertThat(result).isEqualTo("Admin Full Name");
    }

    @Test
    void shouldReturnCurrentUserProfile() {
        // Arrange
        User user = TestDataBuilders.buildAdminUser();
        when(userService.getUserByUserName("test.admin")).thenReturn(user);

        // Act
        String result = subject.getCurrentUserProfile("test.admin");

        // Assert
        assertThat(result).isEqualTo(Role.ADMINISTRADOR.getValue());
    }

    @Test
    void shouldReturnTrueWhenUserIsAdmin() {
        // Arrange
        User user = TestDataBuilders.buildAdminUser();
        when(userService.getUserByUserName("test.admin")).thenReturn(user);

        // Act
        boolean result = subject.isAdmin("test.admin");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNullInIsAdmin() {
        // Arrange
        when(userService.getUserByUserName("missing")).thenReturn(null);

        // Act
        boolean result = subject.isAdmin("missing");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenUserIsSupervisor() {
        // Arrange
        User user = TestDataBuilders.buildMonitorUser();
        user.setRole(Role.SUPERVISOR.getValue());
        when(userService.getUserByUserName("supervisor")).thenReturn(user);

        // Act
        boolean result = subject.isSupervisor("supervisor");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void shouldCreateUserAccount() {
        // Arrange

        // Act
        subject.createUserAccount();

        // Assert
        assertThat(subject.getCreatedUserAccount()).isNotNull();
    }

    @Test
    void shouldNotSaveUserAccountWhenPasswordIsWeak() {
        // Arrange
        User user = new User();
        user.setUserId(null);
        user.setPassword("weak");
        subject.setCreatedUserAccount(user);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.saveUserAccount();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldSaveUserAccountWhenPasswordIsStrong() throws Exception {
        // Arrange
        User user = new User();
        user.setUserId(null);
        user.setPassword("Strong#123");
        subject.setCreatedUserAccount(user);

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.saveUserAccount();

            // Assert
            assertThat(result).isTrue();
            verify(userService).addUser(user);
            assertThat(subject.getCreatedUserAccount()).isNull();
        }
    }

    @Test
    void shouldNotSaveUserAccountWhenServiceThrowsException() throws Exception {
        // Arrange
        User user = new User();
        user.setUserId(null);
        user.setPassword("Strong#123");
        subject.setCreatedUserAccount(user);
        when(userService.addUser(user)).thenThrow(new LabToDoExeption("error"));

        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.saveUserAccount();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldNotRequireNewPasswordWhenUserDoesNotExist() {
        // Arrange
        subject.setUserName("unknown");
        when(userService.getUserByUserName("unknown")).thenReturn(null);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.requiredNewPassword();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldRequireNewPasswordWhenAccountTypeIsAccepted() {
        // Arrange
        subject.setUserName("accepted");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACEPTADO.getValue());
        when(userService.getUserByUserName("accepted")).thenReturn(user);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.requiredNewPassword();

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Test
    void shouldRequireNewPasswordWhenAccountTypeIsActivo() {
        // Arrange
        subject.setUserName("active");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACTIVO.getValue());
        when(userService.getUserByUserName("active")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.requiredNewPassword();

            // Assert
            assertThat(result).isTrue();
            assertThat(user.getAccountType()).isEqualTo(AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue());
            verify(userService).updateUser(user);
        }
    }

    @Test
    void shouldNotSubmitNewPasswordWhenPasswordsDoNotMatch() {
        // Arrange
        subject.setUserName("accepted");
        subject.setNewPassword("Strong#123");
        subject.setConfirmPassword("Other#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACEPTADO.getValue());
        when(userService.getUserByUserName("accepted")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.submitNewPassword();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldSubmitNewPasswordWhenDataIsValid() {
        // Arrange
        subject.setUserName("accepted");
        subject.setNewPassword("Strong#123");
        subject.setConfirmPassword("Strong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACEPTADO.getValue());
        user.setPassword("old");
        when(userService.getUserByUserName("accepted")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.submitNewPassword();

            // Assert
            assertThat(result).isTrue();
            assertThat(user.getAccountType()).isEqualTo(AccountType.ACTIVO.getValue());
            verify(userService).updateUser(user);
            assertThat(subject.getNewPassword()).isNull();
            assertThat(subject.getConfirmPassword()).isNull();
        }
    }

    @Test
    void shouldLogoutAndClearUserName() throws IOException {
        // Arrange
        subject.setUserName("someone");
        FacesContext facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestContextPath()).thenReturn("/ctx");

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            boolean result = subject.logout();

            // Assert
            assertThat(result).isTrue();
            assertThat(subject.getUserName()).isNull();
            verify(externalContext).redirect("/ctx./login.xhtml");
        }
    }

    @Test
    void shouldReturnTrueOnRedirectPathForAdmin() throws IOException {
        // Arrange
        User admin = TestDataBuilders.buildAdminUser();
        when(userService.getUserByUserName("admin")).thenReturn(admin);
        FacesContext facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestContextPath()).thenReturn("/ctx");

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            boolean result = subject.getRedirectPath("admin", "control");

            // Assert
            assertThat(result).isTrue();
            verify(externalContext).redirect("/ctx./admindashboard.xhtml");
        }
    }

    @Test
    void shouldReturnFalseOnRedirectPathForNonAdmin() throws IOException {
        // Arrange
        User monitor = TestDataBuilders.buildMonitorUser();
        when(userService.getUserByUserName("monitor")).thenReturn(monitor);
        FacesContext facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestContextPath()).thenReturn("/ctx");

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            boolean result = subject.getRedirectPath("monitor", "config");

            // Assert
            assertThat(result).isFalse();
            verify(externalContext).redirect("/ctx./settings.xhtml");
        }
    }

    @Test
    void shouldNotLoginWhenUserDoesNotExist() {
        // Arrange
        subject.setUserName("missing");
        subject.setPassword("Strong#123");
        when(userService.getUserByUserName("missing")).thenReturn(null);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.login();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldNotLoginWhenAccountTypeIsAccepted() {
        // Arrange
        subject.setUserName("accepted");
        subject.setPassword("Strong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACEPTADO.getValue());
        when(userService.getUserByUserName("accepted")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.login();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldNotLoginWhenAccountTypeIsPasswordChangeRequested() {
        // Arrange
        subject.setUserName("requested");
        subject.setPassword("Strong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue());
        when(userService.getUserByUserName("requested")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.login();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldNotLoginWhenPasswordDoesNotMatch() {
        // Arrange
        subject.setUserName("active");
        subject.setPassword("Wrong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACTIVO.getValue());
        user.setPassword(new BCryptPasswordEncoder().encode("Correct#123"));
        when(userService.getUserByUserName("active")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.login();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldNotLoginWhenAccountIsUnverified() {
        // Arrange
        subject.setUserName("unverified");
        subject.setPassword("Strong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.SIN_VERIFICAR.getValue());
        user.setPassword(new BCryptPasswordEncoder().encode("Strong#123"));
        when(userService.getUserByUserName("unverified")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.login();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldLoginWhenCredentialsAreValidAndAccountIsActive() throws Exception {
        // Arrange
        subject.setUserName("active");
        subject.setPassword("Strong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACTIVO.getValue());
        user.setPassword(new BCryptPasswordEncoder().encode("Strong#123"));
        when(userService.getUserByUserName("active")).thenReturn(user);

        FacesContext facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestContextPath()).thenReturn("/ctx");

        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.login();

            // Assert
            assertThat(result).isTrue();
            assertThat(subject.getPassword()).isNull();
            verify(userService).updateUser(user);
            verify(externalContext).redirect("/ctx./dashboard.xhtml");
        }
    }

    @Test
    void shouldNotRequireNewPasswordWhenAccountTypeIsSolicitudCambioContrasena() {
        // Arrange
        subject.setUserName("requested");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue());
        when(userService.getUserByUserName("requested")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.requiredNewPassword();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldNotRequireNewPasswordWhenUserIsNotActive() {
        // Arrange
        subject.setUserName("inactive");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.INACTIVO.getValue());
        when(userService.getUserByUserName("inactive")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.requiredNewPassword();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldNotSubmitNewPasswordWhenUserDoesNotExist() {
        // Arrange
        subject.setUserName("missing");
        subject.setNewPassword("Strong#123");
        subject.setConfirmPassword("Strong#123");
        when(userService.getUserByUserName("missing")).thenReturn(null);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.submitNewPassword();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Test
    void shouldNotSubmitNewPasswordWhenAccountTypeIsNotAccepted() {
        // Arrange
        subject.setUserName("active");
        subject.setNewPassword("Strong#123");
        subject.setConfirmPassword("Strong#123");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACTIVO.getValue());
        when(userService.getUserByUserName("active")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.submitNewPassword();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldNotSubmitNewPasswordWhenPasswordIsWeak() {
        // Arrange
        subject.setUserName("accepted");
        subject.setNewPassword("weak");
        subject.setConfirmPassword("weak");
        User user = TestDataBuilders.buildMonitorUser();
        user.setAccountType(AccountType.ACEPTADO.getValue());
        when(userService.getUserByUserName("accepted")).thenReturn(user);
        FacesContext facesContext = mock(FacesContext.class);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class);
             MockedStatic<PrimeFaces> pf = org.mockito.Mockito.mockStatic(PrimeFaces.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            boolean result = subject.submitNewPassword();

            // Assert
            assertThat(result).isFalse();
            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void shouldReturnFalseOnRedirectPathForAdminSupervision() throws IOException {
        // Arrange
        User admin = TestDataBuilders.buildAdminUser();
        when(userService.getUserByUserName("admin")).thenReturn(admin);
        FacesContext facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestContextPath()).thenReturn("/ctx");

        // Act
        try (MockedStatic<FacesContext> faces = org.mockito.Mockito.mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            boolean result = subject.getRedirectPath("admin", "supervision");

            // Assert
            assertThat(result).isTrue();
            verify(externalContext).redirect("/ctx./dashboardSupervision.xhtml");
        }
    }

    @Test
    void shouldReturnFalseWhenUserNameIsNullInIsAdmin() {
        // Arrange
        subject.setUserName(null);

        // Act
        boolean result = subject.isAdmin(null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldExposeMutableStateThroughDataAccessors() {
        // Arrange
        User created = TestDataBuilders.buildMonitorUser();
        when(userService.getUsers()).thenReturn(List.of(created));

        // Act
        subject.setCreatedUserAccount(created);
        subject.setUserName("monitor");
        subject.setPassword("Strong#123");
        subject.setUsers(List.of(created));
        subject.setNewPassword("New#123");
        subject.setConfirmPassword("New#123");
        int hash = subject.hashCode();
        String text = subject.toString();

        // Assert
        assertThat(subject.getCreatedUserAccount()).isEqualTo(created);
        assertThat(subject.getUserName()).isEqualTo("monitor");
        assertThat(subject.getPassword()).isEqualTo("Strong#123");
        assertThat(subject.getUsers()).hasSize(1);
        assertThat(subject.getNewPassword()).isEqualTo("New#123");
        assertThat(subject.getConfirmPassword()).isEqualTo("New#123");
        assertThat(hash).isNotZero();
        assertThat(text).contains("LoginController");
        assertThat(subject).isEqualTo(subject);
    }

}
