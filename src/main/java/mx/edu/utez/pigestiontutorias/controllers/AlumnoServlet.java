package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.CarreraDao;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.PeriodoEscolarDao;
import mx.edu.utez.pigestiontutorias.utils.GeneradorMatricula;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Servlet que gestiona el módulo de Grupos y Alumnos del sistema de tutorías:
 * alta, edición, baja lógica y reactivación de alumnos, creación de grupos
 * independientes y carga masiva de alumnos mediante archivos Excel.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
@WebServlet(name = "AlumnoServlet", value = "/gestion-grupos")
@MultipartConfig(maxFileSize = AlumnoServlet.MAX_TAMANO_ARCHIVO_EXCEL)
public class AlumnoServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_MATRICULA = "^[a-zA-Z0-9]{10}$";

    private static final int MAX_NOMBRES = 100;
    private static final int MAX_APELLIDO = 50;
    private static final int MAX_CORREO = 100;

    static final long MAX_TAMANO_ARCHIVO_EXCEL = 5L * 1024 * 1024;

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final AsignacionTutorDao asignacionTutorDAO = new AsignacionTutorDao();
    private final GrupoDao grupoDao = new GrupoDao();
    private final PeriodoEscolarDao periodoEscolarDao = new PeriodoEscolarDao();
    private final AcademiaDao academiaDao = new AcademiaDao();
    private final CarreraDao carreraDao = new CarreraDao();

    /**
     * Atiende las peticiones GET del módulo de gestión de grupos y alumnos.
     * Según el parámetro "accion" enruta hacia la agenda del alumno, elimina
     * o reactiva un alumno, prepara el formulario de alta/edición, o bien
     * carga el listado general de alumnos, grupos, géneros, academias,
     * carreras y periodos para la vista de gestión de grupos.
     * @param request petición HTTP con el parámetro "accion" y sus datos asociados
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("agenda".equals(accion)) {

            String matricula = request.getParameter("matricula");
            Alumno alumnoConsultado = alumnoDAO.getById(matricula);

            List<EventoAgenda> listaEventos = (alumnoConsultado != null && alumnoConsultado.getIdGrupo() != null)
                    ? alumnoDAO.getAgendaAlumno(alumnoConsultado.getMatricula(), alumnoConsultado.getIdGrupo())
                    : java.util.Collections.emptyList();

            request.setAttribute("listaEventosAgenda", listaEventos);
            request.getRequestDispatcher("/alumno/agenda.jsp").forward(request, response);
            return;
        }

        if ("eliminar".equals(accion)) {
            boolean eliminado = alumnoDAO.delete(request.getParameter("matricula"));
            String parametro = eliminado ? "exito=eliminado" : "error=alumno_en_uso";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("reactivar".equals(accion)) {
            boolean reactivado = alumnoDAO.reactivar(request.getParameter("matricula"));
            String parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("nuevo".equals(accion) || "prepararEdicion".equals(accion)) {
            Alumno alumnoEdit = "prepararEdicion".equals(accion)
                    ? alumnoDAO.getPerfilCompleto(request.getParameter("matricula"))
                    : null;
            forwardAFormulario(request, response, alumnoEdit, null, null);
            return;
        }

        List<Genero> listaGeneros = alumnoDAO.getAllGeneros();

        Map<Integer, String> nombresGenero = new HashMap<>();
        for (Genero genero : listaGeneros) {
            nombresGenero.put(genero.getId(), genero.getNombre());
        }

        Map<Integer, String> tutoresPorGrupo = new HashMap<>();
        for (AsignacionTutor asignacion : asignacionTutorDAO.getAll()) {
            tutoresPorGrupo.put(asignacion.getIdGrupo(), asignacion.getNombresTutor() + " " + asignacion.getApellidosTutor());
        }

        List<Grupo> listaGrupos = grupoDao.getAll();
        Map<Integer, Grupo> gruposPorId = new HashMap<>();
        for (Grupo grupo : listaGrupos) {
            gruposPorId.put(grupo.getIdGrupo(), grupo);
        }

        request.setAttribute("listaAlumnos", alumnoDAO.getAll());
        request.setAttribute("listaGrupos", listaGrupos);
        request.setAttribute("gruposPorId", gruposPorId);
        request.setAttribute("nombresGenero", nombresGenero);
        request.setAttribute("tutoresPorGrupo", tutoresPorGrupo);

        request.setAttribute("listaAcademias", academiaDao.getAll());
        request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());

        request.setAttribute("listaPeriodos", periodoEscolarDao.getActivos());

        request.getRequestDispatcher("/coordinador/gestion-grupos.jsp").forward(request, response);
    }

    /**
     * Atiende las peticiones POST del módulo de gestión de grupos y alumnos.
     * Según el parámetro "accion" elimina o reactiva un alumno, crea un grupo
     * independiente, procesa la carga masiva de alumnos por Excel, o bien
     * valida y guarda (alta o edición) los datos de un alumno individual,
     * verificando duplicados de matrícula, correo y teléfono antes de persistir.
     * @param request petición HTTP con el parámetro "accion" y los datos del alumno o grupo
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            boolean eliminado = alumnoDAO.delete(request.getParameter("matricula"));
            String parametro = eliminado ? "exito=eliminado" : "error=alumno_en_uso";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("reactivar".equals(accion)) {
            boolean reactivado = alumnoDAO.reactivar(request.getParameter("matricula"));
            String parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("crearGrupo".equals(accion)) {
            crearGrupoIndependiente(request, response);
            return;
        }

        if ("cargaMasivaAlumnos".equals(accion)) {
            procesarCargaMasivaAlumnos(request, response);
            return;
        }

        String matricula = request.getParameter("matricula");

        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula != null ? matricula.trim().toUpperCase() : null);
        alumno.setNombres(trimOrNull(request.getParameter("nombres")));
        alumno.setApellidoPaterno(trimOrNull(request.getParameter("apellidoPaterno")));
        alumno.setApellidoMaterno(trimOrNull(request.getParameter("apellidoMaterno")));
        alumno.setCorreoInstitucional(trimOrNull(request.getParameter("correo")));
        alumno.setTelefono(trimOrNull(request.getParameter("telefono")));
        alumno.setIdGenero(parseIntOrNull(request.getParameter("idGenero")));

        boolean formatoValido = alumno.getMatricula() != null && alumno.getMatricula().matches(REGEX_MATRICULA)
                && alumno.getNombres() != null && alumno.getNombres().matches(REGEX_NOMBRE)
                && alumno.getNombres().length() <= MAX_NOMBRES
                && alumno.getApellidoPaterno() != null && alumno.getApellidoPaterno().matches(REGEX_NOMBRE)
                && alumno.getApellidoPaterno().length() <= MAX_APELLIDO
                && (alumno.getApellidoMaterno() == null || alumno.getApellidoMaterno().isBlank()
                || (alumno.getApellidoMaterno().matches(REGEX_NOMBRE) && alumno.getApellidoMaterno().length() <= MAX_APELLIDO))
                && alumno.getTelefono() != null && alumno.getTelefono().matches(REGEX_TELEFONO)
                && alumno.getCorreoInstitucional() != null && alumno.getCorreoInstitucional().matches(REGEX_CORREO)
                && alumno.getCorreoInstitucional().length() <= MAX_CORREO
                && alumno.getIdGenero() != null;

        if (!formatoValido) {
            forwardAFormulario(request, response, null, alumno, "formato_invalido");
            return;
        }

        Integer idGrupo = parseIntOrNull(request.getParameter("idGrupo"));
        boolean grupoValido = idGrupo != null && grupoDao.getById(idGrupo) != null;

        if (!grupoValido) {
            forwardAFormulario(request, response, null, alumno, "grupo_invalido");
            return;
        }
        alumno.setIdGrupo(idGrupo);

        boolean esEdicion = "editar".equals(accion);
        String errorDuplicado = null;

        if (esEdicion) {
            if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional(), alumno.getMatricula())) {
                errorDuplicado = "correo_duplicado";
            } else if (alumnoDAO.existeTelefono(alumno.getTelefono(), alumno.getMatricula())) {
                errorDuplicado = "telefono_duplicado";
            }
        } else {
            if (alumnoDAO.existeMatricula(alumno.getMatricula())) {
                errorDuplicado = "matricula_duplicada";
            } else if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional())) {
                errorDuplicado = "correo_duplicado";
            } else if (alumnoDAO.existeTelefono(alumno.getTelefono())) {
                errorDuplicado = "telefono_duplicado";
            }
        }

        if (errorDuplicado != null) {
            forwardAFormulario(request, response, null, alumno, errorDuplicado);
            return;
        }

        String parametroExito;
        if (esEdicion) {
            alumnoDAO.update(alumno);
            parametroExito = "editado";
        } else {
            alumnoDAO.create(alumno);
            parametroExito = "guardado";
        }

        response.sendRedirect(request.getContextPath() + "/gestion-grupos?exito=" + parametroExito);
    }

    /**
     * Crea un grupo de forma independiente (sin pasar por la carga masiva de alumnos),
     * validando carrera, cuatrimestre acorde al nivel de la carrera, letra de grupo,
     * año de inicio y periodo escolar activo, y evitando grupos duplicados.
     * @param request petición HTTP con los parámetros idCarrera, cuatrimestre, letra, anioInicio e idPeriodo
     * @param response respuesta HTTP usada para redirigir con el resultado de la operación (éxito o error)
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
    private void crearGrupoIndependiente(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letraParametro = request.getParameter("letra");
        String letra = letraParametro != null ? letraParametro.trim().toUpperCase() : null;
        Integer anioInicio = parseIntOrNull(request.getParameter("anioInicio"));
        Integer idPeriodo = parseIntOrNull(request.getParameter("idPeriodo"));

        Carrera carrera = idCarrera != null ? carreraDao.getById(idCarrera) : null;
        if (carrera == null) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_carrera_invalida");
            return;
        }

        if (cuatrimestre == null || !esCuatrimestreValidoParaNivel(cuatrimestre, carrera.getNivel())) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_cuatrimestre_invalido");
            return;
        }

        if (letra == null || !letra.matches("^[A-F]$")) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_letra_invalida");
            return;
        }

        int anioActual = java.time.Year.now().getValue();
        if (anioInicio == null || anioInicio < anioActual - 5 || anioInicio > anioActual + 1) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_anio_invalido");
            return;
        }

        PeriodoEscolar periodoElegido = idPeriodo != null ? periodoEscolarDao.getById(idPeriodo) : null;
        if (periodoElegido == null || !"S".equals(periodoElegido.getEstado())) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_periodo_invalido");
            return;
        }

        int duracionAnios = cuatrimestre <= 6 ? 2 : 1;
        String generacion = anioInicio + "-" + (anioInicio + duracionAnios);

        if (grupoDao.existeGrupo(idCarrera, cuatrimestre, letra, periodoElegido.getIdPeriodo())) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_duplicado");
            return;
        }

        Integer idGrupo = grupoDao.findOrCreate(idCarrera, cuatrimestre, letra, periodoElegido.getIdPeriodo(), generacion);
        String parametro = idGrupo != null ? "exito=grupo_creado" : "error=grupo_creacion_fallida";
        response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
    }

    /**
     * Procesa la carga masiva de alumnos a partir de un archivo Excel adjunto en la petición.
     * Valida el grupo destino y su periodo, lee cada fila de la hoja, genera matrícula
     * automática cuando corresponde (primer cuatrimestre), valida los datos de cada alumno
     * contra duplicados dentro del lote y en el sistema, y finalmente inserta en bloque
     * los alumnos válidos, guardando en sesión el detalle de las filas inválidas.
     * @param request petición HTTP con el parámetro idGrupo y la parte multipart "archivoExcel"
     * @param response respuesta HTTP usada para redirigir con el resultado de la carga (éxito o error)
     * @throws IOException si ocurre un error de entrada/salida al leer el archivo o redirigir la respuesta
     */
    private void procesarCargaMasivaAlumnos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            Integer idGrupo = parseIntOrNull(request.getParameter("idGrupo"));
            Grupo grupo = idGrupo != null ? grupoDao.getById(idGrupo) : null;
            if (grupo == null || !"S".equals(grupo.getEstado())) {
                response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=carga_sin_grupo");
                return;
            }

            PeriodoEscolar periodoDelGrupo = periodoEscolarDao.getById(grupo.getIdPeriodo());
            if (periodoDelGrupo == null) {
                response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=carga_sin_grupo");
                return;
            }

            Part parteArchivo = request.getPart("archivoExcel");
            if (parteArchivo == null || parteArchivo.getSize() == 0) {
                response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=archivo_vacio");
                return;
            }

            Map<String, Integer> idsPorGenero = new HashMap<>();
            for (Genero g : alumnoDAO.getAllGeneros()) {
                if (g.getNombre() != null) {
                    idsPorGenero.put(g.getNombre().trim().toLowerCase(Locale.ROOT), g.getId());
                }
            }

            String prefijoMatricula = GeneradorMatricula.construirPrefijo(grupo.getNombreCarrera(), periodoDelGrupo.getFechaInicio());
            int siguienteContador = alumnoDAO.obtenerSiguienteContador(prefijoMatricula.toUpperCase(Locale.ROOT));
            boolean esPrimerCuatrimestre = grupo.getCuatrimestre() == 1;

            List<Alumno> alumnosValidos = new ArrayList<>();

            List<String> filasInvalidas = new ArrayList<>();

            Set<String> matriculasEnLote = new HashSet<>();
            Set<String> correosEnLote = new HashSet<>();
            Set<String> telefonosEnLote = new HashSet<>();

            try (InputStream entrada = parteArchivo.getInputStream();
                 Workbook libro = WorkbookFactory.create(entrada)) {

                Sheet hoja = libro.getSheetAt(0);

                for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                    Row fila = hoja.getRow(f);
                    if (fila == null || esTextoVacio(obtenerTexto(fila.getCell(1)))) continue;

                    Alumno alumno = new Alumno();

                    String matricula = obtenerTexto(fila.getCell(0));
                    if (esTextoVacio(matricula)) {
                        if (esPrimerCuatrimestre) {
                            matricula = prefijoMatricula + String.format(Locale.ROOT, "%03d", siguienteContador);
                            siguienteContador++;
                        } else {

                            matricula = null;
                        }
                    } else {
                        matricula = matricula.trim().toUpperCase(Locale.ROOT);
                    }
                    alumno.setMatricula(matricula);

                    alumno.setNombres(obtenerTexto(fila.getCell(1)));
                    alumno.setApellidoPaterno(obtenerTexto(fila.getCell(2)));
                    alumno.setApellidoMaterno(obtenerTexto(fila.getCell(3)));

                    String correo = obtenerTexto(fila.getCell(4));
                    if (esTextoVacio(correo) && alumno.getMatricula() != null) {
                        correo = alumno.getMatricula().toLowerCase(Locale.ROOT) + "@utez.edu.mx";
                    }
                    alumno.setCorreoInstitucional(correo);

                    String generoTexto = obtenerTexto(fila.getCell(5));
                    alumno.setIdGenero(generoTexto != null ? idsPorGenero.get(generoTexto.trim().toLowerCase(Locale.ROOT)) : null);

                    String telefono = obtenerTexto(fila.getCell(6));
                    alumno.setTelefono(telefono != null ? telefono.replaceAll("[^0-9]", "") : null);

                    alumno.setIdGrupo(idGrupo);

                    String motivo = validarFilaAlumno(alumno, matriculasEnLote, correosEnLote, telefonosEnLote);
                    if (motivo == null) {
                        alumnosValidos.add(alumno);
                    } else {

                        filasInvalidas.add("Fila " + (f + 1) + ": " + motivo);
                    }
                }
            }

            if (!filasInvalidas.isEmpty()) {
                request.getSession().setAttribute("filasInvalidasExcel", filasInvalidas);
            }

            if (alumnosValidos.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=archivo_invalido");
                return;
            }

            int insertados = alumnoDAO.crearMasivo(alumnosValidos, idGrupo);
            String parametro = insertados > 0
                    ? "exito=carga_masiva_alumnos&insertados=" + insertados + "&conError=" + filasInvalidas.size()
                    : "error=carga_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);

        } catch (IllegalStateException e) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=archivo_muy_grande");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=archivo_invalido");
        }
    }

    /**
     * Valida los datos de un alumno leído desde una fila del archivo Excel de carga masiva,
     * verificando formato de nombre, apellidos, matrícula, correo, teléfono y género, así como
     * duplicados dentro del propio lote y contra los registros ya existentes en el sistema.
     * @param alumno alumno construido a partir de la fila del Excel a validar
     * @param matriculasEnLote conjunto de matrículas ya vistas en el lote actual, usado para detectar duplicados
     * @param correosEnLote conjunto de correos ya vistos en el lote actual, usado para detectar duplicados
     * @param telefonosEnLote conjunto de teléfonos ya vistos en el lote actual, usado para detectar duplicados
     * @return el mensaje de error correspondiente si el alumno es inválido, o {@code null} si es válido
     */
    private String validarFilaAlumno(Alumno alumno, Set<String> matriculasEnLote, Set<String> correosEnLote, Set<String> telefonosEnLote) {
        if (esTextoVacio(alumno.getNombres()) || !alumno.getNombres().matches(REGEX_NOMBRE) || alumno.getNombres().length() > MAX_NOMBRES) {
            return "el nombre '" + alumno.getNombres() + "' es inválido o está vacío.";
        }
        if (esTextoVacio(alumno.getApellidoPaterno()) || !alumno.getApellidoPaterno().matches(REGEX_NOMBRE) || alumno.getApellidoPaterno().length() > MAX_APELLIDO) {
            return "el apellido paterno '" + alumno.getApellidoPaterno() + "' es inválido o está vacío.";
        }

        if (esTextoVacio(alumno.getApellidoMaterno()) || !alumno.getApellidoMaterno().matches(REGEX_NOMBRE) || alumno.getApellidoMaterno().length() > MAX_APELLIDO) {
            return "el apellido materno '" + alumno.getApellidoMaterno() + "' es inválido o está vacío (es obligatorio en la carga masiva).";
        }
        if (alumno.getMatricula() == null) {
            return "falta la matrícula (obligatoria para alumnos de 2° cuatrimestre en adelante, ya que ya tienen una de su generación original).";
        }
        if (!alumno.getMatricula().matches(REGEX_MATRICULA)) {
            return "la matrícula '" + alumno.getMatricula() + "' no tiene el formato válido (10 caracteres alfanuméricos).";
        }
        if (alumno.getCorreoInstitucional() == null || !alumno.getCorreoInstitucional().matches(REGEX_CORREO) || alumno.getCorreoInstitucional().length() > MAX_CORREO) {
            return "el correo '" + alumno.getCorreoInstitucional() + "' no es válido (debe terminar en @utez.edu.mx).";
        }
        if (alumno.getTelefono() == null || !alumno.getTelefono().matches(REGEX_TELEFONO)) {
            return "el teléfono '" + alumno.getTelefono() + "' debe tener exactamente 10 dígitos.";
        }
        if (alumno.getIdGenero() == null) {
            return "el género no coincide con ninguno del catálogo (usa Masculino, Femenino u Otro).";
        }

        if (!matriculasEnLote.add(alumno.getMatricula())) {
            return "la matrícula '" + alumno.getMatricula() + "' está repetida dentro del mismo archivo.";
        }
        if (!correosEnLote.add(alumno.getCorreoInstitucional().toLowerCase(Locale.ROOT))) {
            return "el correo '" + alumno.getCorreoInstitucional() + "' está repetido dentro del mismo archivo.";
        }
        if (!telefonosEnLote.add(alumno.getTelefono())) {
            return "el teléfono '" + alumno.getTelefono() + "' está repetido dentro del mismo archivo.";
        }

        if (alumnoDAO.existeMatricula(alumno.getMatricula())) {
            return "la matrícula '" + alumno.getMatricula() + "' ya está registrada en el sistema.";
        }
        if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional())) {
            return "el correo '" + alumno.getCorreoInstitucional() + "' ya está registrado en el sistema.";
        }
        if (alumnoDAO.existeTelefono(alumno.getTelefono())) {
            return "el teléfono '" + alumno.getTelefono() + "' ya está registrado en el sistema.";
        }
        return null;
    }

    /**
     * Determina si una cadena de texto es nula o está en blanco.
     * @param texto el texto a evaluar
     * @return {@code true} si el texto es {@code null} o está en blanco; {@code false} en caso contrario
     */
    private static boolean esTextoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /**
     * Obtiene el valor de texto de una celda de Excel, sin importar si su contenido
     * está almacenado como texto o como número (en cuyo caso se convierte a cadena,
     * evitando notación decimal cuando el valor es entero).
     * @param celda la celda de la hoja de Excel de la cual extraer el texto
     * @return el texto contenido en la celda, recortado de espacios, o {@code null} si la celda es nula
     */
    private static String obtenerTexto(Cell celda) {
        if (celda == null) return null;

        if (celda.getCellType() == CellType.NUMERIC) {
            double valor = celda.getNumericCellValue();
            if (valor == Math.floor(valor) && !Double.isInfinite(valor)) {
                return String.valueOf((long) valor);
            }
            return String.valueOf(valor);
        }

        String valor = celda.getStringCellValue();
        return valor != null ? valor.trim() : null;
    }

    /**
     * Verifica que un cuatrimestre sea válido de acuerdo con el nivel académico de la carrera:
     * TSU admite cuatrimestres del 1 al 6, e Ingeniería del 7 al 10.
     * @param cuatrimestre el número de cuatrimestre a validar
     * @param nivel el nivel académico de la carrera ("TSU" o "ING")
     * @return {@code true} si el cuatrimestre es válido para el nivel indicado; {@code false} en caso contrario
     */
    private boolean esCuatrimestreValidoParaNivel(int cuatrimestre, String nivel) {
        if ("TSU".equals(nivel)) {
            return cuatrimestre >= 1 && cuatrimestre <= 6;
        }
        if ("ING".equals(nivel)) {
            return cuatrimestre >= 7 && cuatrimestre <= 10;
        }
        return false;
    }

    /**
     * Recorta los espacios en blanco de una cadena, preservando {@code null}.
     * @param valor el valor a recortar
     * @return el valor recortado, o {@code null} si el valor original era {@code null}
     */
    private String trimOrNull(String valor) {
        return valor != null ? valor.trim() : null;
    }

    /**
     * Convierte una cadena de texto a entero de forma segura.
     * @param valor el texto a convertir
     * @return el valor entero resultante, o {@code null} si el texto es nulo, está en blanco o no es numérico
     */
    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reenvía la petición al formulario de alta/edición de alumno, cargando los datos
     * del alumno a editar o los datos con error capturados, junto con el catálogo de
     * géneros, la lista de grupos y el mensaje de error correspondiente.
     * @param request petición HTTP que será reenviada al formulario
     * @param response respuesta HTTP usada para reenviar la petición a la vista JSP
     * @param alumnoEdit el alumno a editar cuando se prepara una edición, o {@code null} si no aplica
     * @param alumnoConError el alumno con los datos capturados que generaron un error de validación, o {@code null} si no aplica
     * @param codigoError el código del error a mostrar en el formulario, o {@code null} si no hay error
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void forwardAFormulario(HttpServletRequest request, HttpServletResponse response,
                                    Alumno alumnoEdit, Alumno alumnoConError, String codigoError)
            throws ServletException, IOException {
        Alumno alumnoFormulario = alumnoEdit != null ? alumnoEdit : alumnoConError;
        boolean esEdicion = alumnoEdit != null || "editar".equals(request.getParameter("accion"));

        request.setAttribute("alumnoFormulario", alumnoFormulario);
        request.setAttribute("esEdicion", esEdicion);
        request.setAttribute("tituloBanner", esEdicion ? "Editar Alumno" : "Nuevo Alumno");
        request.setAttribute("mensajeError", resolverMensajeError(codigoError));
        request.setAttribute("listaGeneros", alumnoDAO.getAllGeneros());

        request.setAttribute("listaGrupos", grupoDao.getAll());

        request.getRequestDispatcher("/coordinador/formulario-alumno.jsp").forward(request, response);
    }

    /**
     * Traduce un código de error interno en el mensaje descriptivo que se mostrará al usuario
     * en el formulario de alta/edición de alumno.
     * @param codigoError el código del error a traducir
     * @return el mensaje de error correspondiente al código, o {@code null} si el código no coincide con ninguno conocido
     */
    private String resolverMensajeError(String codigoError) {
        if ("matricula_duplicada".equals(codigoError)) {
            return "Esta matrícula ya está registrada en el sistema.";
        }
        if ("correo_duplicado".equals(codigoError)) {
            return "Este correo ya está registrado en el sistema.";
        }
        if ("telefono_duplicado".equals(codigoError)) {
            return "Este número de teléfono ya está registrado en el sistema.";
        }
        if ("formato_invalido".equals(codigoError)) {
            return "Verifica los datos. El formato de uno o más campos es incorrecto.";
        }
        if ("grupo_invalido".equals(codigoError)) {
            return "Selecciona un grupo válido. Si el grupo que necesitas no aparece en la lista, créalo primero desde Gestión de Grupos.";
        }
        return null;
    }

}
