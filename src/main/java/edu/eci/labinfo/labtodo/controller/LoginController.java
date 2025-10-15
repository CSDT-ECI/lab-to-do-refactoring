package edu.eci.labinfo.labtodo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import edu.eci.labinfo.labtodo.model.AccountType;
import edu.eci.labinfo.labtodo.model.LabToDoExeption;
import edu.eci.labinfo.labtodo.model.Role;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.service.UserService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import lombok.Data;

@Component
@Data
@SessionScope
public class LoginController {

    private User createdUserAccount;
    private String userName;
    private String password;
    private List<User> users;
    private String newPassword;
    private String confirmPassword;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static final String LOGIN_FORM_MESSAGES = "login-form:messages";
    private static final String ERROR = "Error";
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    public List<User> getUsers() {
        return userService.getUsers();
    }

    public List<String> getUserNames() {
        List<String> fullNameusers = new ArrayList<>();
        userService.getUsers().stream()
            .filter(u -> {
                String at = u.getAccountType();
                if (at == null) return true; // include if unknown
                return !AccountType.INACTIVO.getValue().equalsIgnoreCase(at)
                        && !AccountType.SIN_VERIFICAR.getValue().equalsIgnoreCase(at);
            })
            .forEach(user -> fullNameusers.add(user.getFullName()));
        return fullNameusers;
    }

    public void createUserAccount() {
        logger.info("Creando cuenta de usuario");
        this.createdUserAccount = new User();
    }


    public Boolean saveUserAccount() {
        logger.info("Guardando cuenta de usuario");
        // Agregar usuario
        try {
            if (this.createdUserAccount.getUserId() == null) {
                // Server-side password strength validation
                if (this.createdUserAccount.getPassword() == null || !isPasswordStrong(this.createdUserAccount.getPassword())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "La contraseña no cumple los requisitos de seguridad.", ""));
                    PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
                    return false;
                }
                this.createdUserAccount.setRole(Role.MONITOR.getValue());
                this.createdUserAccount.setAccountType(AccountType.SIN_VERIFICAR.getValue());
                this.userService.addUser(this.createdUserAccount);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cuenta creada exitosamente"));
            }

        } catch (LabToDoExeption e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
            PrimeFaces.current().executeScript("PF('createAccountDialog').hide()");
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        PrimeFaces.current().executeScript("PF('createAccountDialog').hide()");
        PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
        // Resetear el usuario
        this.createdUserAccount = null;
        return true;
    }


    public Boolean login() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // Verificar que se ingresó un nombre de usuario y una contraseña
        if (password.equals(null) || userName.equals(null)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.INCOMPLETE_FIELDS, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }
        
        User userToLogin = userService.getUserByUserName(userName);

        if (userToLogin == null){
            FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.CREDENTIALS_INCORRECT, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            PrimeFaces.current().executeScript("setTimeout(function() { window.location.href = 'dashboard.xhtml'; }, 5000);");
            return false;
        }

        // Si al usuario ya se le acepto el cambio de contraseña
        if (userToLogin.getAccountType().equals(AccountType.ACEPTADO.getValue())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fue aceptada tu solicitud de cambio de contraseña, realiza el cambio en la sección \"Olvide la contraseña\"", ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        // Si al usuario no se le ha aceptado el cambio de contraseña
        if (userToLogin.getAccountType().equals(AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, LabToDoExeption.WAIT_RESPONSE,""));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }
         // Si el usuario no existe o la contraseña es incorrecta, mostrar un mensaje de error y salir temprano
        if ((!passwordEncoder.matches(password, userToLogin.getPassword()))) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.CREDENTIALS_INCORRECT, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }
        // Si al usuario no se le ha verificado su cuenta, mostrar un mensaje de error y salir temprano
        if (userToLogin.getAccountType().equals(AccountType.SIN_VERIFICAR.getValue())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.UNVERIFIED_ACCOUNT, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }
        
        // Si el usuario está autenticado, redirigirlo a la página correspondiente
        try {
            password = null;
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            String redirectPath = "./dashboard.xhtml";
            ec.redirect(ec.getRequestContextPath() + redirectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        userService.updateUser(userToLogin);
        return true;
    }

    //Metodo para realizar la solicitud de cambio de contraseña
    
    public Boolean requiredNewPassword(){
        User userToLogin = userService.getUserByUserName(userName);
        if (userToLogin == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.USER_INCORRECT, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        String accountType = userToLogin.getAccountType();

        // Si la cuenta ya fue ACEPTADO -> mostrar dialog para ingresar nueva contraseña
        if (AccountType.ACEPTADO.getValue().equalsIgnoreCase(accountType)) {
            PrimeFaces.current().executeScript("PF('changePassword').hide(); PF('changePasswordNew').show();");
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return true;
        }

        // Si ya solicitó el cambio, informar al usuario que esté a la espera
        if (AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue().equalsIgnoreCase(accountType)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, LabToDoExeption.PROCCESS_CHANGE_APPLICATION, ""));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        // Si la cuenta está ACTIVO -> crear la solicitud de cambio
        if (AccountType.ACTIVO.getValue().equalsIgnoreCase(accountType)) {
            userToLogin.setAccountType(AccountType.SOLICITUD_CAMBIO_CONTRASEÑA.getValue());
            userService.updateUser(userToLogin);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Solicitud de cambio de contraseña enviada con exito"));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            PrimeFaces.current().executeScript("PF('changePassword').hide()");
            return true;
        }

        // Otros estados -> notificar
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, LabToDoExeption.USER_NOT_ACTIVE, ""));
        PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
        return false;
    }

    /**
     * Procesa la nueva contraseña enviada desde el dialog cuando el cambio fue ACEPTADO.
     */
    public Boolean submitNewPassword(){
        User userToLogin = userService.getUserByUserName(userName);
        if (userToLogin == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.USER_INCORRECT, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        // Verificar que la cuenta tenga el estado ACEPTADO
        if (!userToLogin.getAccountType().equals(AccountType.ACEPTADO.getValue())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Su solicitud de cambio no fue aceptada aún.", ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        // Validaciones server-side por si el cliente las omitió
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Las contraseñas no coinciden.", ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        if (!isPasswordStrong(newPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "La contraseña no cumple los requisitos de seguridad.", ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encoderPassword = passwordEncoder.encode(newPassword);
        userToLogin.setPassword(encoderPassword);
        userToLogin.setAccountType(AccountType.ACTIVO.getValue());
        userService.updateUser(userToLogin);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Contraseña actualizada con éxito.", ""));
        PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
        PrimeFaces.current().executeScript("PF('changePasswordNew').hide()");
        // limpiar campos
        this.newPassword = null;
        this.confirmPassword = null;
        return true;
    }

    // Validación básica de fuerza de contraseña (mínimo 8, mayúscula, minúscula, número, especial)
    private boolean isPasswordStrong(String pw) {
        if (pw.length() < 8) return false;
        boolean hasUpper = pw.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = pw.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = pw.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = pw.chars().anyMatch(ch -> "!@#$%&*()_+-=[]|,./?><".indexOf(ch) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Obtiene el nombre del usuario actual extraido de la base de datos.
     * 
     * @param userName el nombre del usuario a obtener.
     * @return el nombre del usuario actual.
     */
    public String getCurrentUserName(String userName) {
        return userService.getUserByUserName(userName).getUserName();
    }

    /**
     * Obtiene el nombre completo del usuario actual extraido de la base de datos.
     * 
     * @param userName el nombre del usuario a obtener.
     * @return el nombre completo del usuario actual.
     */
    public String getCurrentFullName(String userName) {
        return userService.getUserByUserName(userName).getFullName();
    }

    /**
     * Obtiene el rol del usuario actual extraido de la base de datos.
     * 
     * @param userName el nombre del usuario a obtener.
     * @return el rol del usuario actual.
     */
    public String getCurrentUserProfile(String userName) {
        return userService.getUserByUserName(userName).getRole();
    }

    /**
     * Función que permite el cierre de sesión
     * 
     * @return True si el cierre de sesión es exitoso, de lo contrario False
     */
    public Boolean logout() {

        userName = null;
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            String redirectPath = "./login.xhtml";

            ec.redirect(ec.getRequestContextPath() + redirectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Función que redirige a la página correspondiente según el rol del usuario
     * 
     * @param user el usuario que se está autenticando
     * @return la ruta de la página a la que se debe redirigir
     */
    public Boolean getRedirectPath(String userName, String sendTo) {
        User user = userService.getUserByUserName(userName);
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        String redirectPath = "";
        switch (sendTo) {
            case "control":
                redirectPath = "./admindashboard.xhtml";
                break;
            case "config":
                redirectPath = "./settings.xhtml";
                break;
            case "supervision":
                redirectPath = "./dashboardSupervision.xhtml";
                break;
            default:
                redirectPath = "./dashboard.xhtml";
                break;
        }
        try {
            if (user.getRole().equals(Role.ADMINISTRADOR.getValue())) {
                ec.redirect(ec.getRequestContextPath() + redirectPath);
                return true;
            } else {
                ec.redirect(ec.getRequestContextPath() + redirectPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Función que verifica si el usuario es administrador.
     * 
     * @param userName el nombre del usuario a verificar.
     * @return True si el usuario es administrador, de lo contrario False.
     */
    public boolean isAdmin(String userName) {
        // If no userName provided, use the current session userName
        if (userName == null) {
            userName = this.userName;
        }
        if (userName == null) {
            return false;
        }
        User user = userService.getUserByUserName(userName);
        if (user == null) return false;
        return Role.ADMINISTRADOR.getValue().equals(user.getRole());
    }


    /**
     * Función que verifica si el usuario es supervisor
     * 
     * @param userName el nombre del usuario a verificar.
     * @return True si el usuario es supervisor, de lo contrario False.
     */
    public boolean isSupervisor(String userName) {
        boolean isSupervisorUser = false;
        User user = userService.getUserByUserName(userName);
        if (user.getRole().equals(Role.SUPERVISOR.getValue())) {
            isSupervisorUser = true;
        }
        return isSupervisorUser;
    }
}
