package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// NOMINA ya no es una columna separada: NUMERO_EMPLEADO es el unico identificador del
// tutor (PK real de TUTOR), asi que cumple el mismo papel que antes tenia "nomina".
// @MultipartConfig es necesario para poder leer request.getPart("archivoExcel") en
// accion=cargaMasivaTutores (mismo patron que AlumnoServlet).
@WebServlet(name = "TutoresServlet", value = "/gestion-tutores")
@MultipartConfig(maxFileSize = TutoresServlet.MAX_TAMANO_ARCHIVO_EXCEL)
public class TutoresServlet extends HttpServlet {

    private static final String REGEX_NOMINA = "^[0-9]{4}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    // Nomina minima permitida: la asignacion automatica (TutorDao#obtenerSiguienteNomina)
    // siempre inicia en 1000, asi que cualquier valor menor se considera invalido.
    private static final int NOMINA_MINIMA = 1000;
    private static final int MAX_NOMBRES = 100;
    private static final int MAX_APELLIDO = 50;

    // Limite del Excel de carga masiva (tambien usado en @MultipartConfig, arriba): mismo
    // criterio/valor que AlumnoServlet#MAX_TAMANO_ARCHIVO_EXCEL.
    static final long MAX_TAMANO_ARCHIVO_EXCEL = 5L * 1024 * 1024;

    // Formato estricto de cada bloque de la columna "Horarios" del Excel: "Dia:HH:mm-HH:mm",
    // varios bloques separados por coma. Ver parsearHorarios().
    private static final Pattern PATRON_HORARIO = Pattern.compile(
            "^(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes):([01]\\d|2[0-3]):([0-5]\\d)-([01]\\d|2[0-3]):([0-5]\\d)$",
            Pattern.CASE_INSENSITIVE);

    private final TutorDao tutorDAO = new TutorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        // 1. ELIMINAR TUTOR (vía GET)
        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }

        // 1b. REACTIVAR TUTOR (vía GET)
        if ("reactivar".equals(accion)) {
            procesarReactivacion(request, response);
            return;
        }

        // 2. NUEVO O PREPARAR EDICIÓN DE TUTOR
        if ("nuevo".equals(accion) || "prepararEdicion".equals(accion)) {
            Tutor tutorEdit = null;
            if ("prepararEdicion".equals(accion)) {
                String nominaStr = request.getParameter("nomina");
                if (nominaStr != null && !nominaStr.trim().isEmpty()) {
                    tutorEdit = tutorDAO.getById(Integer.parseInt(nominaStr.trim()));
                }
                forwardAFormulario(request, response, tutorEdit, null, null);
            } else {
                // Tutor nuevo: la nomina ya no la captura el coordinador, se sugiere/asigna
                // automaticamente a partir de 1000 (ver TutorDao#obtenerSiguienteNomina).
                Tutor tutorNuevo = new Tutor();
                tutorNuevo.setNumeroEmpleado(tutorDAO.obtenerSiguienteNomina());
                forwardAFormulario(request, response, null, tutorNuevo, null);
            }
            return;
        }

        // 3. CONSULTA Y LISTADO GENERAL
        List<Tutor> listaTutores = tutorDAO.getAllConGrupo();
        List<Academia> listaAcademias = tutorDAO.getAllAcademias();

        Map<Integer, String> nombresAcademia = new HashMap<>();
        if (listaAcademias != null) {
            for (Academia ac : listaAcademias) {
                nombresAcademia.put(ac.getIdAcademia(), ac.getNombre());
            }
        }

        request.setAttribute("listaTutores", listaTutores);
        request.setAttribute("listaAcademias", listaAcademias);
        request.setAttribute("nombresAcademia", nombresAcademia);

        request.getRequestDispatcher("/coordinador/gestion-tutores.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        // 1. ELIMINAR TUTOR (vía POST)
        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }

        // 1b. REACTIVAR TUTOR (vía POST)
        if ("reactivar".equals(accion)) {
            procesarReactivacion(request, response);
            return;
        }

        // 1c. CARGA MASIVA DE TUTORES (Excel)
        if ("cargaMasivaTutores".equals(accion)) {
            procesarCargaMasivaTutores(request, response);
            return;
        }

        // 2. CREAR / ACTUALIZAR TUTOR
        Tutor tutor = new Tutor();
        String nominaStr = request.getParameter("nomina");

        // Blindaje de servidor: el <input> de nomina es maxlength/minlength/pattern="^[0-9]{4}$"
        // y queda readonly en edicion, pero eso es solo UX. Como con la matricula en
        // AlumnoServlet, se revalida aqui por si alguien manipula el HTML (inspeccionar/DevTools)
        // y manda una nomina con letras o de largo distinto a 4.
        boolean nominaValida = nominaStr != null && nominaStr.trim().matches(REGEX_NOMINA);
        if (nominaValida) {
            int nominaParseada = Integer.parseInt(nominaStr.trim());
            // Las nominas se asignan automaticamente a partir de 1000 (ver
            // TutorDao#obtenerSiguienteNomina), asi que cualquier valor menor es invalido.
            nominaValida = nominaParseada >= NOMINA_MINIMA;
            if (nominaValida) {
                tutor.setNumeroEmpleado(nominaParseada);
            }
        }

        tutor.setNombres(request.getParameter("nombres"));
        tutor.setApellidoPaterno(request.getParameter("apellidoPaterno"));
        tutor.setApellidoMaterno(request.getParameter("apellidoMaterno"));
        tutor.setCorreoInstitucional(request.getParameter("correo"));
        tutor.setTelefono(request.getParameter("telefono"));

        // Capturar la lista de horarios enviados desde el formulario
        String[] horarios = request.getParameterValues("horariosDispo");
        if (horarios != null) {
            tutor.setHorariosDispo(Arrays.asList(horarios));
        }
        String idAcademiaStr = request.getParameter("idAcademia");
        if (idAcademiaStr != null && !idAcademiaStr.trim().isEmpty()) {
            tutor.setIdAcademia(Integer.parseInt(idAcademiaStr.trim()));
        } else if ("editar".equals(accion) && nominaValida) {
            // NUEVO: Si la academia viene vacía (ej. porque se deshabilitó en el frontend),
            // recuperamos el ID que ya tenía registrado en la base de datos para no reemplazarlo con un 0 (null).
            Tutor tutorAntiguo = tutorDAO.getById(tutor.getNumeroEmpleado());
            if (tutorAntiguo != null) {
                tutor.setIdAcademia(tutorAntiguo.getIdAcademia());
            }
        }

        boolean esEdicion = "editar".equals(accion);

        if (!nominaValida) {
            forwardAFormulario(request, response, null, tutor, "formato_invalido");
            return;
        }

        // Blindaje de servidor: el <input> de correo valida el formato con "pattern" en el
        // HTML, pero eso es solo UX. Se revalida aqui por si el formulario se manipula o se
        // envia sin pasar por la validacion del navegador.
        String correo = tutor.getCorreoInstitucional();
        boolean correoValido = correo != null && correo.trim().matches(REGEX_CORREO);
        if (!correoValido) {
            forwardAFormulario(request, response, null, tutor, "correo_invalido");
            return;
        }

        // Blindaje de servidor: al menos un horario de atencion es obligatorio, ya que
        // el boton de Guardar solo lo exige en el cliente via JS.
        if (tutor.getHorariosDispo() == null || tutor.getHorariosDispo().isEmpty()) {
            forwardAFormulario(request, response, null, tutor, "horario_requerido");
            return;
        }

        String errorDuplicado = null;
        if (esEdicion) {
            if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional(), tutor.getNumeroEmpleado())) {
                errorDuplicado = "correo_duplicado";
            } else if (tutorDAO.existeTelefono(tutor.getTelefono(), tutor.getNumeroEmpleado())) {
                errorDuplicado = "telefono_duplicado";
            }
        } else {
            if (tutorDAO.existeNomina(tutor.getNumeroEmpleado())) {
                errorDuplicado = "nomina_duplicada";
            } else if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional())) {
                errorDuplicado = "correo_duplicado";
            } else if (tutorDAO.existeTelefono(tutor.getTelefono())) {
                errorDuplicado = "telefono_duplicado";
            }
        }

        if (errorDuplicado != null) {
            forwardAFormulario(request, response, null, tutor, errorDuplicado);
            return;
        }

        boolean operacionExitosa = esEdicion ? tutorDAO.update(tutor) : tutorDAO.create(tutor);

        if (operacionExitosa) {
            String exito = esEdicion ? "actualizado" : "guardado";
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?exito=" + exito);
        } else {
            forwardAFormulario(request, response, null, tutor, "registro_fallido");
        }
    }

    // Centraliza lo que antes calculaba el scriptlet de arriba en formulario-tutor.jsp
    // (tutorFormulario, esEdicion, tituloBanner, mensajeError): esa logica no le
    // corresponde a la vista, vive aqui junto con el resto de las reglas del formulario.
    private void forwardAFormulario(HttpServletRequest request, HttpServletResponse response,
                                    Tutor tutorEdit, Tutor tutorConError, String codigoError)
            throws ServletException, IOException {
        Tutor tutorFormulario = tutorEdit != null ? tutorEdit : tutorConError;
        String accionParam = request.getParameter("accion");
        boolean esEdicion = tutorEdit != null || "editar".equals(accionParam) || "prepararEdicion".equals(accionParam);

        request.setAttribute("tutorFormulario", tutorFormulario);
        request.setAttribute("esEdicion", esEdicion);
        request.setAttribute("tituloBanner", esEdicion ? "Editar Tutor" : "Nuevo Tutor");
        request.setAttribute("mensajeError", resolverMensajeError(codigoError));
        request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());

        request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
    }

    private String resolverMensajeError(String codigoError) {
        if ("nomina_duplicada".equals(codigoError)) {
            return "Esta nómina ya está registrada en el sistema.";
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
        if ("correo_invalido".equals(codigoError)) {
            return "El correo debe ser un correo institucional válido terminado en @utez.edu.mx.";
        }
        if ("horario_requerido".equals(codigoError)) {
            return "Debes agregar al menos un horario de atención antes de guardar.";
        }
        if ("registro_fallido".equals(codigoError)) {
            return "No se pudo guardar el tutor. Intenta de nuevo.";
        }
        return null;
    }

    // Carga masiva de tutores desde un archivo Excel (.xlsx/.xls). Formato esperado por hoja
    // (fila 1 = encabezados, los datos empiezan en la fila 2), 7 columnas:
    //   A: Nombres (requerido)             B: Apellido paterno (requerido)
    //   C: Apellido materno (opcional)     D: Correo institucional (OPCIONAL, ver abajo)
    //   E: Telefono, 10 digitos (requerido)
    //   F: Nombre de la Academia (requerido, se traduce a ID_ACADEMIA contra BD)
    //   G: Horarios (opcional, "Dia:HH:mm-HH:mm" separados por coma; ver parsearHorarios)
    //
    // NOMINA (NUMERO_EMPLEADO) nunca se pide en el Excel: se asigna automaticamente aqui,
    // de forma secuencial a partir de TutorDao#obtenerSiguienteNomina, igual que
    // AlumnoServlet asigna la Matricula autogenerada fila por fila.
    //
    // CORREO (columna D): si viene vacio, se autogenera con primerNombre+apellidoPaterno
    // (sin acentos/espacios, en minusculas) + "@utez.edu.mx" (ver limpiarParaCorreo()). A
    // proposito NO se le pega un contador para desempatar homonimos: si dos filas generan el
    // mismo correo (o el Excel ya trae uno repetido a mano), la SEGUNDA simplemente se
    // rechaza en validarFilaTutor() (mismo "patron alumno" que matriculasEnLote en
    // AlumnoServlet: Set correosEnLote + tutorDAO.existeCorreo contra BD). El coordinador
    // corrige esa fila a mano en vez de terminar con un correo "adivinado" (ej. con un
    // numero pegado) que nadie escribio.
    //
    // Cada fila se valida por separado antes de intentar guardarla: una fila invalida se
    // descarta y se cuenta como error, no cancela el resto del archivo. El guardado en si
    // (TutorDao#crearMasivo) es un solo batch/commit por TUTOR y otro por HORARIO_ATENCION,
    // en la misma transaccion. Mismo patron que AlumnoServlet#procesarCargaMasivaAlumnos.
    private void procesarCargaMasivaTutores(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        // Con @MultipartConfig(maxFileSize=...), el contenedor no rechaza la subida al vuelo:
        // el limite recien se valida al parsear el multipart, en el PRIMER acceso a un
        // parametro/Part de este request — por eso todo el metodo esta dentro de este try,
        // con IllegalStateException cachado por separado del catch generico (errores de POI).
        try {
            Part parteArchivo = request.getPart("archivoExcel");
            if (parteArchivo == null || parteArchivo.getSize() == 0) {
                response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_vacio");
                return;
            }

            // Mapa NOMBRE (en minusculas) -> ID_ACADEMIA, para resolver la columna F sin
            // golpear la BD fila por fila.
            Map<String, Integer> idsPorAcademia = new HashMap<>();
            for (Academia a : tutorDAO.getAllAcademias()) {
                if (a.getNombre() != null) {
                    idsPorAcademia.put(a.getNombre().trim().toLowerCase(Locale.ROOT), a.getIdAcademia());
                }
            }

            int siguienteNomina = tutorDAO.obtenerSiguienteNomina();

            List<Tutor> tutoresValidos = new ArrayList<>();
            // Un mensaje por cada fila descartada ("Fila 6: la academia 'Contabilidad' no
            // existe en el catálogo."), no solo el numero de renglon: asi el coordinador
            // corrige de una sola pasada en vez de adivinar o volver a intentar a ciegas.
            List<String> filasInvalidas = new ArrayList<>();

            // Detectan duplicados DENTRO del mismo archivo (la validacion contra la BD, via
            // tutorDAO.existeX, solo detecta duplicados contra registros ya guardados).
            Set<String> correosEnLote = new HashSet<>();
            Set<String> telefonosEnLote = new HashSet<>();

            try (InputStream entrada = parteArchivo.getInputStream();
                 Workbook libro = WorkbookFactory.create(entrada)) {

                Sheet hoja = libro.getSheetAt(0);

                for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                    Row fila = hoja.getRow(f);
                    if (fila == null || esTextoVacio(obtenerTexto(fila.getCell(0)))) continue;

                    Tutor tutor = new Tutor();
                    // Nomina autogenerada, secuencial dentro del lote: cada fila procesada
                    // consume un numero, se use o no (si la fila resulta invalida), igual
                    // que el contador de Matricula en AlumnoServlet.
                    tutor.setNumeroEmpleado(siguienteNomina);
                    siguienteNomina++;

                    tutor.setNombres(obtenerTexto(fila.getCell(0)));
                    tutor.setApellidoPaterno(obtenerTexto(fila.getCell(1)));
                    tutor.setApellidoMaterno(obtenerTexto(fila.getCell(2)));

                    // Columna D: Correo opcional. Vacio -> se autogenera con el primer
                    // nombre + apellido paterno (ver limpiarParaCorreo). Con texto -> se usa
                    // tal cual, y su formato (@utez.edu.mx) se revisa mas abajo en
                    // validarFilaTutor(), igual que si hubiera venido escrito a mano.
                    String correo = obtenerTexto(fila.getCell(3));
                    if (esTextoVacio(correo)) {
                        String primerNombre = tutor.getNombres() != null
                                ? tutor.getNombres().trim().split("\\s+")[0] : "";
                        correo = limpiarParaCorreo(primerNombre + tutor.getApellidoPaterno()) + "@utez.edu.mx";
                    }
                    tutor.setCorreoInstitucional(correo);

                    String telefono = obtenerTexto(fila.getCell(4));
                    tutor.setTelefono(telefono != null ? telefono.replaceAll("[^0-9]", "") : null);

                    // Columna F: traduccion de Academia (texto) a ID_ACADEMIA. Si el nombre
                    // no existe en el catalogo, la fila es invalida y se descarta.
                    String academiaTexto = obtenerTexto(fila.getCell(5));
                    Integer idAcademia = academiaTexto != null ? idsPorAcademia.get(academiaTexto.trim().toLowerCase(Locale.ROOT)) : null;
                    String motivo = null;
                    if (idAcademia == null) {
                        motivo = esTextoVacio(academiaTexto)
                                ? "falta la Academia."
                                : "la academia '" + academiaTexto + "' no existe en el catálogo.";
                    } else {
                        tutor.setIdAcademia(idAcademia);
                    }

                    // Columna G: horarios con validacion estricta (cero tolerancia). Celda
                    // vacia = sin horarios (valido); celda con texto mal formado = fila
                    // invalida completa (parsearHorarios regresa null).
                    if (motivo == null) {
                        List<String> horarios = parsearHorarios(obtenerTexto(fila.getCell(6)));
                        if (horarios == null) {
                            motivo = "el horario '" + obtenerTexto(fila.getCell(6))
                                    + "' no tiene el formato Día:HH:mm-HH:mm.";
                        } else {
                            tutor.setHorariosDispo(horarios);
                        }
                    }

                    if (motivo == null) {
                        motivo = validarFilaTutor(tutor, correosEnLote, telefonosEnLote);
                    }

                    if (motivo == null) {
                        tutoresValidos.add(tutor);
                    } else {
                        // f es el indice 0-based de POI (fila 0 = encabezados de Excel, fila
                        // 1 = primera fila de datos = renglon 2 del Excel); f+1 lo convierte
                        // al numero de renglon tal cual lo ve el coordinador en la hoja.
                        filasInvalidas.add("Fila " + (f + 1) + ": " + motivo);
                    }
                }
            }

            // Se guarda en SESSION (no en la URL) para no saturarla con una lista larga de
            // numeros de fila; gestion-tutores.jsp la lee y la limpia en la siguiente carga
            // de pagina (mismo patron que filasInvalidasExcel en gestion-grupos.jsp).
            if (!filasInvalidas.isEmpty()) {
                request.getSession().setAttribute("filasInvalidasTutoresExcel", filasInvalidas);
            }

            if (tutoresValidos.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_invalido");
                return;
            }

            int insertados = tutorDAO.crearMasivo(tutoresValidos);
            String parametro = insertados > 0
                    ? "exito=carga_masiva_tutores&insertados=" + insertados + "&conError=" + filasInvalidas.size()
                    : "error=carga_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?" + parametro);

        } catch (IllegalStateException e) {
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_muy_grande");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_invalido");
        }
    }

    // Validacion ESTRICTA (cero tolerancia) de la columna "Horarios": cada bloque debe
    // calzar EXACTO con "Dia:HH:mm-HH:mm" (dias sin/con acento, ambos aceptados), varios
    // bloques separados por coma. Si la celda viene vacia, no es un error: el tutor
    // simplemente no trae horarios (la columna es opcional). Pero si trae texto y UN SOLO
    // bloque falla el formato (falla el split o el parseo de la hora), se regresa null para
    // que el llamador descarte la fila COMPLETA — no queremos tutores con horarios a medias
    // por un error tipografico silencioso.
    private List<String> parsearHorarios(String texto) {
        if (esTextoVacio(texto)) return new ArrayList<>();

        List<String> horarios = new ArrayList<>();
        for (String bloqueCrudo : texto.split(",")) {
            String bloque = bloqueCrudo.trim();
            if (bloque.isEmpty()) continue;

            Matcher m = PATRON_HORARIO.matcher(bloque);
            if (!m.matches()) return null;

            String desde = m.group(2) + ":" + m.group(3);
            String hasta = m.group(4) + ":" + m.group(5);
            if (hasta.compareTo(desde) <= 0) return null; // rango invertido o vacio

            horarios.add(bloque);
        }

        // Una celda con solo comas/espacios ("," o " , ") no es texto vacio pero tampoco
        // produjo ningun horario real: se trata igual que un formato invalido.
        return horarios.isEmpty() ? null : horarios;
    }

    // Blindaje de servidor: valida formato de cada campo y descarta duplicados, tanto contra
    // la BD (tutorDAO.existeX) como dentro del propio archivo (correosEnLote/telefonosEnLote),
    // antes de dejar pasar la fila al batch de insercion. Academia y Horarios ya se validan
    // aparte en procesarCargaMasivaTutores (tienen su propio motivo de invalidez).
    // Regresa null si la fila es valida, o el motivo exacto (para filasInvalidas) si no.
    private String validarFilaTutor(Tutor tutor, Set<String> correosEnLote, Set<String> telefonosEnLote) {
        if (esTextoVacio(tutor.getNombres()) || !tutor.getNombres().matches(REGEX_NOMBRE) || tutor.getNombres().length() > MAX_NOMBRES) {
            return "el nombre '" + tutor.getNombres() + "' es inválido o está vacío.";
        }
        if (esTextoVacio(tutor.getApellidoPaterno()) || !tutor.getApellidoPaterno().matches(REGEX_NOMBRE) || tutor.getApellidoPaterno().length() > MAX_APELLIDO) {
            return "el apellido paterno '" + tutor.getApellidoPaterno() + "' es inválido o está vacío.";
        }
        // Apellido Materno es opcional (igual que en el alta individual): solo se valida
        // formato/largo si vino con algo.
        if (!esTextoVacio(tutor.getApellidoMaterno())
                && (!tutor.getApellidoMaterno().matches(REGEX_NOMBRE) || tutor.getApellidoMaterno().length() > MAX_APELLIDO)) {
            return "el apellido materno '" + tutor.getApellidoMaterno() + "' es inválido.";
        }
        if (tutor.getCorreoInstitucional() == null || !tutor.getCorreoInstitucional().matches(REGEX_CORREO)) {
            return "el correo '" + tutor.getCorreoInstitucional() + "' no es válido (debe terminar en @utez.edu.mx).";
        }
        if (tutor.getTelefono() == null || !tutor.getTelefono().matches(REGEX_TELEFONO)) {
            return "el teléfono '" + tutor.getTelefono() + "' debe tener exactamente 10 dígitos.";
        }

        if (!correosEnLote.add(tutor.getCorreoInstitucional().toLowerCase(Locale.ROOT))) {
            return "el correo '" + tutor.getCorreoInstitucional() + "' está repetido dentro del mismo archivo.";
        }
        if (!telefonosEnLote.add(tutor.getTelefono())) {
            return "el teléfono '" + tutor.getTelefono() + "' está repetido dentro del mismo archivo.";
        }

        if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional())) {
            return "el correo '" + tutor.getCorreoInstitucional() + "' ya está registrado en el sistema.";
        }
        if (tutorDAO.existeTelefono(tutor.getTelefono())) {
            return "el teléfono '" + tutor.getTelefono() + "' ya está registrado en el sistema.";
        }
        return null;
    }

    private static boolean esTextoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Deja solo letras/digitos en minuscula, sin acentos ni espacios: usado para armar la
    // parte local del correo autogenerado (columna D vacia) a partir de
    // primerNombre+apellidoPaterno. Ej. "José Pérez" -> "joseperez".
    private static String limpiarParaCorreo(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

    // Lee cualquier celda de POI como texto, sin importar el tipo real (ej. un Telefono
    // capturado/pegado como numero, que POI ve como NUMERIC en vez de STRING).
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

    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parametro = "error=tutor_no_encontrado";
        String nominaStr = request.getParameter("nomina");
        if (nominaStr != null && !nominaStr.trim().isEmpty()) {
            try {
                int numeroEmpleado = Integer.parseInt(nominaStr.trim());

                Tutor tutor = tutorDAO.getById(numeroEmpleado);
                if (tutor != null) {
                    // Blindaje: no se permite eliminar (dar de baja) a un tutor si todavia
                    // tiene grupos asignados, solicitudes sin atender o sesiones pendientes.
                    if (tutorDAO.tienePendientes(tutor.getNumeroEmpleado())) {
                        response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=tutor_con_pendientes");
                        return;
                    }

                    boolean eliminado = tutorDAO.delete(numeroEmpleado);
                    parametro = eliminado ? "exito=eliminado" : "error=tutor_en_uso";
                }
            } catch (Exception e) {
                e.printStackTrace();
                parametro = "error=tutor_en_uso";
            }
        }
        response.sendRedirect(request.getContextPath() + "/gestion-tutores?" + parametro);
    }

    private void procesarReactivacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parametro = "error=tutor_no_encontrado";
        String nominaStr = request.getParameter("nomina");
        if (nominaStr != null && !nominaStr.trim().isEmpty()) {
            try {
                int numeroEmpleado = Integer.parseInt(nominaStr.trim());
                boolean reactivado = tutorDAO.reactivar(numeroEmpleado);
                parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            } catch (Exception e) {
                e.printStackTrace();
                parametro = "error=reactivacion_fallida";
            }
        }
        response.sendRedirect(request.getContextPath() + "/gestion-tutores?" + parametro);
    }
}
