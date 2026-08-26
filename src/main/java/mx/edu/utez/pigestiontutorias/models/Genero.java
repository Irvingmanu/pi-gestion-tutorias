package mx.edu.utez.pigestiontutorias.models;

/**
 * Representa un género del catálogo de géneros del sistema, usado en el registro
 * de alumnos y tutores.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class Genero {
    private int id;
    private String nombre;

    /**
     * Construye un género vacío.
     */
    public Genero() {
    }

    /**
     * Construye un género con los datos indicados.
     * @param id el identificador del género
     * @param nombre el nombre del género
     */
    public Genero(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /** @return el identificador del género */
    public int getId() {
        return id;
    }

    /** @param id el identificador a asignar */
    public void setId(int id) {
        this.id = id;
    }

    /** @return el nombre del género */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre el nombre a asignar */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
