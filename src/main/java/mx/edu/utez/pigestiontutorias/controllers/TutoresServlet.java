package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// NOMINA ya no es una columna separada: NUMERO_EMPLEADO es el unico identificador del
// tutor (PK real de TUTOR), asi que cumple el mismo papel que antes tenia "nomina".
@WebServlet(name = "TutoresServlet", value = "/gestion-tutores")
public class TutoresServlet extends HttpServlet {

    private static final String REGEX_NOMINA = "^[0-9]{4}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
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
}
