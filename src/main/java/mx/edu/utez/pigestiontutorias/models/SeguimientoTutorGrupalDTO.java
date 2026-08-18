package mx.edu.utez.pigestiontutorias.models;

// Fila de la tabla "Seguimiento de Tutorías Grupales" (modal de la tarjeta
// "Tutorías Grupales" en Reportes Globales): avance de UN tutor en UN grupo
// real (Carrera+Cuatrimestre+Letra) dentro del periodo escolar vigente.
public class SeguimientoTutorGrupalDTO {
    private int idTutor;
    private String nombreTutor;
    private String correoTutor;
    private int idCarrera;
    private int idCuatrimestre;
    private int idLetraGrupo;
    private String grupoAsignado;
    private int realizadas;
    private int objetivo;
    private String estatus; // "En Riesgo" | "Al día"

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public String getNombreTutor() { return nombreTutor; }
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    public String getCorreoTutor() { return correoTutor; }
    public void setCorreoTutor(String correoTutor) { this.correoTutor = correoTutor; }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    public int getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(int idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    public int getIdLetraGrupo() { return idLetraGrupo; }
    public void setIdLetraGrupo(int idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }

    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }

    public int getRealizadas() { return realizadas; }
    public void setRealizadas(int realizadas) { this.realizadas = realizadas; }

    public int getObjetivo() { return objetivo; }
    public void setObjetivo(int objetivo) { this.objetivo = objetivo; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
