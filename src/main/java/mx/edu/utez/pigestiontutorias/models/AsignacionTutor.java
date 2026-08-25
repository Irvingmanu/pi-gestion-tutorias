package mx.edu.utez.pigestiontutorias.models;

public class AsignacionTutor {
    private int idAsignacion;
    private int idTutor;
    private int idGrupo;
    private String estado;

    private String nombresTutor;
    private String apellidosTutor;
    private String nombreGrupo;
    private int idAcademia;

    public AsignacionTutor() {}

    public AsignacionTutor(int idTutor, int idGrupo) {
        this.idTutor = idTutor;
        this.idGrupo = idGrupo;
        this.estado = "S";
    }

    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int idAsignacion) { this.idAsignacion = idAsignacion; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombresTutor() { return nombresTutor; }
    public void setNombresTutor(String nombresTutor) { this.nombresTutor = nombresTutor; }

    public String getApellidosTutor() { return apellidosTutor; }
    public void setApellidosTutor(String apellidosTutor) { this.apellidosTutor = apellidosTutor; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public int getIdAcademia() { return idAcademia; }
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }
}
