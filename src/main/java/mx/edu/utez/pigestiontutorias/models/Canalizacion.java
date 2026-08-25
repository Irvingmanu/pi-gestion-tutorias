package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class Canalizacion {
    private int idCanalizacion;
    private int idArea;
    private Integer idMotivo;
    private String matricula;
    private Date fechaCanalizacion;
    private String estatus;
    private String observaciones;
    private String idToken;

    private String nombreArea;
    private String encargadoArea;
    private String correoContactoArea;
    private String enlaceCitaArea;
    private String nombreMotivo;

    public Canalizacion() {}

    public int getIdCanalizacion() { return idCanalizacion; }
    public void setIdCanalizacion(int idCanalizacion) { this.idCanalizacion = idCanalizacion; }

    public int getIdArea() { return idArea; }
    public void setIdArea(int idArea) { this.idArea = idArea; }

    public Integer getIdMotivo() { return idMotivo; }
    public void setIdMotivo(Integer idMotivo) { this.idMotivo = idMotivo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public Date getFechaCanalizacion() { return fechaCanalizacion; }
    public void setFechaCanalizacion(Date fechaCanalizacion) { this.fechaCanalizacion = fechaCanalizacion; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getNombreArea() { return nombreArea; }
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    public String getEncargadoArea() { return encargadoArea; }
    public void setEncargadoArea(String encargadoArea) { this.encargadoArea = encargadoArea; }

    public String getCorreoContactoArea() { return correoContactoArea; }
    public void setCorreoContactoArea(String correoContactoArea) { this.correoContactoArea = correoContactoArea; }

    public String getEnlaceCitaArea() { return enlaceCitaArea; }
    public void setEnlaceCitaArea(String enlaceCitaArea) { this.enlaceCitaArea = enlaceCitaArea; }

    public String getNombreMotivo() { return nombreMotivo; }
    public void setNombreMotivo(String nombreMotivo) { this.nombreMotivo = nombreMotivo; }
}