package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * DTO que representa una atención individual brindada a un alumno (sesión de
 * tutoría individual), con los temas tratados, acuerdos y datos de contacto.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
public class AtencionAlumnoDTO {
    private int idSesion;
    private String tipo;
    private Date fecha;
    private String hora;
    private String grupoAsignado;
    private String matricula;
    private String nombreAlumno;
    private String estado;
    private String temasTratados;
    private String acuerdos;
    private String vinculoDirecto;

    /** Obtiene el identificador de la sesión. @return el identificador de la sesión */
    public int getIdSesion() { return idSesion; }
    /** Establece el identificador de la sesión. @param idSesion el identificador a asignar */
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    /** Obtiene el tipo de sesión. @return el tipo de sesión */
    public String getTipo() { return tipo; }
    /** Establece el tipo de sesión. @param tipo el tipo a asignar */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** Obtiene la fecha de la sesión. @return la fecha de la sesión */
    public Date getFecha() { return fecha; }
    /** Establece la fecha de la sesión. @param fecha la fecha a asignar */
    public void setFecha(Date fecha) { this.fecha = fecha; }

    /** Obtiene la hora de la sesión. @return la hora de la sesión */
    public String getHora() { return hora; }
    /** Establece la hora de la sesión. @param hora la hora a asignar */
    public void setHora(String hora) { this.hora = hora; }

    /** Obtiene el grupo asignado del alumno. @return el grupo asignado */
    public String getGrupoAsignado() { return grupoAsignado; }
    /** Establece el grupo asignado del alumno. @param grupoAsignado el grupo a asignar */
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    /** Obtiene la matrícula del alumno atendido. @return la matrícula del alumno */
    public String getMatricula() { return matricula; }
    /** Establece la matrícula del alumno atendido. @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

    /** Obtiene el nombre del alumno atendido. @return el nombre del alumno */
    public String getNombreAlumno() { return nombreAlumno; }
    /** Establece el nombre del alumno atendido. @param nombreAlumno el nombre a asignar */
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    /** Obtiene el estado de la sesión. @return el estado de la sesión */
    public String getEstado() { return estado; }
    /** Establece el estado de la sesión. @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** Obtiene los temas tratados en la sesión. @return los temas tratados */
    public String getTemasTratados() { return temasTratados; }
    /** Establece los temas tratados en la sesión. @param temasTratados los temas a asignar */
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    /** Obtiene los acuerdos tomados en la sesión. @return los acuerdos tomados */
    public String getAcuerdos() { return acuerdos; }
    /** Establece los acuerdos tomados en la sesión. @param acuerdos los acuerdos a asignar */
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    /** Obtiene el vínculo directo (enlace) de la sesión. @return el vínculo directo */
    public String getVinculoDirecto() { return vinculoDirecto; }
    /** Establece el vínculo directo (enlace) de la sesión. @param vinculoDirecto el vínculo a asignar */
    public void setVinculoDirecto(String vinculoDirecto) { this.vinculoDirecto = vinculoDirecto; }
}
