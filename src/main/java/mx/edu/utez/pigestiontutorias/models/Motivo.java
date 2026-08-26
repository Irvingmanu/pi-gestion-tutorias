package mx.edu.utez.pigestiontutorias.models;

/**
 * Representa un motivo de canalización, asociado a un área de apoyo, usado al
 * canalizar a un alumno.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class Motivo {
    private int idMotivo;
    private int idArea;
    private String nombreMotivo;

    /**
     * Construye un motivo vacío.
     */
    public Motivo() {
    }

    /**
     * Construye un motivo con los datos indicados.
     * @param idMotivo el identificador del motivo
     * @param idArea el identificador del área de apoyo asociada
     * @param nombreMotivo el nombre del motivo
     */
    public Motivo(int idMotivo, int idArea, String nombreMotivo) {
        this.idMotivo = idMotivo;
        this.idArea = idArea;
        this.nombreMotivo = nombreMotivo;
    }

    /** @return el identificador del motivo */
    public int getIdMotivo() {
        return idMotivo;
    }

    /** @param idMotivo el identificador a asignar */
    public void setIdMotivo(int idMotivo) {
        this.idMotivo = idMotivo;
    }

    /** @return el identificador del área de apoyo asociada */
    public int getIdArea() {
        return idArea;
    }

    /** @param idArea el identificador de área a asignar */
    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    /** @return el nombre del motivo */
    public String getNombreMotivo() {
        return nombreMotivo;
    }

    /** @param nombreMotivo el nombre a asignar */
    public void setNombreMotivo(String nombreMotivo) {
        this.nombreMotivo = nombreMotivo;
    }
}
