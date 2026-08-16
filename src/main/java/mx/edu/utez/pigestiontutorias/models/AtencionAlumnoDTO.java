package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

// DTO para el modal "Alumnos Atendidos" del reporte del coordinador: una fila por cada
// SESION_INDIVIDUAL completada (Individual o Espontanea), con los datos ya resueltos de
// grupo/alumno/vinculo directo para no requerir una segunda consulta al pedir "Ver detalles".
public class AtencionAlumnoDTO {
    private int idSesion;
    private String tipo; // "Individual" | "Espontánea"
    private Date fecha;
    private String hora;
    private String grupoAsignado;
    private String matricula;
    private String nombreAlumno;
    private String estado;
    private String temasTratados;
    private String acuerdos;
    private String vinculoDirecto; // null si la sesion no genero una canalizacion

    public int getIdSesion() { return idSesion; }
    public void setIdSesion(int idSesion) { this.idSesion = idSesion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTemasTratados() { return temasTratados; }
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    public String getAcuerdos() { return acuerdos; }
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    public String getVinculoDirecto() { return vinculoDirecto; }
    public void setVinculoDirecto(String vinculoDirecto) { this.vinculoDirecto = vinculoDirecto; }
}
