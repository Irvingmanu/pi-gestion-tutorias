package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Representa la solicitud de un alumno para agendar una tutoría individual con un
 * tutor, incluyendo la propuesta original y la posible reprogramación de fecha/hora.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class Solicitud {

    private int idSolicitud;
    private String matricula;
    private int idTutor;
    private Integer idHorario;
    private String asunto;
    private String descripcion;
    private String estatus;
    private Date fechaPropuesta;
    private Date fechaRespuesta;
    private Date nuevaFecha;
    private String nuevaHora;
    private Integer duracion;
    private String horaPropuesta;
    private Timestamp fechaRegistro;

    private String nombreAlumno;
    private String apellidosAlumno;

    /**
     * Construye una solicitud vacía.
     */
    public Solicitud() {
    }

    /** @return el identificador de la solicitud */
    public int getIdSolicitud() {
        return idSolicitud;
    }

    /** @param idSolicitud el identificador a asignar */
    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /** @return la matrícula del alumno solicitante */
    public String getMatricula() {
        return matricula;
    }

    /** @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    /** @return el identificador del tutor solicitado */
    public int getIdTutor() {
        return idTutor;
    }

    /** @param idTutor el identificador de tutor a asignar */
    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    /** @return el identificador del horario solicitado */
    public Integer getIdHorario() {
        return idHorario;
    }

    /** @param idHorario el identificador de horario a asignar */
    public void setIdHorario(Integer idHorario) {
        this.idHorario = idHorario;
    }

    /** @return el asunto de la solicitud */
    public String getAsunto() {
        return asunto;
    }

    /** @param asunto el asunto a asignar */
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    /** @return la descripción de la solicitud */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion la descripción a asignar */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return el estatus de la solicitud */
    public String getEstatus() {
        return estatus;
    }

    /** @param estatus el estatus a asignar */
    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    /** @return la fecha propuesta originalmente por el alumno */
    public Date getFechaPropuesta() {
        return fechaPropuesta;
    }

    /** @param fechaPropuesta la fecha propuesta a asignar */
    public void setFechaPropuesta(Date fechaPropuesta) {
        this.fechaPropuesta = fechaPropuesta;
    }

    /** @return la fecha en que el tutor respondió la solicitud */
    public Date getFechaRespuesta() {
        return fechaRespuesta;
    }

    /** @param fechaRespuesta la fecha de respuesta a asignar */
    public void setFechaRespuesta(Date fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    /** @return la nueva fecha propuesta por el tutor al reprogramar */
    public Date getNuevaFecha() {
        return nuevaFecha;
    }

    /** @param nuevaFecha la nueva fecha a asignar */
    public void setNuevaFecha(Date nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    /** @return la nueva hora propuesta por el tutor al reprogramar */
    public String getNuevaHora() {
        return nuevaHora;
    }

    /** @param nuevaHora la nueva hora a asignar */
    public void setNuevaHora(String nuevaHora) {
        this.nuevaHora = nuevaHora;
    }

    /** @return la duración en minutos de la tutoría solicitada */
    public Integer getDuracion() {
        return duracion;
    }

    /** @param duracion la duración a asignar */
    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    /** @return la hora propuesta originalmente por el alumno */
    public String getHoraPropuesta() {
        return horaPropuesta;
    }

    /** @param horaPropuesta la hora propuesta a asignar */
    public void setHoraPropuesta(String horaPropuesta) {
        this.horaPropuesta = horaPropuesta;
    }

    /** @return la fecha y hora de registro de la solicitud */
    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    /** @param fechaRegistro la fecha de registro a asignar */
    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /** @return el nombre del alumno solicitante */
    public String getNombreAlumno() {
        return nombreAlumno;
    }

    /** @param nombreAlumno el nombre a asignar */
    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    /** @return los apellidos del alumno solicitante */
    public String getApellidosAlumno() {
        return apellidosAlumno;
    }

    /** @param apellidosAlumno los apellidos a asignar */
    public void setApellidosAlumno(String apellidosAlumno) {
        this.apellidosAlumno = apellidosAlumno;
    }
}