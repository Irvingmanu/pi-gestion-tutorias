package mx.edu.utez.pigestiontutorias.models;

import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;

import java.util.List;

// Contenedor de todo lo que necesitan ReporteExcelBuilder/ReportePdfBuilder para armar la
// exportacion global del coordinador: junta el mismo ReporteResumen que ya usan las tarjetas/
// graficas, el avance grupal y el detalle de atenciones/canalizaciones que ya usan los modales
// de "Tutorías Grupales", "Alumnos Atendidos" y "Alumnos Canalizados", para que el archivo
// exportado sea consistente con lo que el coordinador ve en pantalla.
public class ReporteExportDatos {
    private final ReportesDao.ReporteResumen resumen;
    private final String periodoVigenteNombre;
    private final int objetivoGrupal;
    private final List<AvanceTutorGrupal> avanceGrupal;
    private final List<AtencionAlumnoDTO> atenciones;
    private final List<CanalizacionAlumnoDTO> canalizaciones;
    private final String tituloPeriodo;
    private final String nombreCarrera;
    private final String nombreCuatrimestre;
    private final String nombreGrupo;
    private final String nombreTutor;
    private final String nombreAlumno;
    private final byte[] imagenPastel;
    private final byte[] imagenBarras;

    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              byte[] imagenPastel, byte[] imagenBarras) {
        this(resumen, periodoVigenteNombre, objetivoGrupal, avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor, null, imagenPastel, imagenBarras);
    }

    // Sobrecarga con nombreAlumno: cuando el buscador de alumnos del dashboard tiene un
    // alumno seleccionado, el encabezado del Excel/PDF exportado lo indica junto al resto
    // de los filtros (Carrera/Cuatrimestre/Grupo/Tutor).
    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              String nombreAlumno, byte[] imagenPastel, byte[] imagenBarras) {
        this.resumen = resumen;
        this.periodoVigenteNombre = periodoVigenteNombre;
        this.objetivoGrupal = objetivoGrupal;
        this.avanceGrupal = avanceGrupal;
        this.atenciones = atenciones;
        this.canalizaciones = canalizaciones;
        this.tituloPeriodo = tituloPeriodo;
        this.nombreCarrera = nombreCarrera;
        this.nombreCuatrimestre = nombreCuatrimestre;
        this.nombreGrupo = nombreGrupo;
        this.nombreTutor = nombreTutor;
        this.nombreAlumno = nombreAlumno;
        this.imagenPastel = imagenPastel;
        this.imagenBarras = imagenBarras;
    }

    public ReportesDao.ReporteResumen getResumen() { return resumen; }
    public String getPeriodoVigenteNombre() { return periodoVigenteNombre; }
    public int getObjetivoGrupal() { return objetivoGrupal; }
    public List<AvanceTutorGrupal> getAvanceGrupal() { return avanceGrupal; }
    public List<AtencionAlumnoDTO> getAtenciones() { return atenciones; }
    public List<CanalizacionAlumnoDTO> getCanalizaciones() { return canalizaciones; }
    public String getTituloPeriodo() { return tituloPeriodo; }
    public String getNombreCarrera() { return nombreCarrera; }
    public String getNombreCuatrimestre() { return nombreCuatrimestre; }
    public String getNombreGrupo() { return nombreGrupo; }
    public String getNombreTutor() { return nombreTutor; }
    public String getNombreAlumno() { return nombreAlumno; }
    public byte[] getImagenPastel() { return imagenPastel; }
    public byte[] getImagenBarras() { return imagenBarras; }
}
