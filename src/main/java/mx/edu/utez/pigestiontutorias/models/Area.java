package mx.edu.utez.pigestiontutorias.models;

import java.util.List;

/**
 * Entidad que representa un área de apoyo a la que se pueden canalizar alumnos
 * (por ejemplo psicología o tutorías académicas), con su encargado, contacto,
 * motivos de canalización asociados y el conteo de alumnos canalizados.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
public class Area {
    private int idArea;
    private String nombre;
    private String nombresEncargado;
    private String apellidoPaternoEncargado;
    private String apellidoMaternoEncargado;
    private String correoContacto;
    private String enlaceCita;
    private List<Motivo> motivos;
    private int alumnosCanalizados;

    /**
     * Construye un área vacía.
     */
    public Area() {
    }

    /**
     * Obtiene el identificador del área.
     * @return el identificador del área
     */
    public int getIdArea() {
        return idArea;
    }

    /**
     * Establece el identificador del área.
     * @param idArea el identificador a asignar
     */
    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    /**
     * Obtiene el nombre del área.
     * @return el nombre del área
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del área.
     * @param nombre el nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene los nombres del encargado del área.
     * @return los nombres del encargado
     */
    public String getNombresEncargado() { return nombresEncargado; }
    /**
     * Establece los nombres del encargado del área.
     * @param nombresEncargado los nombres a asignar
     */
    public void setNombresEncargado(String nombresEncargado) { this.nombresEncargado = nombresEncargado; }

    /**
     * Obtiene el apellido paterno del encargado del área.
     * @return el apellido paterno del encargado
     */
    public String getApellidoPaternoEncargado() { return apellidoPaternoEncargado; }
    /**
     * Establece el apellido paterno del encargado del área.
     * @param apellidoPaternoEncargado el apellido paterno a asignar
     */
    public void setApellidoPaternoEncargado(String apellidoPaternoEncargado) { this.apellidoPaternoEncargado = apellidoPaternoEncargado; }

    /**
     * Obtiene el apellido materno del encargado del área.
     * @return el apellido materno del encargado
     */
    public String getApellidoMaternoEncargado() { return apellidoMaternoEncargado; }
    /**
     * Establece el apellido materno del encargado del área.
     * @param apellidoMaternoEncargado el apellido materno a asignar
     */
    public void setApellidoMaternoEncargado(String apellidoMaternoEncargado) { this.apellidoMaternoEncargado = apellidoMaternoEncargado; }

    /**
     * Obtiene el nombre completo del encargado del área, combinando nombres y apellidos disponibles.
     * @return el nombre completo del encargado, o {@code null} si no tiene nombres registrados
     */
    public String getEncargado() {
        if (nombresEncargado == null) return null;
        StringBuilder sb = new StringBuilder(nombresEncargado);
        if (apellidoPaternoEncargado != null && !apellidoPaternoEncargado.isBlank()) sb.append(' ').append(apellidoPaternoEncargado);
        if (apellidoMaternoEncargado != null && !apellidoMaternoEncargado.isBlank()) sb.append(' ').append(apellidoMaternoEncargado);
        return sb.toString();
    }

    /**
     * Obtiene el correo de contacto del área.
     * @return el correo de contacto
     */
    public String getCorreoContacto() {
        return correoContacto;
    }

    /**
     * Establece el correo de contacto del área.
     * @param correoContacto el correo a asignar
     */
    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    /**
     * Obtiene el enlace de cita del área.
     * @return el enlace de cita
     */
    public String getEnlaceCita() {
        return enlaceCita;
    }

    /**
     * Establece el enlace de cita del área.
     * @param enlaceCita el enlace a asignar
     */
    public void setEnlaceCita(String enlaceCita) {
        this.enlaceCita = enlaceCita;
    }

    /**
     * Obtiene la lista de motivos de canalización asociados al área.
     * @return la lista de motivos
     */
    public List<Motivo> getMotivos() {
        return motivos;
    }

    /**
     * Establece la lista de motivos de canalización asociados al área.
     * @param motivos la lista de motivos a asignar
     */
    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    /**
     * Obtiene el número de alumnos canalizados hacia el área.
     * @return el número de alumnos canalizados
     */
    public int getAlumnosCanalizados() {
        return alumnosCanalizados;
    }

    /**
     * Establece el número de alumnos canalizados hacia el área.
     * @param alumnosCanalizados el número de alumnos canalizados a asignar
     */
    public void setAlumnosCanalizados(int alumnosCanalizados) {
        this.alumnosCanalizados = alumnosCanalizados;
    }

    /**
     * Representa al área como una línea de texto separada por comas
     * (identificador, nombre, encargado y correo de contacto).
     * @return la representación en texto del área
     */
    @Override
    public String toString() {
        return idArea + ',' + nombre + ',' + getEncargado() + ',' + correoContacto;
    }
}
