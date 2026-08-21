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

// Carrera+Cuatrimestre+Letra ya no se guardan como 3 FKs sueltas en ALUMNO, y el alta de
// alumno ya no las captura ni resuelve/crea un GRUPO: el formulario solo elige, en
// "Asignar a Grupo", un ID_GRUPO que ya existe (los grupos se crean aparte, desde
// gestion-grupos.jsp / accion=crearGrupo). @MultipartConfig es necesario para poder leer
// request.getPart("archivoExcel") en accion=cargaMasivaAlumnos.
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

    // Limite del Excel de carga masiva (tambien usado en @MultipartConfig, abajo): un
    // archivo de solo texto (matricula/nombre/correo/telefono) nunca deberia acercarse a
    // esto ni con miles de filas; 5MB es margen de sobra contra un archivo corrupto/gigante
    // subido por error, sin ser tan bajo que estorbe un caso legitimo.
    static final long MAX_TAMANO_ARCHIVO_EXCEL = 5L * 1024 * 1024;

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final AsignacionTutorDao asignacionTutorDAO = new AsignacionTutorDao();
    private final GrupoDao grupoDao = new GrupoDao();
    private final PeriodoEscolarDao periodoEscolarDao = new PeriodoEscolarDao();
    private final AcademiaDao academiaDao = new AcademiaDao();
    private final CarreraDao carreraDao = new CarreraDao();

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

        // ==================== LISTADO GENERAL (con filtros) ====================
        List<Genero> listaGeneros = alumnoDAO.getAllGeneros();

        // Mapa de traduccion ID -> nombre de genero, usado por la tabla para mostrar
        // el nombre sin volver a consultar la BD por fila.
        Map<Integer, String> nombresGenero = new HashMap<>();
        for (Genero genero : listaGeneros) {
            nombresGenero.put(genero.getId(), genero.getNombre());
        }

        // Tutor asignado por grupo, para mostrarlo junto al titulo de cada tabla agrupada
        // en gestion-grupos.jsp. La clave es el ID_GRUPO directo (ya no hay que armarla
        // a mano con carrera+cuatrimestre+letra).
        Map<Integer, String> tutoresPorGrupo = new HashMap<>();
        for (AsignacionTutor asignacion : asignacionTutorDAO.getAll()) {
            tutoresPorGrupo.put(asignacion.getIdGrupo(), asignacion.getNombresTutor() + " " + asignacion.getApellidosTutor());
        }

        // Grupo (Carrera + Cuatrimestre + Letra) de cada alumno, indexado por ID_GRUPO:
        // el JSP lo usa para pintar la columna Carrera/Cuatri/Grupo y agrupar la tabla
        // sin tener que resolver el perfil completo de cada alumno.
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
        // Filtro Academia -> Carrera del listado (#academiaFiltroPrincipal / #carreraFiltroPrincipal):
        // listaCarreras trae TODAS las carreras del sistema, no solo las que ya tienen grupos.
        request.setAttribute("listaAcademias", academiaDao.getAll());
        request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());
        // Select "Periodo Escolar" del modal "Nuevo Grupo".
        request.setAttribute("listaPeriodos", periodoEscolarDao.getActivos());

        request.getRequestDispatcher("/coordinador/gestion-grupos.jsp").forward(request, response);
    }

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

        // El grupo ya debe existir (se crea aparte desde gestion-grupos.jsp, modal "Nuevo
        // Grupo"): aqui solo se valida que el ID_GRUPO elegido en "Asignar a Grupo" sea uno
        // real. Se valida por separado (con su propio codigo de error) para no confundirlo
        // con un error de formato en los datos personales: son fallas de naturaleza distinta.
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

    // Alta de un GRUPO independiente (gestion-grupos.jsp, modal "Nuevo Grupo"), sin pasar
    // primero por el alta de un alumno. Letra restringida a A-F (mismo rango fijo que el
    // select de Letra en formulario-alumno.jsp); a diferencia del alta de alumno, aqui SI
    // se distingue "el grupo ya existe" como error, para no confundir al coordinador
    // haciendole creer que genero un grupo nuevo cuando en realidad reuso uno existente.
    private void crearGrupoIndependiente(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letraParametro = request.getParameter("letra");
        String letra = letraParametro != null ? letraParametro.trim().toUpperCase() : null;
        Integer anioInicio = parseIntOrNull(request.getParameter("anioInicio"));
        Integer idPeriodo = parseIntOrNull(request.getParameter("idPeriodo"));

        // Cada campo se valida por separado, con su propio codigo de error, y se corta en el
        // primero que falle (mismo orden en que aparecen en el modal: Carrera, Cuatrimestre,
        // Letra, Año de Inicio, Periodo Escolar). Antes todo caia en un solo
        // "grupo_datos_invalidos" generico y el coordinador tenia que adivinar cual de los 5
        // campos era el problema.
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

        // El coordinador ya no escribe el Año de Fin a mano (evitaba valores absurdos como
        // "2999-2303"): solo captura el Año de Inicio, y el rango se calcula mas abajo segun
        // el Cuatrimestre. El rango permitido (año actual -5 a +1) es defensivo: el
        // <input type="number" min/max> del modal ya lo acota en cliente con los mismos
        // limites (y muestra el rango en su texto de ayuda), pero un POST directo podria
        // saltarselo. El -5 (no -1) es a proposito: un grupo de 6° cuatrimestre o mas
        // registrado HOY pertenece a una generacion que arranco varios años atras (cohortes
        // activas/rezagadas), no solo el año pasado.
        int anioActual = java.time.Year.now().getValue();
        if (anioInicio == null || anioInicio < anioActual - 5 || anioInicio > anioActual + 1) {
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?error=grupo_anio_invalido");
            return;
        }

        // El Periodo Escolar ahora lo elige el coordinador en el modal (select "Periodo
        // Escolar", poblado con PeriodoEscolarDao.getActivos()), ya no se resuelve solo con
        // "el periodo vigente hoy": permite crear un grupo para un periodo que ya arranco o
        // que todavia no empieza. Se valida que el ID elegido corresponda a un periodo
        // activo real (no uno inventado/manipulado en el POST).
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

    // Carga masiva de alumnos desde un archivo Excel (.xlsx/.xls), TODOS para el MISMO
    // Grupo (elegido en el modal "Carga Masiva" antes de subir el archivo, no en el Excel:
    // es el <select name="idGrupo"> de gestion-grupos.jsp — mejora de UX para no tener que
    // escribir/adivinar un ID_GRUPO dentro de la hoja). Formato esperado por hoja (fila 1 =
    // encabezados, los datos empiezan en la fila 2), 7 columnas — orden de tabla plana,
    // distinto al orden visual del formulario individual:
    //   A: Matricula (ver regla especial abajo segun el Cuatrimestre del Grupo)
    //   B: Nombres (requerido)           C: Apellido paterno (requerido)
    //   D: Apellido materno (requerido)  E: Correo (opcional, se autogenera si viene vacio)
    //   F: Genero: "Masculino", "Femenino" u "Otro" (requerido)
    //   G: Telefono, 10 digitos (requerido)
    //
    // REGLA DE MATRICULA (Columna A): autogenerarla a ciegas rompe el historial academico
    // de un alumno de REINGRESO (ya tiene una matricula real de su generacion original).
    //   - Grupo de 1er cuatrimestre (alumno de nuevo ingreso): si la celda viene vacia, se
    //     autogenera con GeneradorMatricula; si trae texto, se usa tal cual.
    //   - Grupo de 2do cuatrimestre en adelante (alumno YA existente en otra generacion):
    //     la matricula es OBLIGATORIA. Si la celda viene vacia, NO se autogenera nada; la
    //     fila se descarta como invalida (suma a filasConError), en vez de arriesgarse a
    //     crearle una matricula nueva a alguien que ya tenia una.
    //
    // Cada fila se valida por separado (formato + duplicados) antes de intentar guardarla:
    // una fila invalida se descarta y se cuenta como error, no cancela el resto del archivo.
    // El guardado en si (AlumnoDAO#crearMasivo) es un solo batch/commit por ALUMNO y otro
    // por ALUMNO_GRUPO_HISTORICO, en la misma transaccion.
    private void procesarCargaMasivaAlumnos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        // Con @MultipartConfig(maxFileSize=...), el contenedor no rechaza la subida al
        // vuelo: el archivo se recibe completo y recien se valida el limite al parsear el
        // multipart, en el PRIMER acceso a un parametro/Part de este request (aqui,
        // getParameter("idGrupo") mas abajo) — por eso todo el metodo, no solo
        // request.getPart(), esta dentro de este try, con IllegalStateException cachado por
        // separado del catch generico (que es para errores de POI al leer el Excel).
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

            // Mapa NOMBRE (en minusculas) -> ID_GENERO, para resolver la columna F sin
            // golpear la BD fila por fila.
            Map<String, Integer> idsPorGenero = new HashMap<>();
            for (Genero g : alumnoDAO.getAllGeneros()) {
                if (g.getNombre() != null) {
                    idsPorGenero.put(g.getNombre().trim().toLowerCase(Locale.ROOT), g.getId());
                }
            }

            // Mismo prefijo (Anio+Periodo+Sigla) para TODAS las filas del archivo, porque
            // todas van al mismo Grupo; el contador de 3 digitos si avanza fila por fila
            // (siguienteContador++) para no repetir matricula dentro del mismo lote. Solo se
            // usa si el Grupo es de 1er cuatrimestre (ver esPrimerCuatrimestre abajo); para
            // reingresos (cuatrimestre >= 2) la matricula SIEMPRE debe venir del Excel.
            String prefijoMatricula = GeneradorMatricula.construirPrefijo(grupo.getNombreCarrera(), periodoDelGrupo.getFechaInicio());
            int siguienteContador = alumnoDAO.obtenerSiguienteContador(prefijoMatricula.toUpperCase(Locale.ROOT));
            boolean esPrimerCuatrimestre = grupo.getCuatrimestre() == 1;

            List<Alumno> alumnosValidos = new ArrayList<>();
            // Numero de fila REAL del Excel (no el indice 0-based de POI) de cada fila
            // descartada, para que el coordinador sepa exactamente cuales corregir en vez
            // de solo un conteo ("se omitieron 10 filas" sin decir cuales de las 50).
            List<Integer> filasInvalidas = new ArrayList<>();

            // Detectan duplicados DENTRO del mismo archivo (la validacion contra la BD, via
            // alumnoDAO.existeX, solo detecta duplicados contra registros ya guardados).
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
                            // Reingreso (cuatrimestre >= 2): la matricula es obligatoria. NO
                            // se autogenera nada; alumno.setMatricula(null) hace que
                            // esFilaAlumnoValida() descarte la fila (cuenta en filasConError)
                            // en vez de arriesgarse a duplicar/perder el historial academico
                            // del alumno con una matricula inventada.
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

                    // Columna G: Telefono real del archivo (10 digitos), ya no se genera
                    // ningun placeholder. Se normaliza quitando espacios/guiones antes de
                    // validar el formato en esFilaAlumnoValida (REGEX_TELEFONO).
                    String telefono = obtenerTexto(fila.getCell(6));
                    alumno.setTelefono(telefono != null ? telefono.replaceAll("[^0-9]", "") : null);

                    alumno.setIdGrupo(idGrupo);

                    if (esFilaAlumnoValida(alumno, matriculasEnLote, correosEnLote, telefonosEnLote)) {
                        alumnosValidos.add(alumno);
                    } else {
                        // f es el indice 0-based de POI (fila 0 = encabezados de Excel, fila
                        // 1 = primera fila de datos = renglon 2 del Excel); f+1 lo convierte
                        // al numero de renglon tal cual lo ve el coordinador en la hoja.
                        filasInvalidas.add(f + 1);
                    }
                }
            }

            // Se guarda en SESSION (no en la URL) para no saturarla con una lista larga de
            // numeros de fila; gestion-grupos.jsp la lee y la limpia en la siguiente carga
            // de pagina (ver el bloque JSTL debajo del banner).
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

    // Blindaje de servidor: valida formato de cada campo y descarta duplicados, tanto
    // contra la BD (alumnoDAO.existeX) como dentro del propio archivo (matriculasEnLote/
    // correosEnLote/telefonosEnLote), antes de dejar pasar la fila al batch de insercion.
    private boolean esFilaAlumnoValida(Alumno alumno, Set<String> matriculasEnLote, Set<String> correosEnLote, Set<String> telefonosEnLote) {
        if (esTextoVacio(alumno.getNombres()) || !alumno.getNombres().matches(REGEX_NOMBRE) || alumno.getNombres().length() > MAX_NOMBRES) return false;
        if (esTextoVacio(alumno.getApellidoPaterno()) || !alumno.getApellidoPaterno().matches(REGEX_NOMBRE) || alumno.getApellidoPaterno().length() > MAX_APELLIDO) return false;
        // Apellido Materno (columna D) es obligatorio en la carga masiva (a diferencia del
        // alta individual, donde formulario-alumno.jsp si lo deja opcional): una fila con
        // esa celda vacia se descarta igual que si le faltara el Apellido Paterno.
        if (esTextoVacio(alumno.getApellidoMaterno()) || !alumno.getApellidoMaterno().matches(REGEX_NOMBRE) || alumno.getApellidoMaterno().length() > MAX_APELLIDO) return false;
        if (alumno.getMatricula() == null || !alumno.getMatricula().matches(REGEX_MATRICULA)) return false;
        if (alumno.getCorreoInstitucional() == null || !alumno.getCorreoInstitucional().matches(REGEX_CORREO) || alumno.getCorreoInstitucional().length() > MAX_CORREO) return false;
        if (alumno.getTelefono() == null || !alumno.getTelefono().matches(REGEX_TELEFONO)) return false;
        if (alumno.getIdGenero() == null) return false;

        if (!matriculasEnLote.add(alumno.getMatricula())) return false;
        if (!correosEnLote.add(alumno.getCorreoInstitucional().toLowerCase(Locale.ROOT))) return false;
        if (!telefonosEnLote.add(alumno.getTelefono())) return false;

        if (alumnoDAO.existeMatricula(alumno.getMatricula())) return false;
        if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional())) return false;
        return !alumnoDAO.existeTelefono(alumno.getTelefono());
    }

    private static boolean esTextoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Lee cualquier celda de POI como texto, sin importar el tipo real (Excel suele guardar
    // una "Matricula" alfanumerica como STRING, pero si el usuario la escribio o pego como
    // numero, POI la ve como NUMERIC y hay que formatearla sin notacion cientifica ni ".0").
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

    // TSU: cuatrimestres 1-6. Ingenieria (ING): cuatrimestres 7-10.
    private boolean esCuatrimestreValidoParaNivel(int cuatrimestre, String nivel) {
        if ("TSU".equals(nivel)) {
            return cuatrimestre >= 1 && cuatrimestre <= 6;
        }
        if ("ING".equals(nivel)) {
            return cuatrimestre >= 7 && cuatrimestre <= 10;
        }
        return false;
    }

    private String trimOrNull(String valor) {
        return valor != null ? valor.trim() : null;
    }

    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Centraliza lo que antes calculaba el scriptlet de arriba en formulario-alumno.jsp
    // (alumnoFormulario, esEdicion, tituloBanner, mensajeError): esa logica no le
    // corresponde a la vista, vive aqui junto con el resto de las reglas del formulario.
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
        // "Asignar a Grupo" solo lista grupos que ya existen (se crean aparte desde
        // gestion-grupos.jsp); ya no se arman aqui con Academia/Carrera/Cuatrimestre.
        request.setAttribute("listaGrupos", grupoDao.getAll());

        request.getRequestDispatcher("/coordinador/formulario-alumno.jsp").forward(request, response);
    }

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
