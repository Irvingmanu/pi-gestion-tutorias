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
    private final DatosAcademicosAlumno datosAlumno;
    private final byte[] imagenPastel;
    private final byte[] imagenBarras;

    // Datos academicos del alumno filtrado (buscador de alumnos del dashboard), resueltos
    // server-side a partir de ALUMNO + ALUMNO_GRUPO_HISTORICO (ver AlumnoDAO.getTrayectoriaPorAlumno)
    // para que el encabezado del Excel/PDF muestre carrera/nivel/cuatrimestre-grupo/generacion
    // reales en vez de solo el nombre. Queda en null cuando no hay alumno filtrado.
    public static class DatosAcademicosAlumno {
        private final String matricula;
        private final String nombreCompleto;
        private final String carrera;
        private final String nivel;
        private final String cuatrimestreGrupo;
        private final String generacion;

        public DatosAcademicosAlumno(String matricula, String nombreCompleto, String carrera, String nivel,
                                     String cuatrimestreGrupo, String generacion) {
            this.matricula = matricula;
            this.nombreCompleto = nombreCompleto;
            this.carrera = carrera;
            this.nivel = nivel;
            this.cuatrimestreGrupo = cuatrimestreGrupo;
            this.generacion = generacion;
        }

        public String getMatricula() { return matricula; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getCarrera() { return carrera; }
        public String getNivel() { return nivel; }
        public String getCuatrimestreGrupo() { return cuatrimestreGrupo; }
        public String getGeneracion() { return generacion; }
    }

    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              byte[] imagenPastel, byte[] imagenBarras) {
        this(resumen, periodoVigenteNombre, objetivoGrupal, avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor, null, null, imagenPastel, imagenBarras);
    }

    // Sobrecarga con nombreAlumno: cuando el buscador de alumnos del dashboard tiene un
    // alumno seleccionado, el encabezado del Excel/PDF exportado lo indica junto al resto
    // de los filtros (Carrera/Cuatrimestre/Grupo/Tutor).
    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              String nombreAlumno, byte[] imagenPastel, byte[] imagenBarras) {
        this(resumen, periodoVigenteNombre, objetivoGrupal, avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor, nombreAlumno, null, imagenPastel, imagenBarras);
    }

    // Sobrecarga con datosAlumno: datos academicos completos del alumno filtrado (matricula,
    // carrera, nivel, cuatrimestre-grupo, generacion) para el encabezado enriquecido.
    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              String nombreAlumno, DatosAcademicosAlumno datosAlumno,
                              byte[] imagenPastel, byte[] imagenBarras) {
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
        this.datosAlumno = datosAlumno;
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
    public DatosAcademicosAlumno getDatosAlumno() { return datosAlumno; }
    public byte[] getImagenPastel() { return imagenPastel; }
    public byte[] getImagenBarras() { return imagenBarras; }
}
