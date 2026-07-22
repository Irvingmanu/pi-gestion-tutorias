package mx.edu.utez.pigestiontutorias.models;

public class Coordinador {
    private int idCoordinador;
    private int idUsuario;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;

    public Coordinador() {
    }

    public Coordinador(int idCoordinador, int idUsuario, String nombres, String apellidos, String correoInstitucional) {
        this.idCoordinador = idCoordinador;
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
    }

    public int getIdCoordinador() { return idCoordinador; }
    public void setIdCoordinador(int idCoordinador) { this.idCoordinador = idCoordinador; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    @Override
    public String toString() {
        return idCoordinador + ',' + nombres + ',' + apellidos + ',' + correoInstitucional;
    }
}
