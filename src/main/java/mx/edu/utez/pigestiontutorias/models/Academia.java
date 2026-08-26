package mx.edu.utez.pigestiontutorias.models;

/**
 * Entidad que representa una academia (área académica a la que pertenecen las carreras).
 * @author Sebastian-CR7
 * @version 1.0
 * @since 2026-07-23
 */
public class Academia {
    private int idAcademia;
    private String nombre;

    /**
     * Construye una academia vacía.
     */
    public Academia() {
    }

    /**
     * Construye una academia con los datos indicados.
     * @param idAcademia el identificador de la academia
     * @param nombre el nombre de la academia
     */
    public Academia(int idAcademia, String nombre) {
        this.idAcademia = idAcademia;
        this.nombre = nombre;
    }

    /**
     * Obtiene el identificador de la academia.
     * @return el identificador de la academia
     */
    public int getIdAcademia() {
        return idAcademia;
    }

    /**
     * Establece el identificador de la academia.
     * @param idAcademia el identificador a asignar
     */
    public void setIdAcademia(int idAcademia) {
        this.idAcademia = idAcademia;
    }

    /**
     * Obtiene el nombre de la academia.
     * @return el nombre de la academia
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la academia.
     * @param nombre el nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
