package mx.edu.utez.pigestiontutorias.models;

public class CanalizacionRecordatorioDTO {
    private String nombreArea;
    private String correoContactoArea;
    private String encargadoArea;
    private String nombreAlumno;
    private String matricula;
    private String motivoODetalle;
    private String idToken;
    private String estatus;

    public String getNombreArea() { return nombreArea; }
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    public String getCorreoContactoArea() { return correoContactoArea; }
    public void setCorreoContactoArea(String correoContactoArea) { this.correoContactoArea = correoContactoArea; }

    public String getEncargadoArea() { return encargadoArea; }
    public void setEncargadoArea(String encargadoArea) { this.encargadoArea = encargadoArea; }

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getMotivoODetalle() { return motivoODetalle; }
    public void setMotivoODetalle(String motivoODetalle) { this.motivoODetalle = motivoODetalle; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
