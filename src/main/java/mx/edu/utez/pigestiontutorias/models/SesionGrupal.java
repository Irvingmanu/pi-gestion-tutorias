package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * Representa una sesión de tutoría grupal impartida por un tutor a un grupo,
 * incluyendo temas tratados, acuerdos y la lista de matrículas asistentes.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-28
 */
public class SesionGrupal {
    private int idSesionGrupal;
    private int idGrupo;
    private int idTutor;
    private Date fecha;
    private String hora;
    private String temasTratados;
    private String acuerdos;
    private String asesoriasGrupales;
    private String estado;

    private String[] asistentes;

    /**
     * Construye una sesión grupal vacía.
     */
    public SesionGrupal() {}

    /** @return el identificador de la sesión grupal */
    public int getIdSesionGrupal() { return idSesionGrupal; }
    /** @param idSesionGrupal el identificador a asignar */
    public void setIdSesionGrupal(int idSesionGrupal) { this.idSesionGrupal = idSesionGrupal; }

    /** @return el identificador del grupo atendido */
    public int getIdGrupo() { return idGrupo; }
    /** @param idGrupo el identificador de grupo a asignar */
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    /** @return el identificador del tutor que impartió la sesión */
    public int getIdTutor() { return idTutor; }
    /** @param idTutor el identificador de tutor a asignar */
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    /** @return la fecha de la sesión */
    public Date getFecha() { return fecha; }
    /** @param fecha la fecha a asignar */
    public void setFecha(Date fecha) { this.fecha = fecha; }

    /** @return la hora de la sesión */
    public String getHora() { return hora; }
    /** @param hora la hora a asignar */
    public void setHora(String hora) { this.hora = hora; }

    /** @return los temas tratados en la sesión */
    public String getTemasTratados() { return temasTratados; }
    /** @param temasTratados los temas tratados a asignar */
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    /** @return los acuerdos de la sesión */
    public String getAcuerdos() { return acuerdos; }
    /** @param acuerdos los acuerdos a asignar */
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    /** @return las asesorías grupales asociadas a la sesión */
    public String getAsesoriasGrupales() { return asesoriasGrupales; }
    /** @param asesoriasGrupales las asesorías grupales a asignar */
    public void setAsesoriasGrupales(String asesoriasGrupales) { this.asesoriasGrupales = asesoriasGrupales; }

    /** @return el estado de la sesión */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return las matrículas de los alumnos asistentes a la sesión */
    public String[] getAsistentes() { return asistentes; }
    /** @param asistentes las matrículas de los asistentes a asignar */
    public void setAsistentes(String[] asistentes) { this.asistentes = asistentes; }
}
