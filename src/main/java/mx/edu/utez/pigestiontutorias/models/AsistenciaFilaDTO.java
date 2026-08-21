package mx.edu.utez.pigestiontutorias.models;

import java.util.Map;

// Una fila de la cuadricula de "Pase de Lista" (una por alumno): su estatus en cada
// SESION_GRUPAL del periodo (columna) mas los totales ya calculados para pintar la fila.
public class AsistenciaFilaDTO {
    private String matricula;
    private String nombreCompleto;
    private Map<Integer, String> estatusPorSesion; // ID_SESION_GRUPAL -> Presente/Falta/Justificado
    private int totalSesiones;
    private int totalPresentes;
    private int totalJustificadas;
    private double porcentaje;
    private boolean riesgo; // true si porcentaje < 80

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
