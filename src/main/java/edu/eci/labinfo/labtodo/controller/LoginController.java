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
        userService.getUsers().forEach(user -> fullNameusers.add(user.getFullName()));
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
        if (password == null || userName == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.INCOMPLETE_FIELDS, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }
        // Buscar al usuario por nombre de usuario
        User userToLogin = userService.getUserByUserName(userName);
        // Si el usuario no existe o la contraseña es incorrecta, mostrar un mensaje de
        // error y salir temprano
        if (userToLogin == null || !passwordEncoder.matches(password, userToLogin.getPassword())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, LabToDoExeption.CREDENTIALS_INCORRECT, ERROR));
            PrimeFaces.current().ajax().update(LOGIN_FORM_MESSAGES);
            return false;
        }
        // Si al usuario no se le ha verificado su cuenta, mostrar un mensaje de error y
        // salir temprano
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
        userToLogin.setConnect(true);
        userService.updateUser(userToLogin);
        return true;
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
        boolean isAdminUser = false;
        User user = userService.getUserByUserName(userName);
        if (user.getRole().equals(Role.ADMINISTRADOR.getValue())) {
            isAdminUser = true;
        }
        return isAdminUser;
    }

}
