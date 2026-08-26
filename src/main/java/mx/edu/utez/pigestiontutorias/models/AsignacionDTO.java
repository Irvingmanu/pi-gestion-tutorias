package mx.edu.utez.pigestiontutorias.models;

/**
 * DTO que representa una combinación posible de carrera, cuatrimestre y letra
 * de grupo, usada para poblar el selector de asignación de tutores a grupos.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-01
 */
public class AsignacionDTO {
    private int idCarrera;
    private String nombreCarrera;
    private int idCuatrimestre;
    private int numeroCuatri;
    private int idLetra;
    private String letra;

    /**
     * Construye un DTO de asignación vacío.
     */
    public AsignacionDTO() {}

    /**
     * Construye un DTO de asignación con los datos indicados.
     * @param idCarrera el identificador de la carrera
     * @param nombreCarrera el nombre de la carrera
     * @param idCuatrimestre el identificador del cuatrimestre
     * @param numeroCuatri el número de cuatrimestre
     * @param idLetra el identificador de la letra de grupo
     * @param letra la letra de grupo
     */
    public AsignacionDTO(int idCarrera, String nombreCarrera, int idCuatrimestre, int numeroCuatri, int idLetra, String letra) {
        this.idCarrera = idCarrera;
        this.nombreCarrera = nombreCarrera;
        this.idCuatrimestre = idCuatrimestre;
        this.numeroCuatri = numeroCuatri;
        this.idLetra = idLetra;
        this.letra = letra;
    }

    /**
     * Obtiene el identificador de la carrera.
     * @return el identificador de la carrera
     */
    public int getIdCarrera() { return idCarrera; }
    /**
     * Establece el identificador de la carrera.
     * @param idCarrera el identificador a asignar
     */
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    /**
     * Obtiene el nombre de la carrera.
     * @return el nombre de la carrera
     */
    public String getNombreCarrera() { return nombreCarrera; }
    /**
     * Establece el nombre de la carrera.
     * @param nombreCarrera el nombre a asignar
     */
    public void setNombreCarrera(String nombreCarrera) { this.nombreCarrera = nombreCarrera; }

    /**
     * Obtiene el identificador del cuatrimestre.
     * @return el identificador del cuatrimestre
     */
    public int getIdCuatrimestre() { return idCuatrimestre; }
    /**
     * Establece el identificador del cuatrimestre.
     * @param idCuatrimestre el identificador a asignar
     */
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    /**
     * Obtiene el número de cuatrimestre.
     * @return el número de cuatrimestre
     */
    public int getNumeroCuatri() { return numeroCuatri; }
    /**
     * Establece el número de cuatrimestre.
     * @param numeroCuatri el número a asignar
     */
    public void setNumeroCuatri(int numeroCuatri) { this.numeroCuatri = numeroCuatri; }

    /**
     * Obtiene el identificador de la letra de grupo.
     * @return el identificador de la letra
     */
    public int getIdLetra() { return idLetra; }
    /**
     * Establece el identificador de la letra de grupo.
     * @param idLetra el identificador a asignar
     */
    public void setIdLetra(int idLetra) { this.idLetra = idLetra; }

    /**
     * Obtiene la letra de grupo.
     * @return la letra de grupo
     */
    public String getLetra() { return letra; }
    /**
     * Establece la letra de grupo.
     * @param letra la letra a asignar
     */
    public void setLetra(String letra) { this.letra = letra; }

    /**
     * Construye el valor compuesto usado como value del option en el selector
     * (carrera|cuatrimestre|letra).
     * @return el valor compuesto para el option
     */
    public String getValorOption() {
        return idCarrera + "|" + idCuatrimestre + "|" + idLetra;
    }

    /**
     * Construye la etiqueta legible mostrada en el selector, combinando carrera,
     * número de cuatrimestre y letra.
     * @return la etiqueta legible para el option
     */
    public String getEtiqueta() {
        return nombreCarrera + " - " + numeroCuatri + "° " + letra;
    }
}
