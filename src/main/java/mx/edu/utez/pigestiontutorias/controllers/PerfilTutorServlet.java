package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.CambioPasswordUtil;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;

import java.io.IOException;

@WebServlet(name = "PerfilTutorServlet", value = "/perfilTutor")
public class PerfilTutorServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer numeroEmpleado = session != null ? (Integer) session.getAttribute("idUsuario") : null;
        Tutor tutor = numeroEmpleado != null ? tutorDao.getById(numeroEmpleado) : null;
        request.setAttribute("tutor", tutor);
        request.getRequestDispatcher("/tutor/perfil.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        Integer numeroEmpleado = session != null ? (Integer) session.getAttribute("idUsuario") : null;

        if (numeroEmpleado == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Tu sesión expiró, inicia sesión de nuevo.\"}");
            return;
        }

        Tutor tutor = tutorDao.getById(numeroEmpleado);
        if (tutor == null) {
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"No se encontró la información del tutor.\"}");
            return;
        }

        String accion = request.getParameter("accion");

        if ("verificarPassword".equals(accion)) {
            String passwordActual = request.getParameter("passwordActual");
            response.getWriter().write(CambioPasswordUtil.verificarPassword(passwordActual, tutor.getPass()));
            return;
        }

        if ("cambiarPassword".equals(accion)) {
            String passwordActual = request.getParameter("passwordActual");
            String passwordNueva = request.getParameter("passwordNueva");
            String passwordConfirmar = request.getParameter("passwordConfirmar");

            String resultado = CambioPasswordUtil.cambiarPassword(
                    passwordActual, passwordNueva, passwordConfirmar, tutor.getPass(),
                    nuevaPassSinHash -> tutorDao.actualizarPassword(tutor.getNumeroEmpleado(), nuevaPassSinHash)
            );

            if (CambioPasswordUtil.fueExitoso(resultado)) {
                try {
                    new EmailSender().enviarConfirmacionCambio(tutor.getCorreoInstitucional());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            response.getWriter().write(resultado);
            return;
        }

        response.getWriter().write("{\"exito\":false,\"mensaje\":\"Acción no reconocida.\"}");
    }
}