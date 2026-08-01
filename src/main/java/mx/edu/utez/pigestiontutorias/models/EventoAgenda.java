package mx.edu.utez.pigestiontutorias.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    public EventoAgenda() {}

    public EventoAgenda(String tipo, String descripcion, Timestamp fecha, String hora, String estadoAsistenciaAlumno) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.estadoAsistenciaAlumno = estadoAsistenciaAlumno;

        if (fecha != null) {
            // FECHA solo guarda el dia (se crea con Date.valueOf, siempre a medianoche);
            // la hora real de la cita vive aparte en la columna HORA ("HH:mm"). Sin esto,
            // la hora mostrada salia siempre 12:00am.
            LocalDateTime dt = fecha.toLocalDateTime();
            if (hora != null && !hora.isBlank()) {
                String[] partes = hora.trim().split(":");
                try {
                    dt = dt.withHour(Integer.parseInt(partes[0].trim())).withMinute(Integer.parseInt(partes[1].trim()));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    // HORA con formato inesperado: se conserva la medianoche de FECHA como respaldo.
                }
            }
            this.fechaFormateada = formatearFecha(dt);
            this.estado = dt.isAfter(LocalDateTime.now()) ? "Pendiente" : "Tomada";
        } else {
            this.fechaFormateada = "";
            this.estado = "";
        }
    }

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

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaFormateada() { return fechaFormateada; }
    public void setFechaFormateada(String fechaFormateada) { this.fechaFormateada = fechaFormateada; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEstadoAsistenciaAlumno() { return estadoAsistenciaAlumno; }
    public void setEstadoAsistenciaAlumno(String estadoAsistenciaAlumno) { this.estadoAsistenciaAlumno = estadoAsistenciaAlumno; }
}