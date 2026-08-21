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
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 15
)
public class TutoresServlet extends HttpServlet {

    private static final String REGEX_NOMINA = "^[0-9]{4}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";

    // Deben coincidir con VARCHAR2(N) de TUTOR en la BD (ver utils/02_INSERTS...sql)
    // para nunca dejar pasar al DAO un valor que provoque DataTruncation/SQLException.
    private static final int MAX_NOMBRES = 100;
    private static final int MAX_APELLIDO = 50;
    private static final int MAX_CORREO = 100;
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
            // "0000" cumple el regex de 4 digitos pero no es una nomina real (parsea a 0).
            nominaValida = nominaParseada >= NOMINA_MINIMA;
            if (nominaValida) {
                tutor.setNumeroEmpleado(nominaParseada);
            }
        }

        tutor.setNombres(trimOrNull(request.getParameter("nombres")));
        tutor.setApellidoPaterno(trimOrNull(request.getParameter("apellidoPaterno")));
        tutor.setApellidoMaterno(trimOrNull(request.getParameter("apellidoMaterno")));
        tutor.setCorreoInstitucional(trimOrNull(request.getParameter("correo")));
        tutor.setTelefono(trimOrNull(request.getParameter("telefono")));

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

        // Blindaje de servidor: nombres/apellidos/telefono solo se validaban con "pattern"
        // en el HTML (igual que nomina/correo antes de blindarse). Se revalida formato Y
        // longitud aqui para no depender del navegador y no dejar pasar un valor mas largo
        // de lo que soporta la columna VARCHAR2 correspondiente en TUTOR.
        boolean datosPersonalesValidos = tutor.getNombres() != null && tutor.getNombres().matches(REGEX_NOMBRE)
                && tutor.getNombres().length() <= MAX_NOMBRES
                && tutor.getApellidoPaterno() != null && tutor.getApellidoPaterno().matches(REGEX_NOMBRE)
                && tutor.getApellidoPaterno().length() <= MAX_APELLIDO
                && (tutor.getApellidoMaterno() == null || tutor.getApellidoMaterno().isBlank()
                || (tutor.getApellidoMaterno().matches(REGEX_NOMBRE) && tutor.getApellidoMaterno().length() <= MAX_APELLIDO))
                && tutor.getTelefono() != null && tutor.getTelefono().matches(REGEX_TELEFONO);
        if (!datosPersonalesValidos) {
            forwardAFormulario(request, response, null, tutor, "formato_invalido");
            return;
        }

        // Blindaje de servidor: el <input> de correo valida el formato con "pattern" en el
        // HTML, pero eso es solo UX. Se revalida aqui por si el formulario se manipula o se
        // envia sin pasar por la validacion del navegador.
        String correo = tutor.getCorreoInstitucional();
        boolean correoValido = correo != null && correo.matches(REGEX_CORREO) && correo.length() <= MAX_CORREO;
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

    private String trimOrNull(String valor) {
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

    // Carga masiva de tutores desde un archivo Excel (.xlsx/.xls). Formato esperado por
    // hoja (fila 1 = encabezados, los datos empiezan en la fila 2):
    // A: Nomina (opcional, se autoasigna desde 1000 si viene vacia)
    // B: Nombres (requerido)          C: Apellido paterno (requerido)
    // D: Apellido materno (opcional)  E: Correo (opcional, se autogenera si viene vacio)
    // F: Telefono, 10 digitos (requerido)
    // G: ID Academia (requerido, numerico; ver catalogo de academias en gestion-tutores.jsp)
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

            // Detectan duplicados DENTRO del mismo archivo (la validacion contra la BD,
            // via tutorDAO.existeX, solo detecta duplicados contra registros ya guardados).
            Set<Integer> nominasEnLote = new HashSet<>();
            Set<String> correosEnLote = new HashSet<>();
            Set<String> telefonosEnLote = new HashSet<>();
            int siguienteNomina = tutorDAO.obtenerSiguienteNomina();

            try (InputStream entrada = parteArchivo.getInputStream();
                 Workbook libro = WorkbookFactory.create(entrada)) {

                Sheet hoja = libro.getSheetAt(0);

                for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                    Row fila = hoja.getRow(f);
                    if (fila == null || esTextoVacio(obtenerTexto(fila.getCell(1)))) continue;

                    Tutor tutor = new Tutor();

                    int numeroEmpleado = (int) obtenerNumerico(fila.getCell(0));
                    if (numeroEmpleado < NOMINA_MINIMA) {
                        numeroEmpleado = siguienteNomina++;
                    } else {
                        siguienteNomina = Math.max(siguienteNomina, numeroEmpleado + 1);
                    }
                    tutor.setNumeroEmpleado(numeroEmpleado);

                    tutor.setNombres(obtenerTexto(fila.getCell(1)));
                    tutor.setApellidoPaterno(obtenerTexto(fila.getCell(2)));
                    tutor.setApellidoMaterno(obtenerTexto(fila.getCell(3)));

                    String correo = obtenerTexto(fila.getCell(4));
                    if (esTextoVacio(correo)) {
                        correo = generarCorreoInstitucional(tutor.getNombres(), tutor.getApellidoPaterno());
                    }
                    tutor.setCorreoInstitucional(correo);

                    tutor.setTelefono(obtenerTexto(fila.getCell(5)));
                    tutor.setIdAcademia((int) obtenerNumerico(fila.getCell(6)));

                    if (esFilaValida(tutor, nominasEnLote, correosEnLote, telefonosEnLote)) {
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

    // Blindaje de servidor: valida formato de cada campo y descarta duplicados, tanto
    // contra la BD (tutorDAO.existeX) como dentro del propio archivo (nominasEnLote/
    // correosEnLote/telefonosEnLote), antes de dejar pasar la fila al batch de insercion.
    private boolean esFilaValida(Tutor tutor, Set<Integer> nominasEnLote, Set<String> correosEnLote, Set<String> telefonosEnLote) {
        if (esTextoVacio(tutor.getNombres()) || esTextoVacio(tutor.getApellidoPaterno())) return false;
        if (tutor.getTelefono() == null || !tutor.getTelefono().matches(REGEX_TELEFONO)) return false;
        if (tutor.getCorreoInstitucional() == null || !tutor.getCorreoInstitucional().matches(REGEX_CORREO)) return false;
        if (tutor.getIdAcademia() <= 0) return false;
        if (tutor.getNumeroEmpleado() < NOMINA_MINIMA) return false;

        if (!nominasEnLote.add(tutor.getNumeroEmpleado())) return false;
        if (!correosEnLote.add(tutor.getCorreoInstitucional().toLowerCase())) return false;
        if (!telefonosEnLote.add(tutor.getTelefono())) return false;

        if (tutorDAO.existeNomina(tutor.getNumeroEmpleado())) return false;
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
