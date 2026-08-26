package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.Coordinador;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.CoordinadorDAO;
import mx.edu.utez.pigestiontutorias.models.dao.TokenDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.utils.EmailSender;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * Servlet que gestiona el flujo de recuperación de contraseña para alumnos,
 * tutores y coordinadores: solicitud de código por correo, verificación del
 * código y cambio final de contraseña con notificación por correo.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-21
 */
@WebServlet(name = "RecuperarServlet", urlPatterns = {"/recuperar"})
public class RecuperarServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final TutorDao tutorDao = new TutorDao();
    private final CoordinadorDAO coordinadorDAO = new CoordinadorDAO();
    private final TokenDao tokenDao = new TokenDao();
    private final EmailSender emailSender = new EmailSender();

    private static final int MAX_PASS_TEXTO_PLANO = 50;

    /**
     * Atiende las peticiones POST del flujo de recuperación de contraseña,
     * enrutando según el parámetro "action" hacia la solicitud de código,
     * la verificación del código o el cambio final de contraseña.
     * @param request petición HTTP con el parámetro "action" y los datos del paso correspondiente
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista JSP
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "solicitar";

        switch (action) {
            case "solicitar" -> solicitarCodigo(request, response);
            case "verificar" -> verificarCodigo(request, response);
            case "cambiar" -> cambiarPassword(request, response);
            default -> response.sendRedirect("recuperar-contra.jsp");
        }
    }

    /**
     * Genera un código de recuperación y lo asocia al alumno, tutor o coordinador
     * cuyo correo coincida con el dato recibido, enviándolo por correo si se pudo crear.
     * Por seguridad, siempre muestra el mismo mensaje genérico sin revelar si el correo existe.
     * @param request petición HTTP con el parámetro "dato" (correo a buscar)
     * @param response respuesta HTTP usada para reenviar a la vista de recuperación
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void solicitarCodigo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String correo = request.getParameter("dato");
        correo = correo != null ? correo.trim() : null;

        if (correo != null && !correo.isBlank()) {
            String codigo = generarCodigo(6);
            boolean creado = false;

            Alumno alumno = alumnoDAO.findByCorreo(correo);
            Tutor tutor = alumno == null ? tutorDao.findByCorreo(correo) : null;
            Coordinador coordinador = (alumno == null && tutor == null) ? coordinadorDAO.findByCorreo(correo) : null;

            if (alumno != null) {
                creado = tokenDao.crearParaAlumno(codigo, alumno.getMatricula());
            } else if (tutor != null) {
                creado = tokenDao.crearParaTutor(codigo, tutor.getNumeroEmpleado());
            } else if (coordinador != null) {
                creado = tokenDao.crearParaCoordinador(codigo, coordinador.getNumeroEmpleado());
            }

            if (creado) {
                emailSender.enviarCodigoRecuperacion(correo, codigo);
            }
        }

        request.setAttribute("mensajeInfo", "Si el dato se encuentra registrado, te llegará un correo con instrucciones.");
        request.setAttribute("step", "verificar");
        request.getRequestDispatcher("recuperar-contra.jsp").forward(request, response);
    }

    /**
     * Verifica que el código de recuperación ingresado sea válido y esté vigente;
     * si lo es, guarda en sesión los datos del token para permitir el cambio de contraseña.
     * @param request petición HTTP con el parámetro "codigo" ingresado por el usuario
     * @param response respuesta HTTP usada para reenviar a la vista de recuperación
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void verificarCodigo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String codigo = request.getParameter("codigo");
        TokenDao.TokenInfo info = codigo != null && !codigo.isBlank() ? tokenDao.buscarVigente(codigo.trim()) : null;

        if (info != null) {
            HttpSession session = request.getSession();
            session.setAttribute("idTokenRecuperacion", info.idToken);
            session.setAttribute("rolRecuperacion", info.rol);
            session.setAttribute("matriculaRecuperacion", info.matricula);
            session.setAttribute("numeroEmpleadoRecuperacion", info.numeroEmpleado);

            request.setAttribute("step", "cambiar");
            request.getRequestDispatcher("recuperar-contra.jsp").forward(request, response);
        } else {
            request.setAttribute("mensajeError", "Código incorrecto o expirado, intenta de nuevo.");
            request.setAttribute("step", "verificar");
            request.getRequestDispatcher("recuperar-contra.jsp").forward(request, response);
        }
    }

    /**
     * Aplica el cambio de contraseña para el usuario (alumno, tutor o coordinador)
     * asociado al token de recuperación vigente en sesión, validando longitud máxima
     * y coincidencia de las dos contraseñas capturadas, y notificando por correo el cambio.
     * @param request petición HTTP con los parámetros pass1 y pass2 y la sesión con el token de recuperación
     * @param response respuesta HTTP usada para redirigir o reenviar a la vista correspondiente
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
    private void cambiarPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer idToken = (Integer) session.getAttribute("idTokenRecuperacion");
        String rol = (String) session.getAttribute("rolRecuperacion");
        String matricula = (String) session.getAttribute("matriculaRecuperacion");
        Integer numeroEmpleado = (Integer) session.getAttribute("numeroEmpleadoRecuperacion");

        if (idToken == null || rol == null) {
            response.sendRedirect("recuperar-contra.jsp");
            return;
        }

        String pass1 = request.getParameter("pass1");
        String pass2 = request.getParameter("pass2");
        pass1 = pass1 != null ? pass1.trim() : null;
        pass2 = pass2 != null ? pass2.trim() : null;

        if (pass1 != null && pass1.length() > MAX_PASS_TEXTO_PLANO) {
            request.setAttribute("mensajeError", "La contraseña no puede superar los " + MAX_PASS_TEXTO_PLANO + " caracteres.");
            request.setAttribute("step", "cambiar");
            request.getRequestDispatcher("recuperar-contra.jsp").forward(request, response);
            return;
        }

        if (pass1 != null && pass1.equals(pass2) && !pass1.isEmpty()) {
            boolean actualizado = false;
            String correoNotificar = null;

            switch (rol) {
                case "Alumno" -> {
                    actualizado = alumnoDAO.actualizarPassword(matricula, pass1);
                    if (actualizado) {
                        Alumno a = alumnoDAO.getById(matricula);
                        correoNotificar = a != null ? a.getCorreoInstitucional() : null;
                    }
                }
                case "Tutor" -> {
                    actualizado = tutorDao.actualizarPassword(numeroEmpleado, pass1);
                    if (actualizado) {
                        Tutor t = tutorDao.getById(numeroEmpleado);
                        correoNotificar = t != null ? t.getCorreoInstitucional() : null;
                    }
                }
                case "Coordinador" -> {
                    actualizado = coordinadorDAO.actualizarPassword(numeroEmpleado, pass1);
                    if (actualizado) {
                        Coordinador c = coordinadorDAO.getById(numeroEmpleado);
                        correoNotificar = c != null ? c.getCorreoInstitucional() : null;
                    }
                }
            }

            if (actualizado) {
                tokenDao.marcarUtilizado(idToken);
                if (correoNotificar != null) {
                    emailSender.enviarConfirmacionCambio(correoNotificar);
                }
                session.removeAttribute("idTokenRecuperacion");
                session.removeAttribute("rolRecuperacion");
                session.removeAttribute("matriculaRecuperacion");
                session.removeAttribute("numeroEmpleadoRecuperacion");

                request.setAttribute("mensajeExito", "Tu contraseña ha sido cambiada exitosamente.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            } else {
                request.setAttribute("mensajeError", "Error al actualizar la contraseña.");
                request.setAttribute("step", "cambiar");
                request.getRequestDispatcher("recuperar-contra.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("mensajeError", "Las contraseñas no coinciden o están vacías.");
            request.setAttribute("step", "cambiar");
            request.getRequestDispatcher("recuperar-contra.jsp").forward(request, response);
        }
    }

    /**
     * Genera un código alfanumérico aleatorio y criptográficamente seguro
     * de la longitud indicada, usado como código de recuperación.
     * @param longitud la cantidad de caracteres del código a generar
     * @return el código alfanumérico generado en mayúsculas
     */
    private String generarCodigo(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }
}
