package mx.edu.utez.pigestiontutorias.models;
public class LetraGrupo {
    private int idLetra;
    private String letra;

    public LetraGrupo() {}
    public LetraGrupo(int idLetra, String letra) { this.idLetra = idLetra; this.letra = letra; }
    public int getIdLetra() { return idLetra; }
    public void setIdLetra(int idLetra) { this.idLetra = idLetra; }
    public String getLetra() { return letra; }
    public void setLetra(String letra) { this.letra = letra; }
}