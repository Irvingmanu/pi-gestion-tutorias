package mx.edu.utez.pigestiontutorias.models;

/**
 * Entidad que representa a un coordinador del sistema de tutorías, con sus
 * datos personales, de contacto y credenciales de acceso.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class Coordinador {
    private int numeroEmpleado;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoInstitucional;
    private String telefono;
    private String pass;
    private String estado;

    /**
     * Construye un coordinador vacío.
     */
    public Coordinador() {
    }

    /** Obtiene el número de empleado del coordinador. @return el número de empleado */
    public int getNumeroEmpleado() { return numeroEmpleado; }
    /** Establece el número de empleado del coordinador. @param numeroEmpleado el número a asignar */
    public void setNumeroEmpleado(int numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    /** Obtiene los nombres del coordinador. @return los nombres del coordinador */
    public String getNombres() { return nombres; }
    /** Establece los nombres del coordinador. @param nombres los nombres a asignar */
    public void setNombres(String nombres) { this.nombres = nombres; }

    /** Obtiene el apellido paterno del coordinador. @return el apellido paterno */
    public String getApellidoPaterno() { return apellidoPaterno; }
    /** Establece el apellido paterno del coordinador. @param apellidoPaterno el apellido paterno a asignar */
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    /** Obtiene el apellido materno del coordinador. @return el apellido materno */
    public String getApellidoMaterno() { return apellidoMaterno; }
    /** Establece el apellido materno del coordinador. @param apellidoMaterno el apellido materno a asignar */
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    /**
     * Obtiene los apellidos completos del coordinador, combinando paterno y
     * materno cuando el materno está presente.
     * @return el apellido paterno solo, o paterno y materno concatenados
     */
    public String getApellidos() {
        if (apellidoMaterno == null || apellidoMaterno.isBlank()) return apellidoPaterno;
        return apellidoPaterno + " " + apellidoMaterno;
    }

    /** Obtiene el correo institucional del coordinador. @return el correo institucional */
    public String getCorreoInstitucional() { return correoInstitucional; }
    /** Establece el correo institucional del coordinador. @param correoInstitucional el correo a asignar */
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    /** Obtiene el teléfono del coordinador. @return el teléfono del coordinador */
    public String getTelefono() { return telefono; }
    /** Establece el teléfono del coordinador. @param telefono el teléfono a asignar */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** Obtiene la contraseña (hash) del coordinador. @return la contraseña del coordinador */
    public String getPass() { return pass; }
    /** Establece la contraseña (hash) del coordinador. @param pass la contraseña a asignar */
    public void setPass(String pass) { this.pass = pass; }

    /** Obtiene el estado (activo/inactivo) del coordinador. @return el estado del coordinador */
    public String getEstado() { return estado; }
    /** Establece el estado (activo/inactivo) del coordinador. @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Representa al coordinador como una línea de texto separada por comas
     * (número de empleado, nombres, apellidos y correo institucional).
     * @return la representación en texto del coordinador
     */
    @Override
    public String toString() {
        return numeroEmpleado + ',' + nombres + ',' + getApellidos() + ',' + correoInstitucional;
    }
}
