package mx.edu.utez.pigestiontutorias.models;

/**
 * Representa un grupo académico (carrera, cuatrimestre, letra y periodo escolar)
 * al que pertenecen los alumnos y al que se asigna un tutor.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class Grupo {
    private int idGrupo;
    private int idCarrera;
    private int cuatrimestre;
    private String letra;
    private int idPeriodo;
    private String generacion;
    private String estado;

    private String nombreCarrera;
    private String nombreGrupo;
    private int idAcademia;

    /**
     * Construye un grupo vacío.
     */
    public Grupo() {
    }

    /**
     * Construye un grupo indicando su identificador y su nombre para mostrar.
     * @param idGrupo el identificador del grupo
     * @param nombreGrupo el nombre del grupo a mostrar
     */
    public Grupo(int idGrupo, String nombreGrupo) {
        this.idGrupo = idGrupo;
        this.nombreGrupo = nombreGrupo;
    }

    /** @return el identificador del grupo */
    public int getIdGrupo() { return idGrupo; }
    /** @param idGrupo el identificador a asignar */
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    /** @return el identificador de la carrera del grupo */
    public int getIdCarrera() { return idCarrera; }
    /** @param idCarrera el identificador de carrera a asignar */
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    /** @return el cuatrimestre del grupo */
    public int getCuatrimestre() { return cuatrimestre; }
    /** @param cuatrimestre el cuatrimestre a asignar */
    public void setCuatrimestre(int cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    /** @return la letra del grupo */
    public String getLetra() { return letra; }
    /** @param letra la letra a asignar */
    public void setLetra(String letra) { this.letra = letra; }

    /** @return el identificador del periodo escolar del grupo */
    public int getIdPeriodo() { return idPeriodo; }
    /** @param idPeriodo el identificador de periodo a asignar */
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    /** @return la generación del grupo */
    public String getGeneracion() { return generacion; }
    /** @param generacion la generación a asignar */
    public void setGeneracion(String generacion) { this.generacion = generacion; }

    /** @return el estado del grupo */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return el nombre de la carrera del grupo */
    public String getNombreCarrera() { return nombreCarrera; }
    /** @param nombreCarrera el nombre de carrera a asignar */
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    /** @return el nombre para mostrar del grupo */
    public String getNombreGrupo() { return nombreGrupo; }
    /** @param nombreGrupo el nombre a asignar */
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    /** @return el identificador de la academia del grupo */
    public int getIdAcademia() { return idAcademia; }
    /** @param idAcademia el identificador de academia a asignar */
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }
}
