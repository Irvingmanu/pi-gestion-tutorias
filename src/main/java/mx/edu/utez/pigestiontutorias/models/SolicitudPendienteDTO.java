package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * DTO que representa una solicitud de tutoría pendiente, con los datos del alumno
 * solicitante y del tutor destinatario ya resueltos para su presentación en las
 * vistas de reportes y seguimiento.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
public class SolicitudPendienteDTO {
    private int idSolicitud;
    private String matricula;
    private String nombreAlumno;
    private String grupoAsignado;
    private String asunto;
    private String descripcion;
    private Date fechaPropuesta;
    private String horaPropuesta;
    private Integer duracion;
    private String estatus;
    private int idTutor;
    private String nombreTutor;
    private String correoTutor;

    /** @return el identificador de la solicitud */
    public int getIdSolicitud() { return idSolicitud; }
    /** @param idSolicitud el identificador a asignar */
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    /** @return la matrícula del alumno solicitante */
    public String getMatricula() { return matricula; }
    /** @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /** @return el nombre del alumno solicitante */
    public String getNombreAlumno() { return nombreAlumno; }
    /** @param nombreAlumno el nombre a asignar */
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    /** @return el nombre del grupo asignado al alumno */
    public String getGrupoAsignado() { return grupoAsignado; }
    /** @param grupoAsignado el nombre de grupo a asignar */
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    /** @return el asunto de la solicitud */
    public String getAsunto() { return asunto; }
    /** @param asunto el asunto a asignar */
    public void setAsunto(String asunto) { this.asunto = asunto; }

    /** @return la descripción de la solicitud */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion la descripción a asignar */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return la fecha propuesta para la tutoría */
    public Date getFechaPropuesta() { return fechaPropuesta; }
    /** @param fechaPropuesta la fecha propuesta a asignar */
    public void setFechaPropuesta(Date fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }

    /** @return la hora propuesta para la tutoría */
    public String getHoraPropuesta() { return horaPropuesta; }
    /** @param horaPropuesta la hora propuesta a asignar */
    public void setHoraPropuesta(String horaPropuesta) { this.horaPropuesta = horaPropuesta; }

    /** @return la duración en minutos de la tutoría solicitada */
    public Integer getDuracion() { return duracion; }
    /** @param duracion la duración a asignar */
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    /** @return el estatus de la solicitud */
    public String getEstatus() { return estatus; }
    /** @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }

    /** @return el identificador del tutor destinatario */
    public int getIdTutor() { return idTutor; }
    /** @param idTutor el identificador de tutor a asignar */
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    /** @return el nombre del tutor destinatario */
    public String getNombreTutor() { return nombreTutor; }
    /** @param nombreTutor el nombre de tutor a asignar */
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    /** @return el correo del tutor destinatario */
    public String getCorreoTutor() { return correoTutor; }
    /** @param correoTutor el correo a asignar */
    public void setCorreoTutor(String correoTutor) { this.correoTutor = correoTutor; }
}
