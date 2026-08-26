package mx.edu.utez.pigestiontutorias.models;

/**
 * Entidad que representa una carrera universitaria, con su nivel (TSU o
 * Ingeniería) y la academia a la que pertenece.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class Carrera {
    private int idCarrera;
    private String nombre;
    private String nivel;
    private int idAcademia;

    /**
     * Construye una carrera vacía.
     */
    public Carrera() {}

    /**
     * Construye una carrera con identificador y nombre.
     * @param idCarrera el identificador de la carrera
     * @param nombre el nombre de la carrera
     */
    public Carrera(int idCarrera, String nombre) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
    }

    /**
     * Construye una carrera con identificador, nombre y nivel.
     * @param idCarrera el identificador de la carrera
     * @param nombre el nombre de la carrera
     * @param nivel el nivel académico de la carrera (TSU o ING)
     */
    public Carrera(int idCarrera, String nombre, String nivel) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.nivel = nivel;
    }

    /**
     * Obtiene el identificador de la carrera.
     * @return el identificador de la carrera
     */
    public int getIdCarrera() {
        return idCarrera;
    }

    /**
     * Establece el identificador de la carrera.
     * @param idCarrera el identificador a asignar
     */
    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    /**
     * Obtiene el nombre de la carrera.
     * @return el nombre de la carrera
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la carrera.
     * @param nombre el nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Obtiene el nivel académico de la carrera. @return el nivel académico */
    public String getNivel() { return nivel; }
    /** Establece el nivel académico de la carrera. @param nivel el nivel a asignar */
    public void setNivel(String nivel) { this.nivel = nivel; }

    /** Obtiene el identificador de la academia a la que pertenece la carrera. @return el identificador de la academia */
    public int getIdAcademia() { return idAcademia; }
    /** Establece el identificador de la academia a la que pertenece la carrera. @param idAcademia el identificador a asignar */
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }
}
