package mx.edu.utez.pigestiontutorias.models;

public class Alumno {
    private String matricula;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoInstitucional;
    private String telefono;
    private Integer idGenero;
    private Integer idGrupo;
    private String pass;
    private String estado;

    private Grupo grupo;

    public Alumno() {
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

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

    public Integer getIdGenero() { return idGenero; }
    public void setIdGenero(Integer idGenero) { this.idGenero = idGenero; }

    public Integer getIdGrupo() { return idGrupo; }
    public void setIdGrupo(Integer idGrupo) { this.idGrupo = idGrupo; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }

    @Override
    public String toString() {
        return matricula + ',' + nombres + ',' + getApellidos() + ',' + correoInstitucional;
    }
}
