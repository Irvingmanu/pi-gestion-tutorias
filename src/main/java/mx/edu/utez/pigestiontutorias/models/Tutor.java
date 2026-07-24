package mx.edu.utez.pigestiontutorias.models;

import java.util.List;

public class Tutor {
    private int idTutor;
    private int nomina;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String telefono;
    private int idAcademia;
    private int idUsuario;
    private List<String>horariosDispo;

    public Tutor() {
    }

    public Tutor(int idTutor, int nomina, String nombres, String apellidos, String correoInstitucional, String telefono, int idAcademia, int idUsuario) {
        this.idTutor = idTutor;
        this.nomina = nomina;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.telefono = telefono;
        this.idAcademia = idAcademia;
        this.idUsuario = idUsuario;
    }

    // Getters y Setters
    public int getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    public int getNomina() {
        return nomina;
    }

    public void setNomina(int nomina) {
        this.nomina = nomina;
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

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getIdAcademia() {
        return idAcademia;
    }

    public void setIdAcademia(int idAcademia) {
        this.idAcademia = idAcademia;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public List<String> getHorariosDispo() {
        return horariosDispo;
    }

    public void setHorariosDispo(List<String> horariosDispo) {
        this.horariosDispo = horariosDispo;
    }
}
