package mx.edu.utez.pigestiontutorias.models;

public class CeldaAsistenciaDTO {
    private int idSesionGrupal;
    private String matricula;
    private String estatus;

    public CeldaAsistenciaDTO() {}

    public CeldaAsistenciaDTO(int idSesionGrupal, String matricula, String estatus) {
        this.idSesionGrupal = idSesionGrupal;
        this.matricula = matricula;
        this.estatus = estatus;
    }

    public int getIdSesionGrupal() { return idSesionGrupal; }
    public void setIdSesionGrupal(int idSesionGrupal) { this.idSesionGrupal = idSesionGrupal; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
