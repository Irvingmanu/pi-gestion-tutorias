package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

// Fila del modal "Solicitudes Pendientes" del reporte del coordinador (ver
// ReportesGlobalesServlet/SolicitudDao.getSolicitudesPendientesGlobal). Combina SOLICITUD_TUTORIA
// (ESTATUS = 'Pendiente') con el alumno/grupo que la creo y el tutor al que va dirigida, para
// poder listarla y, desde el detalle, mandarle un recordatorio por correo a ese tutor.
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

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(Date fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }

    public String getHoraPropuesta() { return horaPropuesta; }
    public void setHoraPropuesta(String horaPropuesta) { this.horaPropuesta = horaPropuesta; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getNombreTutor() { return nombreTutor; }
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    public String getCorreoTutor() { return correoTutor; }
    public void setCorreoTutor(String correoTutor) { this.correoTutor = correoTutor; }
}
