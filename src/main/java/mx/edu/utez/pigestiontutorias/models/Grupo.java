package mx.edu.utez.pigestiontutorias.models;

// Representa un GRUPO real (Carrera + Cuatrimestre + Letra + Periodo), que ahora es
// una tabla propia en BD en vez de resolverse cruzando 3 catalogos (CARRERA, CUATRIMESTRE,
// LETRA_GRUPO ya no existen: CUATRIMESTRE y LETRA son columnas directas de GRUPO).
public class Grupo {
    private int idGrupo;
    private int idCarrera;
    private int cuatrimestre;
    private String letra;
    private int idPeriodo;
    private String generacion;
    private String estado;

    // Resueltos via JOIN, no son columnas propias de GRUPO
    private String nombreCarrera;
    private String nombreGrupo;
    private int idAcademia;

    public Grupo() {
    }

    public Grupo(int idGrupo, String nombreGrupo) {
        this.idGrupo = idGrupo;
        this.nombreGrupo = nombreGrupo;
    }

    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    public int getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(int cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public String getLetra() { return letra; }
    public void setLetra(String letra) { this.letra = letra; }

    public int getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    public String getGeneracion() { return generacion; }
    public void setGeneracion(String generacion) { this.generacion = generacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public int getIdAcademia() { return idAcademia; }
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }
}
