package mx.edu.utez.pigestiontutorias.models;

/**
 * Entidad que representa la asignación de un tutor a un grupo, con su estado
 * y datos enriquecidos del tutor y del grupo para su despliegue en listados.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
public class AsignacionTutor {
    private int idAsignacion;
    private int idTutor;
    private int idGrupo;
    private String estado;

    private String nombresTutor;
    private String apellidosTutor;
    private String nombreGrupo;
    private int idAcademia;

    /**
     * Construye una asignación de tutor vacía.
     */
    public AsignacionTutor() {}

    /**
     * Construye una asignación de tutor a un grupo, con estado activo ("S") por defecto.
     * @param idTutor el identificador del tutor
     * @param idGrupo el identificador del grupo
     */
    public AsignacionTutor(int idTutor, int idGrupo) {
        this.idTutor = idTutor;
        this.idGrupo = idGrupo;
        this.estado = "S";
    }

    /**
     * Obtiene el identificador de la asignación.
     * @return el identificador de la asignación
     */
    public int getIdAsignacion() { return idAsignacion; }
    /**
     * Establece el identificador de la asignación.
     * @param idAsignacion el identificador a asignar
     */
    public void setIdAsignacion(int idAsignacion) { this.idAsignacion = idAsignacion; }

    /**
     * Obtiene el identificador del tutor asignado.
     * @return el identificador del tutor
     */
    public int getIdTutor() { return idTutor; }
    /**
     * Establece el identificador del tutor asignado.
     * @param idTutor el identificador a asignar
     */
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    /**
     * Obtiene el identificador del grupo asignado.
     * @return el identificador del grupo
     */
    public int getIdGrupo() { return idGrupo; }
    /**
     * Establece el identificador del grupo asignado.
     * @param idGrupo el identificador a asignar
     */
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    /**
     * Obtiene el estado (activo/inactivo) de la asignación.
     * @return el estado de la asignación
     */
    public String getEstado() { return estado; }
    /**
     * Establece el estado (activo/inactivo) de la asignación.
     * @param estado el estado a asignar
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Obtiene los nombres del tutor asignado.
     * @return los nombres del tutor
     */
    public String getNombresTutor() { return nombresTutor; }
    /**
     * Establece los nombres del tutor asignado.
     * @param nombresTutor los nombres a asignar
     */
    public void setNombresTutor(String nombresTutor) { this.nombresTutor = nombresTutor; }

    /**
     * Obtiene los apellidos del tutor asignado.
     * @return los apellidos del tutor
     */
    public String getApellidosTutor() { return apellidosTutor; }
    /**
     * Establece los apellidos del tutor asignado.
     * @param apellidosTutor los apellidos a asignar
     */
    public void setApellidosTutor(String apellidosTutor) { this.apellidosTutor = apellidosTutor; }

    /**
     * Obtiene el nombre del grupo asignado.
     * @return el nombre del grupo
     */
    public String getNombreGrupo() { return nombreGrupo; }
    /**
     * Establece el nombre del grupo asignado.
     * @param nombreGrupo el nombre a asignar
     */
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    /**
     * Obtiene el identificador de la academia del grupo asignado.
     * @return el identificador de la academia
     */
    public int getIdAcademia() { return idAcademia; }
    /**
     * Establece el identificador de la academia del grupo asignado.
     * @param idAcademia el identificador a asignar
     */
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }
}
