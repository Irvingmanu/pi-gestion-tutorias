package mx.edu.utez.pigestiontutorias.models;

/**
 * Representa las credenciales de acceso de un usuario del sistema (alumno, tutor o
 * coordinador), incluyendo el control de intentos fallidos y la recuperación de contraseña.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
public class Usuario {
    private int idUsuario;
    private String rol;
    private String identificador;
    private String pass;
    private int intentosFallidos;
    private String codigoRecuperacion;
    private String correoInstitucional;

    /**
     * Construye un usuario vacío.
     */
    public Usuario() {

    }

    /** @return el identificador interno del usuario */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @param idUsuario el identificador a asignar */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /** @return el rol del usuario (Alumno, Tutor o Coordinador) */
    public String getRol() {
        return rol;
    }

    /** @param rol el rol a asignar */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /** @return el identificador de acceso del usuario (matrícula o número de empleado) */
    public String getIdentificador() {
        return identificador;
    }

    /** @param identificador el identificador de acceso a asignar */
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    /** @return la contraseña (hash) del usuario */
    public String getPass() {
        return pass;
    }

    /** @param pass la contraseña a asignar */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /** @return el número de intentos fallidos de inicio de sesión */
    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    /** @param intentosFallidos el número de intentos fallidos a asignar */
    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    /** @return el código de recuperación de contraseña vigente */
    public String getCodigoRecuperacion() {
        return codigoRecuperacion;
    }

    /** @param codigoRecuperacion el código de recuperación a asignar */
    public void setCodigoRecuperacion(String codigoRecuperacion) {
        this.codigoRecuperacion = codigoRecuperacion;
    }

    /** @return el correo institucional del usuario */
    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    /** @param correoInstitucional el correo institucional a asignar */
    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }
}