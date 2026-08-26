package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * Representa un periodo escolar del sistema, con su rango de fechas, estado y el
 * objetivo de asistencias grupales configurado para dicho periodo.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-10
 */
public class PeriodoEscolar {
    private int idPeriodo;
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado;
    private int asistenciasGrupales;

    /**
     * Construye un periodo escolar vacío.
     */
    public PeriodoEscolar() {}

    /** @return el identificador del periodo escolar */
    public int getIdPeriodo() { return idPeriodo; }
    /** @param idPeriodo el identificador a asignar */
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    /** @return el nombre del periodo escolar */
    public String getNombre() { return nombre; }
    /** @param nombre el nombre a asignar */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return la fecha de inicio del periodo */
    public Date getFechaInicio() { return fechaInicio; }
    /** @param fechaInicio la fecha de inicio a asignar */
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    /** @return la fecha de fin del periodo */
    public Date getFechaFin() { return fechaFin; }
    /** @param fechaFin la fecha de fin a asignar */
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    /** @return el estado del periodo escolar */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return el objetivo de asistencias grupales configurado para el periodo */
    public int getAsistenciasGrupales() { return asistenciasGrupales; }
    /** @param asistenciasGrupales el objetivo de asistencias grupales a asignar */
    public void setAsistenciasGrupales(int asistenciasGrupales) { this.asistenciasGrupales = asistenciasGrupales; }
}
