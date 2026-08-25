package mx.edu.utez.pigestiontutorias.models;

import java.util.List;

public class Area {
    private int idArea;
    private String nombre;
    private String nombresEncargado;
    private String apellidoPaternoEncargado;
    private String apellidoMaternoEncargado;
    private String correoContacto;
    private String enlaceCita;
    private List<Motivo> motivos;
    private int alumnosCanalizados;

    public Area() {
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

    public String getNombresEncargado() { return nombresEncargado; }
    public void setNombresEncargado(String nombresEncargado) { this.nombresEncargado = nombresEncargado; }

    public String getApellidoPaternoEncargado() { return apellidoPaternoEncargado; }
    public void setApellidoPaternoEncargado(String apellidoPaternoEncargado) { this.apellidoPaternoEncargado = apellidoPaternoEncargado; }

    public String getApellidoMaternoEncargado() { return apellidoMaternoEncargado; }
    public void setApellidoMaternoEncargado(String apellidoMaternoEncargado) { this.apellidoMaternoEncargado = apellidoMaternoEncargado; }

    public String getEncargado() {
        if (nombresEncargado == null) return null;
        StringBuilder sb = new StringBuilder(nombresEncargado);
        if (apellidoPaternoEncargado != null && !apellidoPaternoEncargado.isBlank()) sb.append(' ').append(apellidoPaternoEncargado);
        if (apellidoMaternoEncargado != null && !apellidoMaternoEncargado.isBlank()) sb.append(' ').append(apellidoMaternoEncargado);
        return sb.toString();
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public String getEnlaceCita() {
        return enlaceCita;
    }

    public void setEnlaceCita(String enlaceCita) {
        this.enlaceCita = enlaceCita;
    }

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    public int getAlumnosCanalizados() {
        return alumnosCanalizados;
    }

    public void setAlumnosCanalizados(int alumnosCanalizados) {
        this.alumnosCanalizados = alumnosCanalizados;
    }

    @Override
    public String toString() {
        return idArea + ',' + nombre + ',' + getEncargado() + ',' + correoContacto;
    }
}
