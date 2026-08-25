package mx.edu.utez.pigestiontutorias.models;

public class AsignacionDTO {
    private int idCarrera;
    private String nombreCarrera;
    private int idCuatrimestre;
    private int numeroCuatri;
    private int idLetra;
    private String letra;

    public AsignacionDTO() {}

    public AsignacionDTO(int idCarrera, String nombreCarrera, int idCuatrimestre, int numeroCuatri, int idLetra, String letra) {
        this.idCarrera = idCarrera;
        this.nombreCarrera = nombreCarrera;
        this.idCuatrimestre = idCuatrimestre;
        this.numeroCuatri = numeroCuatri;
        this.idLetra = idLetra;
        this.letra = letra;
    }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    public String getNombreCarrera() { return nombreCarrera; }
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    public int getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    public int getNumeroCuatri() { return numeroCuatri; }
    public void setNumeroCuatri(int numeroCuatri) { this.numeroCuatri = numeroCuatri; }

    public int getIdLetra() { return idLetra; }
    public void setIdLetra(int idLetra) { this.idLetra = idLetra; }

    public String getLetra() { return letra; }
    public void setLetra(String letra) { this.letra = letra; }

    public String getValorOption() {
        return idCarrera + "|" + idCuatrimestre + "|" + idLetra;
    }

    public String getEtiqueta() {
        return nombreCarrera + " - " + numeroCuatri + "° " + letra;
    }
}
