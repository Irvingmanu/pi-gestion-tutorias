package mx.edu.utez.pigestiontutorias.models;

import java.util.List;

/**
 * Representa a un tutor del sistema, con sus datos personales, su academia,
 * horarios de disponibilidad y los grupos que tiene asignados.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
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

    /**
     * Construye un tutor vacío.
     */
    public Tutor() {
    }

    /** @return el número de empleado del tutor */
    public int getNumeroEmpleado() { return numeroEmpleado; }
    /** @param numeroEmpleado el número de empleado a asignar */
    public void setNumeroEmpleado(int numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    /** @return los nombres del tutor */
    public String getNombres() { return nombres; }
    /** @param nombres los nombres a asignar */
    public void setNombres(String nombres) { this.nombres = nombres; }

    /** @return el apellido paterno del tutor */
    public String getApellidoPaterno() { return apellidoPaterno; }
    /** @param apellidoPaterno el apellido paterno a asignar */
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    /** @return el apellido materno del tutor */
    public String getApellidoMaterno() { return apellidoMaterno; }
    /** @param apellidoMaterno el apellido materno a asignar */
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    /**
     * Concatena el apellido paterno y materno del tutor, omitiendo el materno si
     * está vacío.
     * @return los apellidos completos del tutor
     */
    public String getApellidos() {
        if (apellidoMaterno == null || apellidoMaterno.isBlank()) return apellidoPaterno;
        return apellidoPaterno + " " + apellidoMaterno;
    }

    /** @return el correo institucional del tutor */
    public String getCorreoInstitucional() { return correoInstitucional; }
    /** @param correoInstitucional el correo a asignar */
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    /** @return el teléfono del tutor */
    public String getTelefono() { return telefono; }
    /** @param telefono el teléfono a asignar */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** @return el identificador de la academia del tutor */
    public int getIdAcademia() { return idAcademia; }
    /** @param idAcademia el identificador de academia a asignar */
    public void setIdAcademia(int idAcademia) { this.idAcademia = idAcademia; }

    /** @return la contraseña (hash) del tutor */
    public String getPass() { return pass; }
    /** @param pass la contraseña a asignar */
    public void setPass(String pass) { this.pass = pass; }

    /** @return el estado del tutor */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return los horarios de disponibilidad del tutor */
    public List<String> getHorariosDispo() { return horariosDispo; }
    /** @param horariosDispo los horarios de disponibilidad a asignar */
    public void setHorariosDispo(List<String> horariosDispo) { this.horariosDispo = horariosDispo; }

    /** @return la academia a la que pertenece el tutor */
    public Academia getAcademia() { return academia; }
    /** @param academia la academia a asignar */
    public void setAcademia(Academia academia) { this.academia = academia; }

    /** @return los nombres de los grupos asignados al tutor */
    public List<String> getGruposAsignados() { return gruposAsignados; }
    /** @param gruposAsignados los grupos asignados a asignar */
    public void setGruposAsignados(List<String> gruposAsignados) { this.gruposAsignados = gruposAsignados; }

    /**
     * Construye el texto con los grupos asignados al tutor separados por coma.
     * @return el texto de los grupos asignados, o "Sin grupo asignado" si no tiene ninguno
     */
    public String getGrupoAsignadoTexto() {
        if (gruposAsignados == null || gruposAsignados.isEmpty()) return "Sin grupo asignado";
        return String.join(", ", gruposAsignados);
    }
}
