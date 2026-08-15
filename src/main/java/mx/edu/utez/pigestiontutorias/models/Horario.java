package mx.edu.utez.pigestiontutorias.models;

public class Horario {

    private int idHorario;
    private int idTutor;
    private String diaSemana;
    private String horaDesde;   // ya formateada como "08:00" para mostrar en el <select>
    private String horaHasta;   // ya formateada como "09:00"
    private String estado;

    public Horario() {
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(int idTutor) {
        this.idTutor = idTutor;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(String horaDesde) {
        this.horaDesde = horaDesde;
    }

    public String getHoraHasta() {
        return horaHasta;
    }

    public void setHoraHasta(String horaHasta) {
        this.horaHasta = horaHasta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Útil para mostrar directo en el <option> del select, ej: "Lunes 08:00 - 09:00"
    public String getEtiqueta() {
        return diaSemana + " " + horaDesde + " - " + horaHasta;
    }
}
