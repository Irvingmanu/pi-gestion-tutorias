package mx.edu.utez.pigestiontutorias.models;

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
