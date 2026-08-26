package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO que representa el avance de un tutor respecto al objetivo de sesiones
 * grupales que debe realizar en un grupo durante el periodo escolar vigente.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
public class AvanceTutorGrupal {
    private int idTutor;
    private String nombreTutor;
    private String correoTutor;
    private int idGrupo;
    private String grupoAsignado;
    private int realizadas;
    private int objetivo;
    private String estatus;

    /** Obtiene el identificador del tutor. @return el identificador del tutor */
    public int getIdTutor() { return idTutor; }
    /** Establece el identificador del tutor. @param idTutor el identificador a asignar */
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    /** Obtiene el nombre del tutor. @return el nombre del tutor */
    public String getNombreTutor() { return nombreTutor; }
    /** Establece el nombre del tutor. @param nombreTutor el nombre a asignar */
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    /** Obtiene el correo del tutor. @return el correo del tutor */
    public String getCorreoTutor() { return correoTutor; }
    /** Establece el correo del tutor. @param correoTutor el correo a asignar */
    public void setCorreoTutor(String correoTutor) { this.correoTutor = correoTutor; }

    /** Obtiene el identificador del grupo. @return el identificador del grupo */
    public int getIdGrupo() { return idGrupo; }
    /** Establece el identificador del grupo. @param idGrupo el identificador a asignar */
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    /** Obtiene el nombre del grupo asignado. @return el grupo asignado */
    public String getGrupoAsignado() { return grupoAsignado; }
    /** Establece el nombre del grupo asignado. @param grupoAsignado el grupo a asignar */
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    /** Obtiene el número de sesiones grupales realizadas. @return el número de sesiones realizadas */
    public int getRealizadas() { return realizadas; }
    /** Establece el número de sesiones grupales realizadas. @param realizadas el número a asignar */
    public void setRealizadas(int realizadas) { this.realizadas = realizadas; }

    /** Obtiene el objetivo de sesiones grupales del periodo. @return el objetivo de sesiones */
    public int getObjetivo() { return objetivo; }
    /** Establece el objetivo de sesiones grupales del periodo. @param objetivo el objetivo a asignar */
    public void setObjetivo(int objetivo) { this.objetivo = objetivo; }

    /** Obtiene el estatus del avance respecto al objetivo. @return el estatus del avance */
    public String getEstatus() { return estatus; }
    /** Establece el estatus del avance respecto al objetivo. @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
