package mx.edu.utez.pigestiontutorias.models;

// Fila liviana del buscador de alumnos (tarjeta de busqueda en Reportes): solo lo
// necesario para pintar la lista de resultados y despues pedir el reporte individual
// completo por matricula (ver AlumnoDAO.buscarAlumnos).
public class AlumnoBusquedaDTO {
    private String matricula;
    private String nombreCompleto;
    private String grupoAsignado;

    public AlumnoBusquedaDTO() {}

    public AlumnoBusquedaDTO(String matricula, String nombreCompleto, String grupoAsignado) {
        this.matricula = matricula;
        this.nombreCompleto = nombreCompleto;
        this.grupoAsignado = grupoAsignado;
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }
}
