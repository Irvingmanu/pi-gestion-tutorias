package mx.edu.utez.pigestiontutorias.models;

import java.sql.Date;

public class SesionGrupal {
    private int idSesionGrupal;
    private int idGrupo;
    private int idTutor;
    private Date fecha;
    private String hora;
    private String temasTratados;
    private String acuerdos;
    private String asesoriasGrupales; // puede ser null
    private String estado;

    // Campo transitorio (no es columna de SESION_GRUPAL): transporta las matriculas
    // que asistieron para que el DAO las inserte en ASISTENCIA junto con la sesion.
    private String[] asistentes;

    public SesionGrupal() {}

    public int getIdSesionGrupal() { return idSesionGrupal; }
    public void setIdSesionGrupal(int idSesionGrupal) { this.idSesionGrupal = idSesionGrupal; }

    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getTemasTratados() { return temasTratados; }
    public void setTemasTratados(String temasTratados) { this.temasTratados = temasTratados; }

    public String getAcuerdos() { return acuerdos; }
    public void setAcuerdos(String acuerdos) { this.acuerdos = acuerdos; }

    public String getAsesoriasGrupales() { return asesoriasGrupales; }
    public void setAsesoriasGrupales(String asesoriasGrupales) { this.asesoriasGrupales = asesoriasGrupales; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String[] getAsistentes() { return asistentes; }
    public void setAsistentes(String[] asistentes) { this.asistentes = asistentes; }
}
