package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

// Envoltura de solo lectura para pintar Acuerdos Individuales y Grupales
// intercalados y ordenados por fecha (mas reciente arriba) sin modificar
// SesionIndividual, SesionGrupal, ni las listas que ya arma AcuerdosServlet.
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
