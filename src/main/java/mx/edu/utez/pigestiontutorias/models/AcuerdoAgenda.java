package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class AcuerdoAgenda {
    private final String tipo;
    private final Date fecha;
    private final String acuerdos;

    public AcuerdoAgenda(String tipo, Date fecha, String acuerdos) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.acuerdos = acuerdos;
    }

    public String getTipo() { return tipo; }
    public Date getFecha() { return fecha; }
    public String getAcuerdos() { return acuerdos; }
}
