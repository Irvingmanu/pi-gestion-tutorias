package mx.edu.utez.pigestiontutorias.models;

import java.util.Map;

public class AsistenciaFilaDTO {
    private String matricula;
    private String nombreCompleto;
    private Map<Integer, String> estatusPorSesion;
    private int totalSesiones;
    private int totalPresentes;
    private int totalJustificadas;
    private double porcentaje;
    private boolean riesgo;

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Map<Integer, String> getEstatusPorSesion() { return estatusPorSesion; }
    public void setEstatusPorSesion(Map<Integer, String> estatusPorSesion) { this.estatusPorSesion = estatusPorSesion; }

    public int getTotalSesiones() { return totalSesiones; }
    public void setTotalSesiones(int totalSesiones) { this.totalSesiones = totalSesiones; }

    public int getTotalPresentes() { return totalPresentes; }
    public void setTotalPresentes(int totalPresentes) { this.totalPresentes = totalPresentes; }

    public int getTotalJustificadas() { return totalJustificadas; }
    public void setTotalJustificadas(int totalJustificadas) { this.totalJustificadas = totalJustificadas; }

    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }

    public boolean isRiesgo() { return riesgo; }
    public void setRiesgo(boolean riesgo) { this.riesgo = riesgo; }
}
