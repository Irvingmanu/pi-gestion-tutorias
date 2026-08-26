package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * DTO que representa una canalización de un alumno, con datos enriquecidos
 * del alumno, tutor, área y motivo, usado en los listados de canalizaciones.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-16
 */
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

    /** Obtiene el identificador de la canalización. @return el identificador de la canalización */
    public int getIdCanalizacion() { return idCanalizacion; }
    /** Establece el identificador de la canalización. @param idCanalizacion el identificador a asignar */
    public void setIdCanalizacion(int idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    /** Obtiene la matrícula del alumno canalizado. @return la matrícula del alumno */
    public String getMatricula() { return matricula; }
    /** Establece la matrícula del alumno canalizado. @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /** Obtiene el nombre del alumno canalizado. @return el nombre del alumno */
    public String getNombreAlumno() { return nombreAlumno; }
    /** Establece el nombre del alumno canalizado. @param nombreAlumno el nombre a asignar */
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    /** Obtiene el grupo asignado del alumno. @return el grupo asignado */
    public String getGrupoAsignado() { return grupoAsignado; }
    /** Establece el grupo asignado del alumno. @param grupoAsignado el grupo a asignar */
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    /** Obtiene la fecha de la canalización. @return la fecha de la canalización */
    public Date getFechaCanalizacion() { return fechaCanalizacion; }
    /** Establece la fecha de la canalización. @param fechaCanalizacion la fecha a asignar */
    public void setFechaCanalizacion(Date fechaCanalizacion) { this.fechaCanalizacion = fechaCanalizacion; }

    /** Obtiene el nombre del tutor que canalizó al alumno. @return el nombre del tutor */
    public String getNombreTutor() { return nombreTutor; }
    /** Establece el nombre del tutor que canalizó al alumno. @param nombreTutor el nombre a asignar */
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    /** Obtiene el nombre del área de destino. @return el nombre del área */
    public String getNombreArea() { return nombreArea; }
    /** Establece el nombre del área de destino. @param nombreArea el nombre a asignar */
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    /** Obtiene el nombre del motivo de canalización. @return el nombre del motivo */
    public String getNombreMotivo() { return nombreMotivo; }
    /** Establece el nombre del motivo de canalización. @param nombreMotivo el nombre a asignar */
    public void setNombreMotivo(String nombreMotivo) { this.nombreMotivo = nombreMotivo; }

    /** Obtiene el estatus de la canalización. @return el estatus de la canalización */
    public String getEstatus() { return estatus; }
    /** Establece el estatus de la canalización. @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }

    /** Obtiene las observaciones de la canalización. @return las observaciones */
    public String getObservaciones() { return observaciones; }
    /** Establece las observaciones de la canalización. @param observaciones las observaciones a asignar */
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
