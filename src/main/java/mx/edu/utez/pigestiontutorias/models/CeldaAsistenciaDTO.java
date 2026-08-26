package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO que representa una celda individual de la matriz de asistencia grupal:
 * el estatus de un alumno específico en una sesión grupal específica.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-21
 */
public class CeldaAsistenciaDTO {
    private int idSesionGrupal;
    private String matricula;
    private String estatus;

    /**
     * Construye una celda de asistencia vacía.
     */
    public CeldaAsistenciaDTO() {}

    /**
     * Construye una celda de asistencia con los datos indicados.
     * @param idSesionGrupal el identificador de la sesión grupal
     * @param matricula la matrícula del alumno
     * @param estatus el estatus de asistencia del alumno en la sesión
     */
    public CeldaAsistenciaDTO(int idSesionGrupal, String matricula, String estatus) {
        this.idSesionGrupal = idSesionGrupal;
        this.matricula = matricula;
        this.estatus = estatus;
    }

    /** Obtiene el identificador de la sesión grupal. @return el identificador de la sesión */
    public int getIdSesionGrupal() { return idSesionGrupal; }
    /** Establece el identificador de la sesión grupal. @param idSesionGrupal el identificador a asignar */
    public void setIdSesionGrupal(int idSesionGrupal) { this.idSesionGrupal = idSesionGrupal; }

    /** Obtiene la matrícula del alumno. @return la matrícula del alumno */
    public String getMatricula() { return matricula; }
    /** Establece la matrícula del alumno. @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /** Obtiene el estatus de asistencia del alumno en la sesión. @return el estatus de asistencia */
    public String getEstatus() { return estatus; }
    /** Establece el estatus de asistencia del alumno en la sesión. @param estatus el estatus a asignar */
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
