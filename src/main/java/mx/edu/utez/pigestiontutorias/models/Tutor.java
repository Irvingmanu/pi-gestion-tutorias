package mx.edu.utez.pigestiontutorias.models;

import java.util.List;

public class Tutor {
    private int numeroEmpleado;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoInstitucional;
    private String telefono;
    private int idAcademia;
    private String pass;
    private String estado;
    private List<String> horariosDispo;
    private Academia academia;
    private List<String> gruposAsignados;

    public Tutor() {
    }

    public int getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(int numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    // Concatena APELLIDO_PATERNO + APELLIDO_MATERNO (columnas separadas en BD) para
    // mostrarlos como un solo nombre completo en listados, historial y correos.
    public String getApellidos() {
        if (apellidoMaterno == null || apellidoMaterno.isBlank()) return apellidoPaterno;
        return apellidoPaterno + " " + apellidoMaterno;
    }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIdAcademia() { return idAcademia; }
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<String> getHorariosDispo() { return horariosDispo; }
    public void setHorariosDispo(List<String> horariosDispo) { this.horariosDispo = horariosDispo; }

    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }

    public List<String> getGruposAsignados() { return gruposAsignados; }
    public void setGruposAsignados(List<String> gruposAsignados) { this.gruposAsignados = gruposAsignados; }

    // Concatena los grupos resueltos via JOIN en TutorDao#getAllConGrupo() en un solo
    // texto legible, para pintar la columna "Grupo" de gestion-tutores.jsp sin logica
    // adicional en la vista (mismo criterio que getApellidos()).
    public String getGrupoAsignadoTexto() {
        if (gruposAsignados == null || gruposAsignados.isEmpty()) return "Sin grupo asignado";
        return String.join(", ", gruposAsignados);
    }
}
