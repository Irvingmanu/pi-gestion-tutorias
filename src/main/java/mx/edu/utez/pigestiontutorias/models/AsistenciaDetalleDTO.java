package mx.edu.utez.pigestiontutorias.models;

public class AsistenciaDetalleDTO {
    private String matricula;
    private String nombreCompleto;
    private String estatusAsistencia;

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEstatusAsistencia() { return estatusAsistencia; }
    public void setEstatusAsistencia(String estatusAsistencia) { this.estatusAsistencia = estatusAsistencia; }
}
