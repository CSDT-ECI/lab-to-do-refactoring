package edu.eci.labinfo.labtodo.model;

/**
 * Enumeración que representa roles de usuario en una aplicación.
 * Cada rol tiene un valor asociado que se utiliza para identificarlo.
 */
public enum Role {

    MONITOR("Monitor"),
    ADMINISTRADOR("Administrador"),
    SUPERVISOR("Supervisor");

    private String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Busca un rol por su valor, insensible a mayúsculas y minúsculas.
     *
     * @param role El valor del rol a buscar.
     * @return El rol correspondiente al valor proporcionado, o null si no se
     *         encuentra.
     */
    public static Role findByValue(String role) {
        Role response = null;
        for (Role r : Role.values()) {
            if (r.getValue().equalsIgnoreCase(role)) {
                response = r;
                break;
            }
        }
        return response;
    }

}
