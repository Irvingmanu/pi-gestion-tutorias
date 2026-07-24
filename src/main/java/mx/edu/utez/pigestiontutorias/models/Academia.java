package mx.edu.utez.pigestiontutorias.models;

public class Academia {
    private int idAcademia;
    private String nombre;

    public Academia() {
    }

    public Academia(int idAcademia, String nombre) {
        this.idAcademia = idAcademia;
        this.nombre = nombre;
    }

    public int getIdAcademia() {
        return idAcademia;
    }

    public void setIdAcademia(int idAcademia) {
        this.idAcademia = idAcademia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
