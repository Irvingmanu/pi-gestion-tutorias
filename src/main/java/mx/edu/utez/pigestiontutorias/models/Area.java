package mx.edu.utez.pigestiontutorias.models;

import java.util.List;

public class Area {
    private int idArea;
    private String nombre;
    private String encargado;
    private String correoContacto;
    private List<Motivo> motivos;

    public Area() {
    }

    public Area(int idArea, String nombre, String encargado, String correoContacto) {
        this.idArea = idArea;
        this.nombre = nombre;
        this.encargado = encargado;
        this.correoContacto = correoContacto;
    }

    public int getIdArea() {
        return idArea;
    }

    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEncargado() {
        return encargado;
    }

    public void setEncargado(String encargado) {
        this.encargado = encargado;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    @Override
    public String toString() {
        return idArea + ',' + nombre + ',' + encargado + ',' + correoContacto;
    }
}
