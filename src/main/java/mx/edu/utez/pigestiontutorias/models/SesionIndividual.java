package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * Representa una sesión de tutoría individual entre un tutor y un alumno,
 * pudiendo originarse de una canalización o de una solicitud directa.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class SesionIndividual {
    private int idSesionIndividual;
    private int idTutor;
    private String matricula;
    private Date fecha;
    private String hora;
    private String temasTratados;
    private String acuerdos;
    private Integer idCanalizacion;
    private String estado;
    private String estatusAsistencia;
    private String origen;

    /**
     * Construye una sesión individual vacía.
     */
    public SesionIndividual() {}

    /** @return el identificador de la sesión individual */
    public int getIdSesionIndividual() { return idSesionIndividual; }
    /** @param idSesionIndividual el identificador a asignar */
    public void setIdSesionIndividual(int idSesionIndividual) { this.idSesionIndividual = idSesionIndividual; }

    /** @return el identificador del tutor que atendió la sesión */
    public int getIdTutor() { return idTutor; }
    /** @param idTutor el identificador de tutor a asignar */
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    /** @return la matrícula del alumno atendido */
    public String getMatricula() { return matricula; }
    /** @param matricula la matrícula a asignar */
    public void setMatricula(String matricula) { this.matricula = matricula; }

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

    /** @return el identificador de la canalización de origen, si aplica */
    public Integer getIdCanalizacion() { return idCanalizacion; }
    /** @param idCanalizacion el identificador de canalización a asignar */
    public void setIdCanalizacion(Integer idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    /** @return el estado de la sesión */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return el estatus de asistencia del alumno a la sesión */
    public String getEstatusAsistencia() { return estatusAsistencia; }
    /** @param estatusAsistencia el estatus de asistencia a asignar */
    public void setEstatusAsistencia(String estatusAsistencia) { this.estatusAsistencia = estatusAsistencia; }

    /** @return el origen de la sesión (por ejemplo, "Canalización" o "Solicitud") */
    public String getOrigen() { return origen; }
    /** @param origen el origen a asignar */
    public void setOrigen(String origen) { this.origen = origen; }
}