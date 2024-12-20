package edu.eci.labinfo.labtodo.model;

public enum AccountType {

    SIN_VERIFICAR("Sin verificar"),
    ACTIVO("Activo"),
    ACEPTADO("Aceptado cambio de contraseña"),
    INACTIVO("Inactivo"),
    SOLICITUD_CAMBIO_CONTRASEÑA("Cambio de contraseña");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Busca un tipo de cuenta por su valor, insensible a mayúsculas y minúsculas.
     *
     * @param accountType El valor del rol a buscar.
     * @return El tipo de cuenta correspondiente al valor proporcionado, o null si no se
     * encuentra.
     */
    public static AccountType findByValue(String accountType) {
        AccountType response = null;
        for (AccountType r : AccountType.values()) {
            if (r.getValue().equalsIgnoreCase(accountType)) {
                response = r;
                break;
            }
        }
        return response;
    }

}
