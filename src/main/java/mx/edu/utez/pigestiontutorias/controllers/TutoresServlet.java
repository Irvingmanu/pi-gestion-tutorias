package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "TutoresServlet", value = "/TutoresServlet")
public class TutoresServlet extends HttpServlet {

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
        if (nominaStr != null && !nominaStr.trim().isEmpty()) {
            tutor.setNomina(Integer.parseInt(nominaStr.trim()));
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

        // Código anterior (captura de datos)...

        HttpSession session = request.getSession();
        boolean operacionExitosa;

        if ("editar".equals(accion)) {
            // Para editar también deberías hacer validaciones usando tus métodos existeCorreo(correo, idTutorActual)
            operacionExitosa = tutorDAO.update(tutor);
            if (operacionExitosa) {
                session.setAttribute("mensajeExito", "Tutor actualizado exitosamente.");
            } else {
                session.setAttribute("mensajeError", "No se pudo actualizar el tutor.");
            }
        } else {
            // --- NUEVAS VALIDACIONES ANTES DE CREAR ---
            if (tutorDAO.existeNomina(tutor.getNomina())) {
                session.setAttribute("mensajeError", "Error: La nómina ya está registrada.");
                response.sendRedirect(request.getContextPath() + "/TutoresServlet");
                return;
            }

            if (tutorDAO.existeCorreo(tutor.getCorreoInstitucional())) {
                session.setAttribute("mensajeError", "Error: El correo ya está registrado.");
                response.sendRedirect(request.getContextPath() + "/TutoresServlet");
                return;
            }
            // ------------------------------------------

            operacionExitosa = tutorDAO.create(tutor);
            if (operacionExitosa) {
                session.setAttribute("mensajeExito", "Tutor registrado exitosamente.");
            } else {
                session.setAttribute("mensajeError", "No se pudo registrar el tutor.");
            }
        }

// Redireccionar al GET del Servlet
        response.sendRedirect(request.getContextPath() + "/TutoresServlet");
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
        response.sendRedirect(request.getContextPath() + "/TutoresServlet?" + parametro);
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
        response.sendRedirect(request.getContextPath() + "/TutoresServlet?" + parametro);
    }
}
