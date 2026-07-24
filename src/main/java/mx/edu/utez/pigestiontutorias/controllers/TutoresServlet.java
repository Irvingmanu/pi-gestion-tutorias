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

        String idAcademiaStr = request.getParameter("idAcademia");
        if (idAcademiaStr != null && !idAcademiaStr.trim().isEmpty()) {
            tutor.setIdAcademia(Integer.parseInt(idAcademiaStr.trim()));
        }

        // Capturar la lista de horarios enviados desde el formulario
        String[] horarios = request.getParameterValues("horariosDispo");
        if (horarios != null) {
            tutor.setHorariosDispo(Arrays.asList(horarios));
        }

        HttpSession session = request.getSession();
        boolean operacionExitosa;

        if ("editar".equals(accion)) {
            operacionExitosa = tutorDAO.update(tutor);
            if (operacionExitosa) {
                session.setAttribute("mensajeExito", "Tutor actualizado exitosamente.");
            } else {
                session.setAttribute("mensajeError", "No se pudo actualizar el tutor.");
            }
        } else {
            // Caso por defecto: "nuevo"
            operacionExitosa = tutorDAO.create(tutor);
            if (operacionExitosa) {
                session.setAttribute("mensajeExito", "Tutor registrado exitosamente.");
            } else {
                session.setAttribute("mensajeError", "No se pudo registrar el tutor.");
            }
        }

        // Redireccionar al GET del Servlet para consultar la lista actualizada de la BD
        response.sendRedirect(request.getContextPath() + "/TutoresServlet");
    }

    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String nominaStr = request.getParameter("nomina");
        if (nominaStr != null && !nominaStr.trim().isEmpty()) {
            try {
                int nomina = Integer.parseInt(nominaStr.trim());
                boolean eliminado = tutorDAO.delete(nomina);
                if (eliminado) {
                    session.setAttribute("mensajeExito", "Tutor eliminado correctamente.");
                } else {
                    session.setAttribute("mensajeError", "No se pudo eliminar el tutor.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("mensajeError", "Ocurrió un error al intentar eliminar.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/TutoresServlet");
    }
}
