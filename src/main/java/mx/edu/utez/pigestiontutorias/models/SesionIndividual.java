package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class SesionIndividual {
    private int idSesionIndividual;
    private int idTutor;
    private String matricula;
    private Date fecha;
    private String hora; // ej. "13:00", la hora original propuesta/confirmada de la cita
    private String temasTratados;
    private String acuerdos;
    private Integer idCanalizacion; // puede ser null
    private String estado;
    private String estatusAsistencia; // 'Presente' o 'Falta'
    private String origen; // 'Programada' (via Solicitud) o 'Espontanea' (alta directa del tutor)

    public SesionIndividual() {}

    public int getIdSesionIndividual() { return idSesionIndividual; }
    public void setIdSesionIndividual(int idSesionIndividual) { this.idSesionIndividual = idSesionIndividual; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getTemasTratados() { return temasTratados; }
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    public String getAcuerdos() { return acuerdos; }
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    public Integer getIdCanalizacion() { return idCanalizacion; }
    public void setIdCanalizacion(Integer idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEstatusAsistencia() { return estatusAsistencia; }
    public void setEstatusAsistencia(String estatusAsistencia) { this.estatusAsistencia = estatusAsistencia; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}