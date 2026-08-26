package mx.edu.utez.pigestiontutorias.models;

/**
 * Entidad que representa a un alumno del sistema de tutorías, con sus datos
 * personales, de contacto, credenciales y su grupo asignado.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
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

    /**
     * Construye un alumno vacío.
     */
    public Alumno() {
    }

    /**
     * Obtiene la matrícula del alumno.
     * @return la matrícula del alumno
     */
    public String getMatricula() { return matricula; }
    /**
     * Establece la matrícula del alumno.
     * @param matricula la matrícula a asignar
     */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /**
     * Obtiene los nombres del alumno.
     * @return los nombres del alumno
     */
    public String getNombres() { return nombres; }
    /**
     * Establece los nombres del alumno.
     * @param nombres los nombres a asignar
     */
    public void setNombres(String nombres) { this.nombres = nombres; }

    /**
     * Obtiene el apellido paterno del alumno.
     * @return el apellido paterno
     */
    public String getApellidoPaterno() { return apellidoPaterno; }
    /**
     * Establece el apellido paterno del alumno.
     * @param apellidoPaterno el apellido paterno a asignar
     */
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    /**
     * Obtiene el apellido materno del alumno.
     * @return el apellido materno
     */
    public String getApellidoMaterno() { return apellidoMaterno; }
    /**
     * Establece el apellido materno del alumno.
     * @param apellidoMaterno el apellido materno a asignar
     */
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    /**
     * Obtiene los apellidos completos del alumno, combinando paterno y materno
     * cuando el materno está presente.
     * @return el apellido paterno solo, o paterno y materno concatenados
     */
    public String getApellidos() {
        if (apellidoMaterno == null || apellidoMaterno.isBlank()) return apellidoPaterno;
        return apellidoPaterno + " " + apellidoMaterno;
    }

    /**
     * Obtiene el correo institucional del alumno.
     * @return el correo institucional
     */
    public String getCorreoInstitucional() { return correoInstitucional; }
    /**
     * Establece el correo institucional del alumno.
     * @param correoInstitucional el correo institucional a asignar
     */
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    /**
     * Obtiene el teléfono del alumno.
     * @return el teléfono del alumno
     */
    public String getTelefono() { return telefono; }
    /**
     * Establece el teléfono del alumno.
     * @param telefono el teléfono a asignar
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Obtiene el identificador del género del alumno.
     * @return el identificador del género
     */
    public Integer getIdGenero() { return idGenero; }
    /**
     * Establece el identificador del género del alumno.
     * @param idGenero el identificador del género a asignar
     */
    public void setIdGenero(Integer idGenero) { this.idGenero = idGenero; }

    /**
     * Obtiene el identificador del grupo al que pertenece el alumno.
     * @return el identificador del grupo
     */
    public Integer getIdGrupo() { return idGrupo; }
    /**
     * Establece el identificador del grupo al que pertenece el alumno.
     * @param idGrupo el identificador del grupo a asignar
     */
    public void setIdGrupo(Integer idGrupo) { this.idGrupo = idGrupo; }

    /**
     * Obtiene la contraseña (hash) del alumno.
     * @return la contraseña del alumno
     */
    public String getPass() { return pass; }
    /**
     * Establece la contraseña (hash) del alumno.
     * @param pass la contraseña a asignar
     */
    public void setPass(String pass) { this.pass = pass; }

    /**
     * Obtiene el estado (activo/inactivo) del alumno.
     * @return el estado del alumno
     */
    public String getEstado() { return estado; }
    /**
     * Establece el estado (activo/inactivo) del alumno.
     * @param estado el estado a asignar
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Obtiene el grupo asociado al alumno.
     * @return el grupo del alumno
     */
    public Grupo getGrupo() { return grupo; }
    /**
     * Establece el grupo asociado al alumno.
     * @param grupo el grupo a asignar
     */
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }

    /**
     * Representa al alumno como una línea de texto separada por comas
     * (matrícula, nombres, apellidos y correo institucional).
     * @return la representación en texto del alumno
     */
    @Override
    public String toString() {
        return matricula + ',' + nombres + ',' + getApellidos() + ',' + correoInstitucional;
    }
}
