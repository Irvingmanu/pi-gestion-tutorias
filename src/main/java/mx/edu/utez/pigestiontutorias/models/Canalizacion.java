package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * Entidad que representa la canalización de un alumno hacia un área de apoyo,
 * con su motivo, estatus, token de confirmación y datos enriquecidos del área
 * y motivo para su despliegue.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class Canalizacion {
    private int idCanalizacion;
    private int idArea;
    private Integer idMotivo;
    private String matricula;
    private Date fechaCanalizacion;
    private String estatus;
    private String observaciones;
    private String idToken;

    private String nombreArea;
    private String encargadoArea;
    private String correoContactoArea;
    private String enlaceCitaArea;
    private String nombreMotivo;

    /**
     * Construye una canalización vacía.
     */
    public Canalizacion() {}

    /** Obtiene el identificador de la canalización. @return el identificador de la canalización */
    public int getIdCanalizacion() { return idCanalizacion; }
    /** Establece el identificador de la canalización. @param idCanalizacion el identificador a asignar */
    public void setIdCanalizacion(int idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    /** Obtiene el identificador del área de destino. @return el identificador del área */
    public int getIdArea() { return idArea; }
    /** Establece el identificador del área de destino. @param idArea el identificador a asignar */
    public void setIdArea(int idArea) { this.idArea = idArea; }

    /** Obtiene el identificador del motivo de canalización. @return el identificador del motivo */
    public Integer getIdMotivo() { return idMotivo; }
    /** Establece el identificador del motivo de canalización. @param idMotivo el identificador a asignar */
    public void setIdMotivo(Integer idMotivo) { this.idMotivo = idMotivo; }

    /** Obtiene la matrícula del alumno canalizado. @return la matrícula del alumno */
    public String getMatricula() { return matricula; }
    /** Establece la matrícula del alumno canalizado. @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /** Obtiene la fecha de la canalización. @return la fecha de la canalización */
    public Date getFechaCanalizacion() { return fechaCanalizacion; }
    /** Establece la fecha de la canalización. @param fechaCanalizacion la fecha a asignar */
    public void setFechaCanalizacion(Date fechaCanalizacion) { this.fechaCanalizacion = fechaCanalizacion; }

    /** Obtiene el estatus de la canalización. @return el estatus de la canalización */
    public String getEstatus() { return estatus; }
    /** Establece el estatus de la canalización. @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }

    /** Obtiene las observaciones de la canalización. @return las observaciones */
    public String getObservaciones() { return observaciones; }
    /** Establece las observaciones de la canalización. @param observaciones las observaciones a asignar */
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    /** Obtiene el token de confirmación de la canalización. @return el token de confirmación */
    public String getIdToken() { return idToken; }
    /** Establece el token de confirmación de la canalización. @param idToken el token a asignar */
    public void setIdToken(String idToken) { this.idToken = idToken; }

    /** Obtiene el nombre del área de destino. @return el nombre del área */
    public String getNombreArea() { return nombreArea; }
    /** Establece el nombre del área de destino. @param nombreArea el nombre a asignar */
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    /** Obtiene el nombre del encargado del área. @return el encargado del área */
    public String getEncargadoArea() { return encargadoArea; }
    /** Establece el nombre del encargado del área. @param encargadoArea el encargado a asignar */
    public void setEncargadoArea(String encargadoArea) { this.encargadoArea = encargadoArea; }

    /** Obtiene el correo de contacto del área. @return el correo de contacto del área */
    public String getCorreoContactoArea() { return correoContactoArea; }
    /** Establece el correo de contacto del área. @param correoContactoArea el correo a asignar */
    public void setCorreoContactoArea(String correoContactoArea) { this.correoContactoArea = correoContactoArea; }

    /** Obtiene el enlace de cita del área. @return el enlace de cita del área */
    public String getEnlaceCitaArea() { return enlaceCitaArea; }
    /** Establece el enlace de cita del área. @param enlaceCitaArea el enlace a asignar */
    public void setEnlaceCitaArea(String enlaceCitaArea) { this.enlaceCitaArea = enlaceCitaArea; }

    /** Obtiene el nombre del motivo de canalización. @return el nombre del motivo */
    public String getNombreMotivo() { return nombreMotivo; }
    /** Establece el nombre del motivo de canalización. @param nombreMotivo el nombre a asignar */
    public void setNombreMotivo(String nombreMotivo) { this.nombreMotivo = nombreMotivo; }
}