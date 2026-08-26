package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO ligero usado en el autocompletado del buscador de alumnos, con la
 * matrícula, el nombre completo y el grupo asignado del alumno.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
public class AlumnoBusquedaDTO {
    private String matricula;
    private String nombreCompleto;
    private String grupoAsignado;

    /**
     * Construye un DTO de búsqueda de alumno vacío.
     */
    public AlumnoBusquedaDTO() {}

    /**
     * Construye un DTO de búsqueda de alumno con los datos indicados.
     * @param matricula la matrícula del alumno
     * @param nombreCompleto el nombre completo del alumno
     * @param grupoAsignado el nombre del grupo asignado al alumno
     */
    public AlumnoBusquedaDTO(String matricula, String nombreCompleto, String grupoAsignado) {
        this.matricula = matricula;
        this.nombreCompleto = nombreCompleto;
        this.grupoAsignado = grupoAsignado;
    }

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
     * Obtiene el nombre del grupo asignado al alumno.
     * @return el grupo asignado
     */
    public String getGrupoAsignado() { return grupoAsignado; }
    /**
     * Establece el nombre del grupo asignado al alumno.
     * @param grupoAsignado el grupo a asignar
     */
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }
}
