package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

/**
 * DTO que representa un acuerdo registrado en la agenda de tutorías de un alumno,
 * indicando el tipo de sesión, la fecha y el texto de los acuerdos tomados.
 * @author 20253ds092-star
 * @version 1.0
 * @since 2026-08-10
 */
public class AcuerdoAgenda {
    private final String tipo;
    private final Date fecha;
    private final String acuerdos;

    /**
     * Construye un acuerdo de agenda con los datos indicados.
     * @param tipo el tipo de sesión asociada (individual o grupal)
     * @param fecha la fecha en que se registró el acuerdo
     * @param acuerdos el texto de los acuerdos tomados
     */
    public AcuerdoAgenda(String tipo, Date fecha, String acuerdos) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.acuerdos = acuerdos;
    }

    /**
     * Obtiene el tipo de sesión asociada al acuerdo.
     * @return el tipo de sesión
     */
    public String getTipo() { return tipo; }
    /**
     * Obtiene la fecha del acuerdo.
     * @return la fecha del acuerdo
     */
    public Date getFecha() { return fecha; }
    /**
     * Obtiene el texto de los acuerdos tomados.
     * @return el texto de los acuerdos
     */
    public String getAcuerdos() { return acuerdos; }
}
