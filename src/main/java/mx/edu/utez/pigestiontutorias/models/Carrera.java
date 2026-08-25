package mx.edu.utez.pigestiontutorias.models;

public class Carrera {
    private int idCarrera;
    private String nombre;
    private String nivel;
    private int idAcademia;

    public Carrera() {}

    public Carrera(int idCarrera, String nombre) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
    }

    public Carrera(int idCarrera, String nombre, String nivel) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public int getIdAcademia() { return idAcademia; }
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }
}
