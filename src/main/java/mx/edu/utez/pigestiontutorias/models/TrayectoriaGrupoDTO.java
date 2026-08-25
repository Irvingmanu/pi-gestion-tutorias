package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class TrayectoriaGrupoDTO {
    private String nombreCarrera;
    private String nivel;
    private int cuatrimestre;
    private String letra;
    private String generacion;
    private Date fechaInicio;
    private Date fechaFin;
    private String motivoCambio;

    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public int getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(int cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public String getLetra() { return letra; }
    public void setLetra(String letra) { this.letra = letra; }

    public String getGeneracion() { return generacion; }
    public void setGeneracion(String generacion) { this.generacion = generacion; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public String getMotivoCambio() { return motivoCambio; }
    public void setMotivoCambio(String motivoCambio) { this.motivoCambio = motivoCambio; }
}
