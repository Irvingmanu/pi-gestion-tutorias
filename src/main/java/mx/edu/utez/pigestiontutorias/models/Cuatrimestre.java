package mx.edu.utez.pigestiontutorias.models;

/**
 * Entidad que representa un cuatrimestre del catálogo académico.
 * @author 20253ds092-star
 * @version 1.0
 * @since 2026-07-21
 */
public class Cuatrimestre {
    private int idCuatrimestre;
    private int numero;

    /**
     * Construye un cuatrimestre vacío.
     */
    public Cuatrimestre() {
    }

    /**
     * Construye un cuatrimestre con los datos indicados.
     * @param idCuatrimestre el identificador del cuatrimestre
     * @param numero el número de cuatrimestre
     */
    public Cuatrimestre(int idCuatrimestre, int numero) {
        this.idCuatrimestre = idCuatrimestre;
        this.numero = numero;
    }

    /**
     * Obtiene el identificador del cuatrimestre.
     * @return el identificador del cuatrimestre
     */
    public int getIdCuatrimestre() {
        return idCuatrimestre;
    }

    /**
     * Establece el identificador del cuatrimestre.
     * @param idCuatrimestre el identificador a asignar
     */
    public void setIdCuatrimestre(int idCuatrimestre) {
        this.idCuatrimestre = idCuatrimestre;
    }

    /**
     * Obtiene el número de cuatrimestre.
     * @return el número de cuatrimestre
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Establece el número de cuatrimestre.
     * @param numero el número a asignar
     */
    public void setNumero(int numero) {
        this.numero = numero;
    }
}