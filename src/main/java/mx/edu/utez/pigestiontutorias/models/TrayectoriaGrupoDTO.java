package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * DTO que representa un renglón de la trayectoria académica de un alumno (un grupo
 * por el que pasó), incluyendo el rango de fechas de vigencia y el motivo de cambio.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-21
 */
public class TrayectoriaGrupoDTO {
    private String nombreCarrera;
    private String nivel;
    private int cuatrimestre;
    private String letra;
    private String generacion;
    private Date fechaInicio;
    private Date fechaFin;
    private String motivoCambio;

    /** @return el nombre de la carrera del grupo */
    public String getNombreCarrera() { return nombreCarrera; }
    /** @param nombreCarrera el nombre de carrera a asignar */
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    /** @return el nivel académico de la carrera (TSU o ING) */
    public String getNivel() { return nivel; }
    /** @param nivel el nivel a asignar */
    public void setNivel(String nivel) { this.nivel = nivel; }

    /** @return el cuatrimestre del grupo */
    public int getCuatrimestre() { return cuatrimestre; }
    /** @param cuatrimestre el cuatrimestre a asignar */
    public void setCuatrimestre(int cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    /** @return la letra del grupo */
    public String getLetra() { return letra; }
    /** @param letra la letra a asignar */
    public void setLetra(String letra) { this.letra = letra; }

    /** @return la generación del grupo */
    public String getGeneracion() { return generacion; }
    /** @param generacion la generación a asignar */
    public void setGeneracion(String generacion) { this.generacion = generacion; }

    /** @return la fecha de inicio de vigencia en este grupo */
    public Date getFechaInicio() { return fechaInicio; }
    /** @param fechaInicio la fecha de inicio a asignar */
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    /** @return la fecha de fin de vigencia en este grupo, o {@code null} si sigue vigente */
    public Date getFechaFin() { return fechaFin; }
    /** @param fechaFin la fecha de fin a asignar */
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    /** @return el motivo del cambio de grupo */
    public String getMotivoCambio() { return motivoCambio; }
    /** @param motivoCambio el motivo de cambio a asignar */
    public void setMotivoCambio(String motivoCambio) { this.motivoCambio = motivoCambio; }
}
