package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO con los datos necesarios para enviar el correo de recordatorio al área
 * de apoyo sobre una canalización que sigue "En proceso".
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
public class CanalizacionRecordatorioDTO {
    private String nombreArea;
    private String correoContactoArea;
    private String encargadoArea;
    private String nombreAlumno;
    private String matricula;
    private String motivoODetalle;
    private String idToken;
    private String estatus;

    /** Obtiene el nombre del área de destino. @return el nombre del área */
    public String getNombreArea() { return nombreArea; }
    /** Establece el nombre del área de destino. @param nombreArea el nombre a asignar */
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    /** Obtiene el correo de contacto del área. @return el correo de contacto del área */
    public String getCorreoContactoArea() { return correoContactoArea; }
    /** Establece el correo de contacto del área. @param correoContactoArea el correo a asignar */
    public void setCorreoContactoArea(String correoContactoArea) { this.correoContactoArea = correoContactoArea; }

    /** Obtiene el nombre del encargado del área. @return el encargado del área */
    public String getEncargadoArea() { return encargadoArea; }
    /** Establece el nombre del encargado del área. @param encargadoArea el encargado a asignar */
    public void setEncargadoArea(String encargadoArea) { this.encargadoArea = encargadoArea; }

    /** Obtiene el nombre del alumno canalizado. @return el nombre del alumno */
    public String getNombreAlumno() { return nombreAlumno; }
    /** Establece el nombre del alumno canalizado. @param nombreAlumno el nombre a asignar */
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    /** Obtiene la matrícula del alumno canalizado. @return la matrícula del alumno */
    public String getMatricula() { return matricula; }
    /** Establece la matrícula del alumno canalizado. @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /** Obtiene el motivo o detalle de la canalización. @return el motivo o detalle */
    public String getMotivoODetalle() { return motivoODetalle; }
    /** Establece el motivo o detalle de la canalización. @param motivoODetalle el motivo a asignar */
    public void setMotivoODetalle(String motivoODetalle) { this.motivoODetalle = motivoODetalle; }

    /** Obtiene el token de confirmación de la canalización. @return el token de confirmación */
    public String getIdToken() { return idToken; }
    /** Establece el token de confirmación de la canalización. @param idToken el token a asignar */
    public void setIdToken(String idToken) { this.idToken = idToken; }

    /** Obtiene el estatus de la canalización. @return el estatus de la canalización */
    public String getEstatus() { return estatus; }
    /** Establece el estatus de la canalización. @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
