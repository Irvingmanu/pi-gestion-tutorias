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
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import org.apache.poi.ss.usermodel.Cell;
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
import java.util.Map;
import java.util.Set;

// NOMINA ya no es una columna separada: NUMERO_EMPLEADO es el unico identificador del
// tutor (PK real de TUTOR), asi que cumple el mismo papel que antes tenia "nomina".
@WebServlet(name = "TutoresServlet", value = "/gestion-tutores")
// Habilita request.getPart(...) para la carga masiva de tutores via Excel.
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,       // 1 MB en memoria antes de volcar a disco
        maxFileSize = 1024 * 1024 * 10,        // 10 MB por archivo
        maxRequestSize = 1024 * 1024 * 15      // 15 MB por request
)
public class TutoresServlet extends HttpServlet {

    private static final String REGEX_NOMINA = "^[0-9]{4}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    // Nomina minima permitida: la asignacion automatica (TutorDao#obtenerSiguienteNomina)
    // siempre inicia en 1000, asi que cualquier valor menor se considera invalido.
    private static final int NOMINA_MINIMA = 1000;

    private final TutorDao tutorDAO = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDAO = new AsignacionTutorDao();

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

        // 1c. CARGA MASIVA DE TUTORES (Excel, vía POST multipart/form-data)
        if ("cargaMasiva".equals(accion)) {
            procesarCargaMasiva(request, response);
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

    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parametro = "error=tutor_no_encontrado";
        String nominaStr = request.getParameter("nomina");
        if (nominaStr != null && !nominaStr.trim().isEmpty()) {
            try {
                int numeroEmpleado = Integer.parseInt(nominaStr.trim());

                Tutor tutor = tutorDAO.getById(numeroEmpleado);
                if (tutor != null) {
                    // Blindaje: no se permite eliminar (dar de baja) a un tutor si tiene
                    // al menos una asignación activa dentro de un periodo escolar activo.
                    if (asignacionTutorDAO.existeAsignacionEnPeriodoActivo(tutor.getNumeroEmpleado())) {
                        response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=tutor_periodo_activo");
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

    // Un horario de Excel debe traer un dia de la semana y al menos 2 horas (desde/hasta),
    // igual que el extractor "inteligente" de TutorDao#insertarHorarios.
    private static final java.util.regex.Pattern PATRON_DIA_HORARIO =
            java.util.regex.Pattern.compile("(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes)", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final java.util.regex.Pattern PATRON_HORA_HORARIO =
            java.util.regex.Pattern.compile("([0-2][0-9]:[0-5][0-9])");

    // Carga masiva de tutores desde un archivo Excel (.xlsx/.xls). Formato esperado por
    // hoja (fila 1 = encabezados, los datos empiezan en la fila 2):
    // A: Nombres (requerido)              B: Apellido paterno (requerido)
    // C: Apellido materno (requerido)     D: Telefono, 10 digitos (requerido)
    // E: ID Academia (requerido, numerico; ver catalogo de academias en gestion-tutores.jsp)
    // F: Horarios de atencion (requerido, al menos uno). Formato por horario "Dia: HH:MM -
    //    HH:MM" (igual que el que arma el formulario individual, ver tutor.js#agregarHorario);
    //    varios horarios en la misma celda se separan con ";" o con un salto de linea.
    //
    // Nomina y Correo YA NO son columnas del Excel: la nomina se autoasigna a partir de
    // 1000 (TutorDao#obtenerSiguienteNomina) y el correo se autogenera desde nombre +
    // apellido paterno (generarCorreoInstitucional), igual que en el alta individual.
    //
    // Cada fila se valida por separado (formato + duplicados) antes de intentar guardarla:
    // una fila invalida se descarta y se cuenta como error, no cancela el resto del archivo.
    // El guardado en si (TutorDao#crearMasivo) es un solo batch/commit.
    private void procesarCargaMasiva(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Part parteArchivo = request.getPart("archivoExcel");
            if (parteArchivo == null || parteArchivo.getSize() == 0) {
                response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_vacio");
                return;
            }

            List<Tutor> tutoresValidos = new ArrayList<>();
            int filasConError = 0;

            // Detecta duplicados DENTRO del mismo archivo (la validacion contra la BD,
            // via tutorDAO.existeX, solo detecta duplicados contra registros ya guardados).
            // La nomina no necesita set propio: siempre se autoasigna con un contador
            // estrictamente creciente, asi que nunca se repite dentro del mismo archivo.
            Set<String> correosEnLote = new HashSet<>();
            Set<String> telefonosEnLote = new HashSet<>();
            int siguienteNomina = tutorDAO.obtenerSiguienteNomina();

            try (InputStream entrada = parteArchivo.getInputStream();
                 Workbook libro = WorkbookFactory.create(entrada)) {

                Sheet hoja = libro.getSheetAt(0);

                for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                    Row fila = hoja.getRow(f);
                    if (fila == null || esTextoVacio(obtenerTexto(fila.getCell(0)))) continue;

                    Tutor tutor = new Tutor();
                    tutor.setNumeroEmpleado(siguienteNomina++);

                    tutor.setNombres(obtenerTexto(fila.getCell(0)));
                    tutor.setApellidoPaterno(obtenerTexto(fila.getCell(1)));
                    tutor.setApellidoMaterno(obtenerTexto(fila.getCell(2)));
                    tutor.setCorreoInstitucional(generarCorreoInstitucional(tutor.getNombres(), tutor.getApellidoPaterno()));
                    tutor.setTelefono(obtenerTexto(fila.getCell(3)));
                    tutor.setIdAcademia((int) obtenerNumerico(fila.getCell(4)));
                    tutor.setHorariosDispo(parsearHorarios(obtenerTexto(fila.getCell(5))));

                    if (esFilaValida(tutor, correosEnLote, telefonosEnLote)) {
                        tutoresValidos.add(tutor);
                    } else {
                        filasConError++;
                    }
                }
            }

            if (tutoresValidos.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_invalido");
                return;
            }

            int insertados = tutorDAO.crearMasivo(tutoresValidos);
            String parametro = insertados > 0
                    ? "exito=carga_masiva&insertados=" + insertados + "&conError=" + filasConError
                    : "error=carga_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?" + parametro);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_invalido");
        }
    }

    // Separa la celda de horarios en entradas individuales: varios horarios en la misma
    // celda pueden venir separados por ";" o por un salto de linea (Alt+Enter en Excel).
    private List<String> parsearHorarios(String textoCelda) {
        List<String> horarios = new ArrayList<>();
        if (esTextoVacio(textoCelda)) return horarios;
        for (String token : textoCelda.split("[;\\n\\r]+")) {
            String limpio = token.trim();
            if (!limpio.isEmpty()) horarios.add(limpio);
        }
        return horarios;
    }

    // Un horario es valido si trae un dia de la semana reconocible y al menos 2 horas
    // (desde/hasta) en formato HH:MM, sin importar el resto de la puntuacion.
    private boolean esHorarioValido(String horario) {
        if (esTextoVacio(horario)) return false;
        if (!PATRON_DIA_HORARIO.matcher(horario).find()) return false;
        java.util.regex.Matcher mHoras = PATRON_HORA_HORARIO.matcher(horario);
        return mHoras.find() && mHoras.find();
    }

    // Blindaje de servidor: valida formato de cada campo y descarta duplicados, tanto
    // contra la BD (tutorDAO.existeX) como dentro del propio archivo (correosEnLote/
    // telefonosEnLote), antes de dejar pasar la fila al batch de insercion.
    private boolean esFilaValida(Tutor tutor, Set<String> correosEnLote, Set<String> telefonosEnLote) {
        if (esTextoVacio(tutor.getNombres()) || esTextoVacio(tutor.getApellidoPaterno()) || esTextoVacio(tutor.getApellidoMaterno())) return false;
        if (tutor.getTelefono() == null || !tutor.getTelefono().matches(REGEX_TELEFONO)) return false;
        if (tutor.getCorreoInstitucional() == null || !tutor.getCorreoInstitucional().matches(REGEX_CORREO)) return false;
        if (tutor.getIdAcademia() <= 0) return false;

        if (tutor.getHorariosDispo() == null || tutor.getHorariosDispo().isEmpty()) return false;
        for (String horario : tutor.getHorariosDispo()) {
            if (!esHorarioValido(horario)) return false;
        }

        if (!correosEnLote.add(tutor.getCorreoInstitucional().toLowerCase())) return false;
        if (!telefonosEnLote.add(tutor.getTelefono())) return false;

        if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional())) return false;
        return !tutorDAO.existeTelefono(tutor.getTelefono());
    }

    // Mismo criterio que el autocompletado del formulario (assets/js/coordinador/tutor.js):
    // primerNombre + primerApellidoPaterno + "@utez.edu.mx", sin acentos ni espacios.
    private String generarCorreoInstitucional(String nombres, String apellidoPaterno) {
        String primerNombre = primeraPalabraSinAcentos(nombres);
        String primerApellido = primeraPalabraSinAcentos(apellidoPaterno);
        if (primerNombre.isEmpty() || primerApellido.isEmpty()) return null;
        return primerNombre + primerApellido + "@utez.edu.mx";
    }

    private String primeraPalabraSinAcentos(String texto) {
        if (esTextoVacio(texto)) return "";
        String primera = texto.trim().split("\\s+")[0];
        String sinAcentos = Normalizer.normalize(primera, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase().replaceAll("[^a-z]", "");
    }

    private boolean esTextoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Lee una celda como texto sin importar si Excel la guardo como texto o como numero
    // (ej. la nomina o el telefono capturados sin formato de texto en la hoja).
    private String obtenerTexto(Cell celda) {
        if (celda == null) return null;
        return switch (celda.getCellType()) {
            case STRING -> celda.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) celda.getNumericCellValue());
            default -> null;
        };
    }

    private double obtenerNumerico(Cell celda) {
        if (celda == null) return 0;
        try {
            return switch (celda.getCellType()) {
                case NUMERIC -> celda.getNumericCellValue();
                case STRING -> Double.parseDouble(celda.getStringCellValue().trim());
                default -> 0;
            };
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
