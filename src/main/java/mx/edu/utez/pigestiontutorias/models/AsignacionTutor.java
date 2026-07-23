package mx.edu.utez.pigestiontutorias.models;

public class AsignacionTutor {
    private int idAsignacion;
    private int idTutor;
    private int idLetraGrupo;
    private int idCuatrimestre;
    private int activo;

    public AsignacionTutor() {}

    public AsignacionTutor(int idTutor, int idLetraGrupo, int idCuatrimestre) {
        this.idTutor = idTutor;
        this.idLetraGrupo = idLetraGrupo;
        this.idCuatrimestre = idCuatrimestre;
        this.activo = 1; // Por defecto activo al crear
    }

    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int idAsignacion) { this.idAsignacion = idAsignacion; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public int getIdLetraGrupo() { return idLetraGrupo; }
    public void setIdLetraGrupo(int idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }

    public int getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }
}