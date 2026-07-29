package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;
import java.sql.Timestamp;

public class Solicitud {

    private int idSolicitud;
    private String matricula;
    private int idTutor;
    private Integer idHorario;      // puede ser NULL
    private String asunto;
    private String descripcion;
    private String estatus;         // Pendiente, Confirmada, Rechazada, Reprogramada
    private Date fechaPropuesta;
    private Date fechaRespuesta;
    private Date nuevaFecha;
    private String nuevaHora;       // hora de la contrapropuesta del tutor, ej. "13:00"
    private Integer duracion;       // horas: 1 o 2
    private String horaPropuesta;   // ej. "13:00"
    private Timestamp fechaRegistro; // se llena sola por defecto en la BD

    // Campos extra que NO existen en la tabla, solo para mostrar datos
    // del alumno en la lista de solicitudes del tutor (se llenan con un JOIN)
    private String nombreAlumno;
    private String apellidosAlumno;

    public Solicitud() {
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public int getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    public Integer getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Integer idHorario) {
        this.idHorario = idHorario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public Date getFechaPropuesta() {
        return fechaPropuesta;
    }

    public void setFechaPropuesta(Date fechaPropuesta) {
        this.fechaPropuesta = fechaPropuesta;
    }

    public Date getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Date fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public Date getNuevaFecha() {
        return nuevaFecha;
    }

    public void setNuevaFecha(Date nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public String getNuevaHora() {
        return nuevaHora;
    }

    public void setNuevaHora(String nuevaHora) {
        this.nuevaHora = nuevaHora;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getHoraPropuesta() {
        return horaPropuesta;
    }

    public void setHoraPropuesta(String horaPropuesta) {
        this.horaPropuesta = horaPropuesta;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public String getApellidosAlumno() {
        return apellidosAlumno;
    }

    public void setApellidosAlumno(String apellidosAlumno) {
        this.apellidosAlumno = apellidosAlumno;
    }
}