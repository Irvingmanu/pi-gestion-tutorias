package mx.edu.utez.pigestiontutorias.models;

public class AsignacionTutor {
    private int idAsignacion;
    private int idTutor;
    private int idCarrera;
    private int idLetraGrupo;
    private int idCuatrimestre;
    private int idPeriodo;
    private int activo;

    // Campos extra que NO existen en la tabla, solo para mostrar datos
    // del tutor/carrera/grupo/cuatrimestre/periodo en la lista (se llenan con un JOIN)
    private String nombresTutor;
    private String apellidosTutor;
    private String nombreCarrera;
    private String letraGrupo;
    private int numeroCuatrimestre;
    private String nombrePeriodo;

    public AsignacionTutor() {}

    public AsignacionTutor(int idTutor, int idCarrera, int idLetraGrupo, int idCuatrimestre, int idPeriodo) {
        this.idTutor = idTutor;
        this.idCarrera = idCarrera;
        this.idLetraGrupo = idLetraGrupo;
        this.idCuatrimestre = idCuatrimestre;
        this.idPeriodo = idPeriodo;
        this.activo = 1; // Por defecto activo al crear
    }

    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int idAsignacion) { this.idAsignacion = idAsignacion; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    public int getIdLetraGrupo() { return idLetraGrupo; }
    public void setIdLetraGrupo(int idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }

    public int getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    public int getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }

    public String getNombresTutor() { return nombresTutor; }
    public void setNombresTutor(String nombresTutor) { this.nombresTutor = nombresTutor; }

    public String getApellidosTutor() { return apellidosTutor; }
    public void setApellidosTutor(String apellidosTutor) { this.apellidosTutor = apellidosTutor; }

    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    public String getLetraGrupo() { return letraGrupo; }
    public void setLetraGrupo(String letraGrupo) { this.letraGrupo = letraGrupo; }

    public int getNumeroCuatrimestre() { return numeroCuatrimestre; }
    public void setNumeroCuatrimestre(int numeroCuatrimestre) { this.numeroCuatrimestre = numeroCuatrimestre; }

    public String getNombrePeriodo() { return nombrePeriodo; }
    public void setNombrePeriodo(String nombrePeriodo) { this.nombrePeriodo = nombrePeriodo; }
}