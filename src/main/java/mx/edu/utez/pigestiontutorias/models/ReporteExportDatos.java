package mx.edu.utez.pigestiontutorias.models;

import mx.edu.utez.pigestiontutorias.models.dao.ReportesDao;

import java.util.List;

/**
 * Agrupa todos los datos necesarios para exportar un reporte de tutorías a Excel
 * o PDF: el resumen general, el avance grupal, las atenciones, las canalizaciones,
 * los filtros aplicados y las imágenes de las gráficas ya renderizadas.
 * @author 20253ds074-art
 * @version 1.0
 * @since 2026-08-16
 */
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

    /**
     * Datos académicos del alumno filtrado (buscador de alumnos del dashboard), resueltos
     * server-side a partir de ALUMNO + ALUMNO_GRUPO_HISTORICO (ver {@code AlumnoDAO.getTrayectoriaPorAlumno})
     * para que el encabezado del Excel/PDF muestre carrera/nivel/cuatrimestre-grupo/generación
     * reales en vez de solo el nombre. Queda en {@code null} cuando no hay alumno filtrado.
     */
    public static class DatosAcademicosAlumno {
        private final String matricula;
        private final String nombreCompleto;
        private final String carrera;
        private final String nivel;
        private final String cuatrimestreGrupo;
        private final String generacion;

        /**
         * Construye los datos académicos resueltos del alumno filtrado.
         * @param matricula la matrícula del alumno
         * @param nombreCompleto el nombre completo del alumno
         * @param carrera el nombre de la carrera del alumno
         * @param nivel el nivel académico de la carrera (TSU o ING)
         * @param cuatrimestreGrupo el cuatrimestre y grupo vigente del alumno
         * @param generacion la generación del grupo vigente del alumno
         */
        public DatosAcademicosAlumno(String matricula, String nombreCompleto, String carrera, String nivel,
                                     String cuatrimestreGrupo, String generacion) {
            this.matricula = matricula;
            this.nombreCompleto = nombreCompleto;
            this.carrera = carrera;
            this.nivel = nivel;
            this.cuatrimestreGrupo = cuatrimestreGrupo;
            this.generacion = generacion;
        }

        /** @return la matrícula del alumno */
        public String getMatricula() { return matricula; }
        /** @return el nombre completo del alumno */
        public String getNombreCompleto() { return nombreCompleto; }
        /** @return el nombre de la carrera del alumno */
        public String getCarrera() { return carrera; }
        /** @return el nivel académico de la carrera */
        public String getNivel() { return nivel; }
        /** @return el cuatrimestre y grupo vigente del alumno */
        public String getCuatrimestreGrupo() { return cuatrimestreGrupo; }
        /** @return la generación del grupo vigente del alumno */
        public String getGeneracion() { return generacion; }
    }

    /**
     * Construye los datos de exportación sin alumno filtrado ni datos académicos.
     * @param resumen el resumen general del reporte
     * @param periodoVigenteNombre el nombre del periodo escolar vigente
     * @param objetivoGrupal el objetivo de asistencias grupales del periodo
     * @param avanceGrupal el avance grupal por tutor
     * @param atenciones las atenciones individuales a mostrar
     * @param canalizaciones las canalizaciones a mostrar
     * @param tituloPeriodo el título del periodo filtrado
     * @param nombreCarrera el nombre de la carrera filtrada
     * @param nombreCuatrimestre el nombre del cuatrimestre filtrado
     * @param nombreGrupo el nombre del grupo filtrado
     * @param nombreTutor el nombre del tutor filtrado
     * @param imagenPastel la imagen de la gráfica de pastel ya renderizada
     * @param imagenBarras la imagen de la gráfica de barras ya renderizada
     */
    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              byte[] imagenPastel, byte[] imagenBarras) {
        this(resumen, periodoVigenteNombre, objetivoGrupal, avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor, null, null, imagenPastel, imagenBarras);
    }

    /**
     * Construye los datos de exportación con el nombre del alumno filtrado, sin datos académicos resueltos.
     * @param resumen el resumen general del reporte
     * @param periodoVigenteNombre el nombre del periodo escolar vigente
     * @param objetivoGrupal el objetivo de asistencias grupales del periodo
     * @param avanceGrupal el avance grupal por tutor
     * @param atenciones las atenciones individuales a mostrar
     * @param canalizaciones las canalizaciones a mostrar
     * @param tituloPeriodo el título del periodo filtrado
     * @param nombreCarrera el nombre de la carrera filtrada
     * @param nombreCuatrimestre el nombre del cuatrimestre filtrado
     * @param nombreGrupo el nombre del grupo filtrado
     * @param nombreTutor el nombre del tutor filtrado
     * @param nombreAlumno el nombre del alumno filtrado
     * @param imagenPastel la imagen de la gráfica de pastel ya renderizada
     * @param imagenBarras la imagen de la gráfica de barras ya renderizada
     */
    public ReporteExportDatos(ReportesDao.ReporteResumen resumen, String periodoVigenteNombre, int objetivoGrupal,
                              List<AvanceTutorGrupal> avanceGrupal, List<AtencionAlumnoDTO> atenciones,
                              List<CanalizacionAlumnoDTO> canalizaciones, String tituloPeriodo,
                              String nombreCarrera, String nombreCuatrimestre, String nombreGrupo, String nombreTutor,
                              String nombreAlumno, byte[] imagenPastel, byte[] imagenBarras) {
        this(resumen, periodoVigenteNombre, objetivoGrupal, avanceGrupal, atenciones, canalizaciones, tituloPeriodo,
                nombreCarrera, nombreCuatrimestre, nombreGrupo, nombreTutor, nombreAlumno, null, imagenPastel, imagenBarras);
    }

    /**
     * Construye los datos de exportación completos, incluyendo los datos académicos
     * resueltos del alumno filtrado (matrícula, carrera, nivel, cuatrimestre-grupo,
     * generación) para el encabezado enriquecido del Excel/PDF.
     * @param resumen el resumen general del reporte
     * @param periodoVigenteNombre el nombre del periodo escolar vigente
     * @param objetivoGrupal el objetivo de asistencias grupales del periodo
     * @param avanceGrupal el avance grupal por tutor
     * @param atenciones las atenciones individuales a mostrar
     * @param canalizaciones las canalizaciones a mostrar
     * @param tituloPeriodo el título del periodo filtrado
     * @param nombreCarrera el nombre de la carrera filtrada
     * @param nombreCuatrimestre el nombre del cuatrimestre filtrado
     * @param nombreGrupo el nombre del grupo filtrado
     * @param nombreTutor el nombre del tutor filtrado
     * @param nombreAlumno el nombre del alumno filtrado
     * @param datosAlumno los datos académicos resueltos del alumno filtrado, o {@code null} si no aplica
     * @param imagenPastel la imagen de la gráfica de pastel ya renderizada
     * @param imagenBarras la imagen de la gráfica de barras ya renderizada
     */
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

    /** @return el resumen general del reporte */
    public ReportesDao.ReporteResumen getResumen() { return resumen; }
    /** @return el nombre del periodo escolar vigente */
    public String getPeriodoVigenteNombre() { return periodoVigenteNombre; }
    /** @return el objetivo de asistencias grupales del periodo */
    public int getObjetivoGrupal() { return objetivoGrupal; }
    /** @return el avance grupal por tutor */
    public List<AvanceTutorGrupal> getAvanceGrupal() { return avanceGrupal; }
    /** @return las atenciones individuales a mostrar */
    public List<AtencionAlumnoDTO> getAtenciones() { return atenciones; }
    /** @return las canalizaciones a mostrar */
    public List<CanalizacionAlumnoDTO> getCanalizaciones() { return canalizaciones; }
    /** @return el título del periodo filtrado */
    public String getTituloPeriodo() { return tituloPeriodo; }
    /** @return el nombre de la carrera filtrada */
    public String getNombreCarrera() { return nombreCarrera; }
    /** @return el nombre del cuatrimestre filtrado */
    public String getNombreCuatrimestre() { return nombreCuatrimestre; }
    /** @return el nombre del grupo filtrado */
    public String getNombreGrupo() { return nombreGrupo; }
    /** @return el nombre del tutor filtrado */
    public String getNombreTutor() { return nombreTutor; }
    /** @return el nombre del alumno filtrado */
    public String getNombreAlumno() { return nombreAlumno; }
    /** @return los datos académicos resueltos del alumno filtrado, o {@code null} si no aplica */
    public DatosAcademicosAlumno getDatosAlumno() { return datosAlumno; }
    /** @return la imagen de la gráfica de pastel ya renderizada */
    public byte[] getImagenPastel() { return imagenPastel; }
    /** @return la imagen de la gráfica de barras ya renderizada */
    public byte[] getImagenBarras() { return imagenBarras; }
}
