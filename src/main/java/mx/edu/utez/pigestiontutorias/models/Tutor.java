package mx.edu.utez.pigestiontutorias.models;

public class Tutor {
    private int idTutor;
    private String nombres;
    private String apellidos;

    public Tutor() {
    }

    public Tutor(int idTutor, String nombres, String apellidos) {
        this.idTutor = idTutor;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public int getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
}