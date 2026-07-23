package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Controlador temporal: redirige a la primera opcion del menu del tutor
// (Tutoria Individual) tras el login. El registro (doPost, action=registrarIndividual)
// enviado desde esa vista se implementara junto con el resto de la logica de tutorias.
@WebServlet("/TutoriaServlet")
public class TutoriaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        request.getRequestDispatcher("/tutor/registro-individual.jsp").forward(request, response);
    }
}
