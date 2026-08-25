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

@WebServlet(name = "TutoresServlet", value = "/gestion-tutores")
@MultipartConfig(maxFileSize = TutoresServlet.MAX_TAMANO_ARCHIVO_EXCEL)
public class TutoresServlet extends HttpServlet {

    private static final String REGEX_NOMINA = "^[0-9]{4}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";

    private static final int NOMINA_MINIMA = 1000;
    private static final int MAX_NOMBRES = 100;
    private static final int MAX_APELLIDO = 50;

    static final long MAX_TAMANO_ARCHIVO_EXCEL = 5L * 1024 * 1024;

    private static final Pattern PATRON_HORARIO = Pattern.compile(
            "^(Lunes|Martes|Mi[eé]rcoles|Jueves|Viernes):([01]\\d|2[0-3]):([0-5]\\d)-([01]\\d|2[0-3]):([0-5]\\d)$",
            Pattern.CASE_INSENSITIVE);

    private final TutorDao tutorDAO = new TutorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }

        if ("reactivar".equals(accion)) {
            procesarReactivacion(request, response);
            return;
        }

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

        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }

        if ("reactivar".equals(accion)) {
            procesarReactivacion(request, response);
            return;
        }

        if ("cargaMasivaTutores".equals(accion)) {
            procesarCargaMasivaTutores(request, response);
            return;
        }

        Tutor tutor = new Tutor();
        String nominaStr = request.getParameter("nomina");

        boolean nominaValida = nominaStr != null && nominaStr.trim().matches(REGEX_NOMINA);
        if (nominaValida) {
            int nominaParseada = Integer.parseInt(nominaStr.trim());

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

        String[] horarios = request.getParameterValues("horariosDispo");
        if (horarios != null) {
            tutor.setHorariosDispo(Arrays.asList(horarios));
        }
        String idAcademiaStr = request.getParameter("idAcademia");
        if (idAcademiaStr != null && !idAcademiaStr.trim().isEmpty()) {
            tutor.setIdAcademia(Integer.parseInt(idAcademiaStr.trim()));
        } else if ("editar".equals(accion) && nominaValida) {

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

        String correo = tutor.getCorreoInstitucional();
        boolean correoValido = correo != null && correo.trim().matches(REGEX_CORREO);
        if (!correoValido) {
            forwardAFormulario(request, response, null, tutor, "correo_invalido");
            return;
        }

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

    private void procesarCargaMasivaTutores(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            Part parteArchivo = request.getPart("archivoExcel");
            if (parteArchivo == null || parteArchivo.getSize() == 0) {
                response.sendRedirect(request.getContextPath() + "/gestion-tutores?error=archivo_vacio");
                return;
            }

            Map<String, Integer> idsPorAcademia = new HashMap<>();
            for (Academia a : tutorDAO.getAllAcademias()) {
                if (a.getNombre() != null) {
                    idsPorAcademia.put(a.getNombre().trim().toLowerCase(Locale.ROOT), a.getIdAcademia());
                }
            }

            int siguienteNomina = tutorDAO.obtenerSiguienteNomina();

            List<Tutor> tutoresValidos = new ArrayList<>();

            List<String> filasInvalidas = new ArrayList<>();

            Set<String> correosEnLote = new HashSet<>();
            Set<String> telefonosEnLote = new HashSet<>();

            try (InputStream entrada = parteArchivo.getInputStream();
                 Workbook libro = WorkbookFactory.create(entrada)) {

                Sheet hoja = libro.getSheetAt(0);

                for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                    Row fila = hoja.getRow(f);
                    if (fila == null || esTextoVacio(obtenerTexto(fila.getCell(0)))) continue;

                    Tutor tutor = new Tutor();

                    tutor.setNumeroEmpleado(siguienteNomina);
                    siguienteNomina++;

                    tutor.setNombres(obtenerTexto(fila.getCell(0)));
                    tutor.setApellidoPaterno(obtenerTexto(fila.getCell(1)));
                    tutor.setApellidoMaterno(obtenerTexto(fila.getCell(2)));

                    String correo = obtenerTexto(fila.getCell(3));
                    if (esTextoVacio(correo)) {
                        String primerNombre = tutor.getNombres() != null
                                ? tutor.getNombres().trim().split("\\s+")[0] : "";
                        correo = limpiarParaCorreo(primerNombre + tutor.getApellidoPaterno()) + "@utez.edu.mx";
                    }
                    tutor.setCorreoInstitucional(correo);

                    String telefono = obtenerTexto(fila.getCell(4));
                    tutor.setTelefono(telefono != null ? telefono.replaceAll("[^0-9]", "") : null);

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

                        filasInvalidas.add("Fila " + (f + 1) + ": " + motivo);
                    }
                }
            }

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
            if (hasta.compareTo(desde) <= 0) return null;

            horarios.add(bloque);
        }

        return horarios.isEmpty() ? null : horarios;
    }

    private String validarFilaTutor(Tutor tutor, Set<String> correosEnLote, Set<String> telefonosEnLote) {
        if (esTextoVacio(tutor.getNombres()) || !tutor.getNombres().matches(REGEX_NOMBRE) || tutor.getNombres().length() > MAX_NOMBRES) {
            return "el nombre '" + tutor.getNombres() + "' es inválido o está vacío.";
        }
        if (esTextoVacio(tutor.getApellidoPaterno()) || !tutor.getApellidoPaterno().matches(REGEX_NOMBRE) || tutor.getApellidoPaterno().length() > MAX_APELLIDO) {
            return "el apellido paterno '" + tutor.getApellidoPaterno() + "' es inválido o está vacío.";
        }

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

    private static String limpiarParaCorreo(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

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
