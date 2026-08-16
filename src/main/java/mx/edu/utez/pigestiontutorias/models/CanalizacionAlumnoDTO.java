package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

// Fila del modal "Alumnos Canalizados" del reporte del coordinador (ver
// ReportesGlobalesServlet/CanalizacionDao.getCanalizacionesDetalladas). Combina CANALIZACION
// con el alumno/grupo canalizado y el tutor que la registro (via SESION_INDIVIDUAL).
public class CanalizacionAlumnoDTO {
    private int idCanalizacion;
    private String matricula;
    private String nombreAlumno;
    private String grupoAsignado;
    private Date fechaCanalizacion;
    private String nombreTutor;
    private String nombreArea;
    private String nombreMotivo;
    private String estatus;
    private String observaciones;

    public int getIdCanalizacion() { return idCanalizacion; }
    public void setIdCanalizacion(int idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    public Date getFechaCanalizacion() { return fechaCanalizacion; }
    public void setFechaCanalizacion(Date fechaCanalizacion) { this.fechaCanalizacion = fechaCanalizacion; }

    public String getNombreTutor() { return nombreTutor; }
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    public String getNombreArea() { return nombreArea; }
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    public String getNombreMotivo() { return nombreMotivo; }
    public void setNombreMotivo(String nombreMotivo) { this.nombreMotivo = nombreMotivo; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
