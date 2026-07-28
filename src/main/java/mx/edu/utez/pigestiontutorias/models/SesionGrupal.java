package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class SesionGrupal {
    private int idSesionGrupal;
    private int idLetraGrupo;
    private int idCarrera;
    private int idCuatrimestre;
    private Date fecha;
    private String acuerdos;
    private String asesoriasGrupales;
    private String temasTratados;

    public SesionGrupal() {}

    public SesionGrupal(int idLetraGrupo, int idCarrera, int idCuatrimestre, Date fecha, String acuerdos, String asesoriasGrupales, String temasTratados) {
        this.idLetraGrupo = idLetraGrupo;
        this.idCarrera = idCarrera;
        this.idCuatrimestre = idCuatrimestre;
        this.fecha = fecha;
        this.acuerdos = acuerdos;
        this.asesoriasGrupales = asesoriasGrupales;
        this.temasTratados = temasTratados;
    }

    // Genera aquí todos los Getters y Setters correspondientes
    public int getIdLetraGrupo() { return idLetraGrupo; }
    public void setIdLetraGrupo(int idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }
    public int getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }
    public String getAcuerdos() { return acuerdos; }
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }
    public String getAsesoriasGrupales() { return asesoriasGrupales; }
    public void setAsesoriasGrupales(String asesoriasGrupales) { this.asesoriasGrupales = asesoriasGrupales; }
    public String getTemasTratados() { return temasTratados; }
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }
}