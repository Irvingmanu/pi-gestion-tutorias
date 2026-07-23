package mx.edu.utez.pigestiontutorias.models;

public class Cuatrimestre {
    private int idCuatrimestre;
    private int numero;

    public Cuatrimestre() {
    }

    public Cuatrimestre(int idCuatrimestre, int numero) {
        this.idCuatrimestre = idCuatrimestre;
        this.numero = numero;
    }

    public int getIdCuatrimestre() {
        return idCuatrimestre;
    }

    public void setIdCuatrimestre(int idCuatrimestre) {
        this.idCuatrimestre = idCuatrimestre;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}