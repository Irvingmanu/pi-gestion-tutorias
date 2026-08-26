package mx.edu.utez.pigestiontutorias.models;

/**
 * Representa una letra de grupo del catálogo usado para nombrar los grupos académicos.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
public class LetraGrupo {
    private int idLetra;
    private String letra;

    /**
     * Construye una letra de grupo vacía.
     */
    public LetraGrupo() {
    }

    /**
     * Construye una letra de grupo con los datos indicados.
     * @param idLetra el identificador de la letra
     * @param letra el carácter de la letra
     */
    public LetraGrupo(int idLetra, String letra) {
        this.idLetra = idLetra;
        this.letra = letra;
    }

    /** @return el identificador de la letra */
    public int getIdLetra() {
        return idLetra;
    }

    /** @param idLetra el identificador a asignar */
    public void setIdLetra(int idLetra) {
        this.idLetra = idLetra;
    }

    /** @return el carácter de la letra */
    public String getLetra() {
        return letra;
    }

    /** @param letra el carácter a asignar */
    public void setLetra(String letra) {
        this.letra = letra;
    }
}