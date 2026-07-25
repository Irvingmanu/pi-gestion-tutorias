package mx.edu.utez.pigestiontutorias.models;

public class Motivo {
    private int idMotivo;
    private int idArea;
    private String nombreMotivo;

    public Motivo() {
    }

    public Motivo(int idMotivo, int idArea, String nombreMotivo) {
        this.idMotivo = idMotivo;
        this.idArea = idArea;
        this.nombreMotivo = nombreMotivo;
    }

    public int getIdMotivo() {
        return idMotivo;
    }

    public void setIdMotivo(int idMotivo) {
        this.idMotivo = idMotivo;
    }

    public int getIdArea() {
        return idArea;
    }

    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    public String getNombreMotivo() {
        return nombreMotivo;
    }

    public void setNombreMotivo(String nombreMotivo) {
        this.nombreMotivo = nombreMotivo;
    }
}
