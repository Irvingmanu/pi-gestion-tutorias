package mx.edu.utez.pigestiontutorias.models;

public class AvanceTutorGrupal {
    private int idTutor;
    private String nombreTutor;
    private String correoTutor;
    private int idGrupo;
    private String grupoAsignado;
    private int realizadas;
    private int objetivo;
    private String estatus;

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getNombreTutor() { return nombreTutor; }
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    public String getCorreoTutor() { return correoTutor; }
    public void setCorreoTutor(String correoTutor) { this.correoTutor = correoTutor; }

    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    public int getRealizadas() { return realizadas; }
    public void setRealizadas(int realizadas) { this.realizadas = realizadas; }

    public int getObjetivo() { return objetivo; }
    public void setObjetivo(int objetivo) { this.objetivo = objetivo; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
