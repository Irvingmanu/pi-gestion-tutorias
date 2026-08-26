package mx.edu.utez.pigestiontutorias.models;

/**
 * Representa un horario de disponibilidad de un tutor para atender tutorías,
 * definido por un día de la semana y un rango de horas.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-23
 */
public class Horario {

    private int idHorario;
    private int idTutor;
    private String diaSemana;
    private String horaDesde;
    private String horaHasta;
    private String estado;

    /**
     * Construye un horario vacío.
     */
    public Horario() {
    }

    /** @return el identificador del horario */
    public int getIdHorario() {
        return idHorario;
    }

    /** @param idHorario el identificador a asignar */
    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    /** @return el identificador del tutor dueño del horario */
    public int getIdTutor() {
        return idTutor;
    }

    /** @param idTutor el identificador de tutor a asignar */
    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    /** @return el día de la semana del horario */
    public String getDiaSemana() {
        return diaSemana;
    }

    /** @param diaSemana el día de la semana a asignar */
    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    /** @return la hora de inicio del horario */
    public String getHoraDesde() {
        return horaDesde;
    }

    /** @param horaDesde la hora de inicio a asignar */
    public void setHoraDesde(String horaDesde) {
        this.horaDesde = horaDesde;
    }

    /** @return la hora de fin del horario */
    public String getHoraHasta() {
        return horaHasta;
    }

    /** @param horaHasta la hora de fin a asignar */
    public void setHoraHasta(String horaHasta) {
        this.horaHasta = horaHasta;
    }

    /** @return el estado del horario */
    public String getEstado() {
        return estado;
    }

    /** @param estado el estado a asignar */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Construye la etiqueta de texto del horario combinando día y rango de horas.
     * @return la etiqueta del horario en formato "día horaDesde - horaHasta"
     */
    public String getEtiqueta() {
        return diaSemana + " " + horaDesde + " - " + horaHasta;
    }
}
