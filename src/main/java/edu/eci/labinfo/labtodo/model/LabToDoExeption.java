package edu.eci.labinfo.labtodo.model;

public class LabToDoExeption extends Exception {

    public static final String NO_STATE_SELECTED = "Por favor, selecciona un estado.";
    public static final String USER_NAME_EXISTS = "Por favor, cambia el nombre de usuario el elegido ya existe.";
    public static final String NO_ROLE_SELECTED = "Por favor, selecciona un rol de usuario.";
    public static final String NO_ACCOUNT_TYPE_SELECTED = "Por favor, selecciona un estado de cuenta.";
    public static final String EXISTING_USER = "La cuenta ya existe.";
    public static final String ERROR_CREATING_ACCOUNT = "Se produjo un error al crear la cuenta.";
    public static final String INCOMPLETE_FIELDS = "Por favor complete todos los campos.";
    public static final String CREDENTIALS_INCORRECT = "Su cuenta o contraseña es incorrecta.";
    public static final String UNVERIFIED_ACCOUNT = "Su cuenta no ha sido verificada.";
    public static final String INVALID_DATE = "La fecha de inicio debe ser menor a la fecha de fin";
    public static final String USER_INCORRECT = "Su cuenta es incorrecta.";
    public static final String WAIT_RESPONSE = "Todavía no es aceptada la solicitud de cambio de contraseña.";
    public static final String USER_NOT_NEW_PASSWORD = "El usuario no ha pedido cambio de contraseña.";
    public static final String USER_NOT_ACTIVE = "Su cuenta no esta activa, no se pudo realizar la solicitud.";
    public static final String PROCCESS_CHANGE_APPLICATION = "Tiene una solicitud de cambio de contraseña ya activa, esper la confirmación.";
    public static final String DELETE_USER = "El usuario que esta tratando de eliminar tiene tareas asignadas";
    public LabToDoExeption(String message) {
        super(message);
    }

}
