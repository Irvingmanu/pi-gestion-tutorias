package mx.edu.utez.pigestiontutorias.models;

// Transporte servlet -> DAO de una celda editada en la cuadricula de "Pase de Lista"
// (una sesion grupal x un alumno) al guardar en lote.
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
