package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class PeriodoEscolar {
    private int idPeriodo;
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado;
    private int asistenciasGrupales;

    public PeriodoEscolar() {}

    public int getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getAsistenciasGrupales() { return asistenciasGrupales; }
    public void setAsistenciasGrupales(int asistenciasGrupales) { this.asistenciasGrupales = asistenciasGrupales; }
}
