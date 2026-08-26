package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * DTO que representa un renglón del historial de tutorías de un tutor (sesión
 * individual o grupal), con los datos ya listos para mostrar en la vista de historial.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-09
 */
public class HistorialItemDTO {
    private String tipo;
    private Date fecha;
    private String hora;
    private String referencia;
    private String temasTratados;
    private String acuerdos;
    private String estado;
    private Integer idGrupo;

    /**
     * Construye un item de historial vacío.
     */
    public HistorialItemDTO() {}

    /** @return el tipo de tutoría ("Individual" o "Grupal") */
    public String getTipo() { return tipo; }
    /** @param tipo el tipo a asignar */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** @return la fecha de la tutoría */
    public Date getFecha() { return fecha; }
    /** @param fecha la fecha a asignar */
    public void setFecha(Date fecha) { this.fecha = fecha; }

    /** @return la hora de la tutoría */
    public String getHora() { return hora; }
    /** @param hora la hora a asignar */
    public void setHora(String hora) { this.hora = hora; }

    /** @return la referencia de la tutoría (alumno o grupo atendido) */
    public String getReferencia() { return referencia; }
    /** @param referencia la referencia a asignar */
    public void setReferencia(String referencia) { this.referencia = referencia; }

    /** @return los temas tratados en la tutoría */
    public String getTemasTratados() { return temasTratados; }
    /** @param temasTratados los temas tratados a asignar */
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    /** @return los acuerdos de la tutoría */
    public String getAcuerdos() { return acuerdos; }
    /** @param acuerdos los acuerdos a asignar */
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    /** @return el estado de la tutoría */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return el identificador del grupo asociado, si aplica */
    public Integer getIdGrupo() { return idGrupo; }
    /** @param idGrupo el identificador de grupo a asignar */
    public void setIdGrupo(Integer idGrupo) { this.idGrupo = idGrupo; }
}