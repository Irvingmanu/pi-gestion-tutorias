package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO que representa el seguimiento de tutorías grupales de un tutor dentro de un
 * grupo asignado, comparando las sesiones realizadas contra el objetivo del periodo.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-17
 */
public class SeguimientoTutorGrupalDTO {
    private int idTutor;
    private String nombreTutor;
    private String correoTutor;
    private int idCarrera;
    private int idCuatrimestre;
    private int idLetraGrupo;
    private String grupoAsignado;
    private int realizadas;
    private int objetivo;
    private String estatus;

    /** @return el identificador del tutor */
    public int getIdTutor() { return idTutor; }
    /** @param idTutor el identificador a asignar */
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    /** @return el nombre del tutor */
    public String getNombreTutor() { return nombreTutor; }
    /** @param nombreTutor el nombre a asignar */
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    /** @return el correo del tutor */
    public String getCorreoTutor() { return correoTutor; }
    /** @param correoTutor el correo a asignar */
    public void setCorreoTutor(String correoTutor) { this.correoTutor = correoTutor; }

    /** @return el identificador de la carrera del grupo asignado */
    public int getIdCarrera() { return idCarrera; }
    /** @param idCarrera el identificador de carrera a asignar */
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    /** @return el identificador del cuatrimestre del grupo asignado */
    public int getIdCuatrimestre() { return idCuatrimestre; }
    /** @param idCuatrimestre el identificador de cuatrimestre a asignar */
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    /** @return el identificador de la letra del grupo asignado */
    public int getIdLetraGrupo() { return idLetraGrupo; }
    /** @param idLetraGrupo el identificador de letra a asignar */
    public void setIdLetraGrupo(int idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }

    /** @return el nombre del grupo asignado al tutor */
    public String getGrupoAsignado() { return grupoAsignado; }
    /** @param grupoAsignado el nombre de grupo a asignar */
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    /** @return la cantidad de sesiones grupales realizadas */
    public int getRealizadas() { return realizadas; }
    /** @param realizadas la cantidad de sesiones realizadas a asignar */
    public void setRealizadas(int realizadas) { this.realizadas = realizadas; }

    /** @return el objetivo de sesiones grupales del periodo */
    public int getObjetivo() { return objetivo; }
    /** @param objetivo el objetivo a asignar */
    public void setObjetivo(int objetivo) { this.objetivo = objetivo; }

    /** @return el estatus del seguimiento (por ejemplo, "Cumplido" o "Pendiente") */
    public String getEstatus() { return estatus; }
    /** @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
