package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO que representa la asistencia de un alumno a una sesión grupal específica.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-17
 */
public class AsistenciaDetalleDTO {
    private String matricula;
    private String nombreCompleto;
    private String estatusAsistencia;

    /**
     * Obtiene la matrícula del alumno.
     * @return la matrícula del alumno
     */
    public String getMatricula() { return matricula; }
    /**
     * Establece la matrícula del alumno.
     * @param matricula la matrícula a asignar
     */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /**
     * Obtiene el nombre completo del alumno.
     * @return el nombre completo del alumno
     */
    public String getNombreCompleto() { return nombreCompleto; }
    /**
     * Establece el nombre completo del alumno.
     * @param nombreCompleto el nombre completo a asignar
     */
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    /**
     * Obtiene el estatus de asistencia del alumno en la sesión.
     * @return el estatus de asistencia
     */
    public String getEstatusAsistencia() { return estatusAsistencia; }
    /**
     * Establece el estatus de asistencia del alumno en la sesión.
     * @param estatusAsistencia el estatus a asignar
     */
    public void setEstatusAsistencia(String estatusAsistencia) { this.estatusAsistencia = estatusAsistencia; }
}
