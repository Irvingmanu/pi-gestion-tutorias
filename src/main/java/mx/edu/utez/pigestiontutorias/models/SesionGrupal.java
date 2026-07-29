package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class SesionGrupal {
    private int idSesionGrupal;
    private int idLetraGrupo;
    private int idCuatrimestre;
    private int idTutor;
    private Date fecha;
    private String temasTratados;
    private String acuerdos;
    private String estado;

    public SesionGrupal() {}

    public int getIdSesionGrupal() { return idSesionGrupal; }
    public void setIdSesionGrupal(int idSesionGrupal) { this.idSesionGrupal = idSesionGrupal; }

    public int getIdLetraGrupo() { return idLetraGrupo; }
    public void setIdLetraGrupo(int idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }

    public int getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getTemasTratados() { return temasTratados; }
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    public String getAcuerdos() { return acuerdos; }
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
