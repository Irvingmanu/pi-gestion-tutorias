package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "TutoresServlet", value = "/gestion-tutores")
public class TutoresServlet extends HttpServlet {

    private static final String REGEX_NOMINA = "^[0-9]{4}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";

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
            request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());

            if ("prepararEdicion".equals(accion)) {
                String nominaStr = request.getParameter("nomina");
                if (nominaStr != null && !nominaStr.trim().isEmpty()) {
                    int nomina = Integer.parseInt(nominaStr.trim());
                    Tutor tutorEdit = tutorDAO.getByNomina(nomina);
                    request.setAttribute("tutorEdit", tutorEdit);
                    request.setAttribute("tutor", tutorEdit);
                }
            }

            request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
            return;
        }

        // 3. CONSULTA Y LISTADO GENERAL
        List<Tutor> listaTutores = tutorDAO.getAll();
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
            // "0000" cumple el regex de 4 digitos pero no es una nomina real (parsea a 0).
            nominaValida = nominaParseada > 0;
            if (nominaValida) {
                tutor.setNomina(nominaParseada);
            }
        }

        tutor.setNombres(request.getParameter("nombres"));
        tutor.setApellidos(request.getParameter("apellidos"));
        tutor.setCorreoInstitucional(request.getParameter("correo"));
        tutor.setTelefono(request.getParameter("telefono"));

        String idTutorStr = request.getParameter("idTutor");
        if (idTutorStr != null && !idTutorStr.trim().isEmpty()) {
            tutor.setIdTutor(Integer.parseInt(idTutorStr.trim()));
        }

        String idUsuarioStr = request.getParameter("idUsuario");
        if (idUsuarioStr != null && !idUsuarioStr.trim().isEmpty()) {
            tutor.setIdUsuario(Integer.parseInt(idUsuarioStr.trim()));
        }

        // Capturar la lista de horarios enviados desde el formulario
        String[] horarios = request.getParameterValues("horariosDispo");
        if (horarios != null) {
            tutor.setHorariosDispo(Arrays.asList(horarios));
        }
        String idAcademiaStr = request.getParameter("idAcademia");
        if (idAcademiaStr != null && !idAcademiaStr.trim().isEmpty()) {
            tutor.setIdAcademia(Integer.parseInt(idAcademiaStr.trim()));
        } else if ("editar".equals(accion)) {
            // NUEVO: Si la academia viene vacía (ej. porque se deshabilitó en el frontend),
            // recuperamos el ID que ya tenía registrado en la base de datos para no reemplazarlo con un 0 (null).
            Tutor tutorAntiguo = tutorDAO.getByNomina(tutor.getNomina());
            if (tutorAntiguo != null) {
                tutor.setIdAcademia(tutorAntiguo.getIdAcademia());
            }
        }

        boolean esEdicion = "editar".equals(accion);

        if (!nominaValida) {
            request.setAttribute("error", "formato_invalido");
            request.setAttribute("tutor", tutor);
            request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());
            request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
            return;
        }

        // Blindaje de servidor: el <input> de correo valida el formato con "pattern" en el
        // HTML, pero eso es solo UX. Se revalida aqui por si el formulario se manipula o se
        // envia sin pasar por la validacion del navegador.
        String correo = tutor.getCorreoInstitucional();
        boolean correoValido = correo != null && correo.trim().matches(REGEX_CORREO);
        if (!correoValido) {
            request.setAttribute("error", "correo_invalido");
            request.setAttribute("tutor", tutor);
            request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());
            request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
            return;
        }

        // Blindaje de servidor: al menos un horario de atencion es obligatorio, ya que
        // el boton de Guardar solo lo exige en el cliente via JS.
        if (tutor.getHorariosDispo() == null || tutor.getHorariosDispo().isEmpty()) {
            request.setAttribute("error", "horario_requerido");
            request.setAttribute("tutor", tutor);
            request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());
            request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
            return;
        }

        String errorDuplicado = null;
        if (esEdicion) {
            if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional(), tutor.getIdTutor())) {
                errorDuplicado = "correo_duplicado";
            } else if (tutorDAO.existeTelefono(tutor.getTelefono(), tutor.getIdTutor())) {
                errorDuplicado = "telefono_duplicado";
            }
        } else {
            if (tutorDAO.existeNomina(tutor.getNomina())) {
                errorDuplicado = "nomina_duplicada";
            } else if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional())) {
                errorDuplicado = "correo_duplicado";
            } else if (tutorDAO.existeTelefono(tutor.getTelefono())) {
                errorDuplicado = "telefono_duplicado";
            }
        }

        if (errorDuplicado != null) {
            request.setAttribute("error", errorDuplicado);
            request.setAttribute("tutor", tutor);
            request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());
            request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
            return;
        }

        boolean operacionExitosa = esEdicion ? tutorDAO.update(tutor) : tutorDAO.create(tutor);

        if (operacionExitosa) {
            String exito = esEdicion ? "actualizado" : "guardado";
            response.sendRedirect(request.getContextPath() + "/gestion-tutores?exito=" + exito);
        } else {
            request.setAttribute("error", "registro_fallido");
            request.setAttribute("tutor", tutor);
            request.setAttribute("listaAcademias", tutorDAO.getAllAcademias());
            request.getRequestDispatcher("/coordinador/formulario-tutor.jsp").forward(request, response);
        }
    }

    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String parametro = "error=tutor_no_encontrado";
        String nominaStr = request.getParameter("nomina");
        if (nominaStr != null && !nominaStr.trim().isEmpty()) {
            try {
                int nomina = Integer.parseInt(nominaStr.trim());
                boolean eliminado = tutorDAO.delete(nomina);
                parametro = eliminado ? "exito=eliminado" : "error=tutor_en_uso";
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
                int nomina = Integer.parseInt(nominaStr.trim());
                boolean reactivado = tutorDAO.reactivar(nomina);
                parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            } catch (Exception e) {
                e.printStackTrace();
                parametro = "error=reactivacion_fallida";
            }
        }
        response.sendRedirect(request.getContextPath() + "/gestion-tutores?" + parametro);
    }
}
