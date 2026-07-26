package mx.edu.utez.pigestiontutorias.controllers;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.EventoAgenda;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/AgendaServlet")
public class AgendaServlet extends HttpServlet {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Object usuarioLogueado = session.getAttribute("usuario");

        String matricula = null;
        int idLetraGrupo = 0;

        // Intentar extraer del objeto Alumno si está en sesión
        if (usuarioLogueado instanceof Alumno) {
            Alumno alumno = (Alumno) usuarioLogueado;
            matricula = alumno.getMatricula();
            idLetraGrupo = alumno.getIdLetraGrupo();
        }

        // Si la matrícula sigue nula, buscar en los atributos de sesión o parámetros de la URL
        if (matricula == null) {
            matricula = (String) session.getAttribute("matricula");
        }
        if (matricula == null || matricula.isEmpty()) {
            matricula = request.getParameter("matricula");
        }

        // Si el idLetraGrupo sigue en 0, buscar en los atributos de sesión o parámetros de la URL
        if (idLetraGrupo == 0) {
            Object objGrupo = session.getAttribute("idLetraGrupo");
            if (objGrupo != null) {
                try {
                    idLetraGrupo = Integer.parseInt(objGrupo.toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                String paramGrupo = request.getParameter("idLetraGrupo");
                if (paramGrupo != null && !paramGrupo.isEmpty()) {
                    try {
                        idLetraGrupo = Integer.parseInt(paramGrupo);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // ==================== RESPALDO DE SEGURIDAD ====================
        if (matricula == null || matricula.isEmpty()) {
            matricula = "20253DS076";
        }
        if (idLetraGrupo == 0) {
            idLetraGrupo = 3;
        }
        // ===============================================================

        // Depuración en consola para verificar qué valores están viajando
        System.out.println("DEBUG AGENDA -> Matrícula: " + matricula + " | ID Grupo: " + idLetraGrupo);

        // Obtener eventos desde el DAO
        List<EventoAgenda> listaEventos = alumnoDAO.getAgendaAlumno(matricula, idLetraGrupo);

        request.setAttribute("listaEventosAgenda", listaEventos);
        request.getRequestDispatcher("/alumno/agenda.jsp").forward(request, response);
    }
}