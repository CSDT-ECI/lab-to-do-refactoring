package edu.eci.labinfo.labtodo.model;

/**
 * Enumeración que representa los tipos de tarea.
 * Cada tipo tiene un valor asociado que se utiliza para identificarlo.
 */
public enum TypeTask {

    MONITOR("Monitor"),
    LABORATORIO("Laboratorio"),
    ADMINISTRADOR("Administradores");

    private String value;

    TypeTask(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Busca un tipo de tarea por su valor, insensible a mayúsculas y minúsculas.
     *
     * @param type El valor de la tarea a buscar.
     * @return El tipo de tarea correspondiente al valor proporcionado, o null si no
     *         se encuentra.
     */
    public static TypeTask findByValue(String typeTask) {
        TypeTask response = null;
        for (TypeTask t : TypeTask.values()) {
            if (t.getValue().equalsIgnoreCase(typeTask)) {
                response = t;
                break;
            }
        }
        return response;
    }
}
