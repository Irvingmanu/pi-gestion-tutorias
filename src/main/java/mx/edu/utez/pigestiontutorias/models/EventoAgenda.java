package mx.edu.utez.pigestiontutorias.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Representa un evento de la agenda de un alumno (sesión individual o grupal) ya
 * formateado para su presentación, con la fecha/hora convertidas a texto legible
 * y el estado calculado (Pendiente o Tomada) según si ya ocurrió o no.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-25
 */
public class EventoAgenda {
    private String tipo;
    private String descripcion;
    private String fechaFormateada;
    private String estado;
    private String estadoAsistenciaAlumno;

    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    /**
     * Construye un evento de agenda vacío.
     */
    public EventoAgenda() {}

    /**
     * Construye un evento de agenda combinando la fecha con la hora indicada (si se
     * provee), calculando el texto formateado de la fecha y el estado del evento
     * (Pendiente si es futuro, Tomada si ya pasó).
     * @param tipo el tipo de evento (por ejemplo, "Individual" o "Grupal")
     * @param descripcion la descripción del evento
     * @param fecha la fecha y hora base del evento
     * @param hora la hora en formato "HH:mm" a sobreponer sobre la fecha, o {@code null} para usar la de {@code fecha}
     * @param estadoAsistenciaAlumno el estado de asistencia del alumno a este evento
     */
    public EventoAgenda(String tipo, String descripcion, Timestamp fecha, String hora, String estadoAsistenciaAlumno) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.estadoAsistenciaAlumno = estadoAsistenciaAlumno;

        if (fecha != null) {

            LocalDateTime dt = fecha.toLocalDateTime();
            if (hora != null && !hora.isBlank()) {
                String[] partes = hora.trim().split(":");
                try {
                    dt = dt.withHour(Integer.parseInt(partes[0].trim())).withMinute(Integer.parseInt(partes[1].trim()));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {

                }
            }
            this.fechaFormateada = formatearFecha(dt);
            this.estado = dt.isAfter(LocalDateTime.now()) ? "Pendiente" : "Tomada";
        } else {
            this.fechaFormateada = "";
            this.estado = "";
        }
    }

    /**
     * Formatea una fecha/hora al estilo "d Mes yyyy - h:mmam/pm" usado en la agenda.
     * @param dt la fecha y hora a formatear
     * @return el texto formateado de la fecha
     */
    private String formatearFecha(LocalDateTime dt) {
        int hora24 = dt.getHour();
        int hora12 = hora24 % 12;
        if (hora12 == 0) hora12 = 12;
        String ampm = hora24 < 12 ? "am" : "pm";
        String minutos = String.format("%02d", dt.getMinute());

        return String.format("%d %s %d - %d:%s%s",
                dt.getDayOfMonth(),
                MESES[dt.getMonthValue() - 1],
                dt.getYear(),
                hora12,
                minutos,
                ampm);
    }

    /** @return el tipo de evento */
    public String getTipo() { return tipo; }
    /** @param tipo el tipo de evento a asignar */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** @return la descripción del evento */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion la descripción a asignar */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return el texto de la fecha ya formateada para mostrar */
    public String getFechaFormateada() { return fechaFormateada; }
    /** @param fechaFormateada el texto de fecha formateada a asignar */
    public void setFechaFormateada(String fechaFormateada) { this.fechaFormateada = fechaFormateada; }

    /** @return el estado del evento ("Pendiente" o "Tomada") */
    public String getEstado() { return estado; }
    /** @param estado el estado a asignar */
    public void setEstado(String estado) { this.estado = estado; }

    /** @return el estado de asistencia del alumno a este evento */
    public String getEstadoAsistenciaAlumno() { return estadoAsistenciaAlumno; }
    /** @param estadoAsistenciaAlumno el estado de asistencia a asignar */
    public void setEstadoAsistenciaAlumno(String estadoAsistenciaAlumno) { this.estadoAsistenciaAlumno = estadoAsistenciaAlumno; }
}