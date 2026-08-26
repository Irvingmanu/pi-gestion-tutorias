package mx.edu.utez.pigestiontutorias.models;

import java.util.Map;

/**
 * DTO que representa una fila del reporte de asistencia grupal de un alumno,
 * con su estatus por cada sesión y los totales/porcentaje calculados para
 * determinar si está en riesgo de baja por inasistencias.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-21
 */
public class AsistenciaFilaDTO {
    private String matricula;
    private String nombreCompleto;
    private Map<Integer, String> estatusPorSesion;
    private int totalSesiones;
    private int totalPresentes;
    private int totalJustificadas;
    private double porcentaje;
    private boolean riesgo;

    /**
     * Obtiene la matrícula del alumno.
     * @return la matrícula del alumno
     */
    public String getMatricula() { return matricula; }
    /**
     * Establece la matrícula del alumno.
     * @param matricula la matrícula a asignar
     */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /**
     * Obtiene el nombre completo del alumno.
     * @return el nombre completo del alumno
     */
    public String getNombreCompleto() { return nombreCompleto; }
    /**
     * Establece el nombre completo del alumno.
     * @param nombreCompleto el nombre completo a asignar
     */
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    /**
     * Obtiene el mapa de estatus de asistencia por identificador de sesión.
     * @return el mapa de estatus por sesión
     */
    public Map<Integer, String> getEstatusPorSesion() { return estatusPorSesion; }
    /**
     * Establece el mapa de estatus de asistencia por identificador de sesión.
     * @param estatusPorSesion el mapa a asignar
     */
    public void setEstatusPorSesion(Map<Integer, String> estatusPorSesion) { this.estatusPorSesion = estatusPorSesion; }

    /**
     * Obtiene el total de sesiones consideradas.
     * @return el total de sesiones
     */
    public int getTotalSesiones() { return totalSesiones; }
    /**
     * Establece el total de sesiones consideradas.
     * @param totalSesiones el total a asignar
     */
    public void setTotalSesiones(int totalSesiones) { this.totalSesiones = totalSesiones; }

    /**
     * Obtiene el total de sesiones en las que el alumno estuvo presente.
     * @return el total de sesiones presentes
     */
    public int getTotalPresentes() { return totalPresentes; }
    /**
     * Establece el total de sesiones en las que el alumno estuvo presente.
     * @param totalPresentes el total a asignar
     */
    public void setTotalPresentes(int totalPresentes) { this.totalPresentes = totalPresentes; }

    /**
     * Obtiene el total de faltas justificadas del alumno.
     * @return el total de faltas justificadas
     */
    public int getTotalJustificadas() { return totalJustificadas; }
    /**
     * Establece el total de faltas justificadas del alumno.
     * @param totalJustificadas el total a asignar
     */
    public void setTotalJustificadas(int totalJustificadas) { this.totalJustificadas = totalJustificadas; }

    /**
     * Obtiene el porcentaje de asistencia calculado del alumno.
     * @return el porcentaje de asistencia
     */
    public double getPorcentaje() { return porcentaje; }
    /**
     * Establece el porcentaje de asistencia calculado del alumno.
     * @param porcentaje el porcentaje a asignar
     */
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }

    /**
     * Indica si el alumno está en riesgo por inasistencias.
     * @return {@code true} si el alumno está en riesgo; {@code false} en caso contrario
     */
    public boolean isRiesgo() { return riesgo; }
    /**
     * Establece si el alumno está en riesgo por inasistencias.
     * @param riesgo {@code true} para marcarlo en riesgo
     */
    public void setRiesgo(boolean riesgo) { this.riesgo = riesgo; }
}
