package mx.edu.utez.pigestiontutorias.models;

public class Canalizacion {
    private int idCanalizacion;
    private int idArea;
    private String matricula;
    private String estatus;
    private String observaciones;

    public Canalizacion() {}

    public int getIdCanalizacion() { return idCanalizacion; }
    public void setIdCanalizacion(int idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    public int getIdArea() { return idArea; }
    public void setIdArea(int idArea) { this.idArea = idArea; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}