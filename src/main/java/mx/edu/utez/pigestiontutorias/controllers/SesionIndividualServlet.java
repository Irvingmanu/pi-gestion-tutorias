package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.*;

import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/SesionIndividualServlet")
public class SesionIndividualServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final AreaDAO areaDAO = new AreaDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();
    private final CanalizacionDao canalizacionDao = new CanalizacionDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.findByIdUsuario((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        cargarListas(request, tutor);
        request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.findByIdUsuario((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        String temasTratados = request.getParameter("temasTratados");
        String acuerdos = request.getParameter("acuerdos");
        String[] idMotivos = request.getParameterValues("idMotivo");
        String idSesionStr = request.getParameter("idSesion");

        boolean esCompletado = idSesionStr != null && !idSesionStr.isBlank();

        if (temasTratados == null || temasTratados.isBlank() || acuerdos == null || acuerdos.isBlank()) {
            request.setAttribute("error", "Completa todos los campos obligatorios.");
            cargarListas(request, tutor);
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
            return;
        }

        boolean guardado;

        if (esCompletado) {
            int idSesion = Integer.parseInt(idSesionStr.trim());
            guardado = sesionIndividualDao.completarSesion(idSesion, temasTratados, acuerdos, idMotivos);
        } else {
            String matricula = request.getParameter("matricula");
            String fechaStr = request.getParameter("fecha");
            String hora = request.getParameter("hora");

            if (matricula == null || matricula.isBlank() || fechaStr == null || fechaStr.isBlank()
                    || hora == null || hora.isBlank()) {
                request.setAttribute("error", "Completa todos los campos obligatorios.");
                cargarListas(request, tutor);
                request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
                return;
            }

            matricula = matricula.trim();

            // Blindaje contra ORA-02291: SESION_INDIVIDUAL.MATRICULA es FK a ALUMNO.MATRICULA,
            // asi que una matricula mal formada o inexistente revienta el INSERT en el DAO.
            if (matricula.length() != 10) {
                response.sendRedirect(request.getContextPath() + "/SesionIndividualServlet?error=matricula_invalida");
                return;
            }

            if (alumnoDAO.getById(matricula) == null) {
                response.sendRedirect(request.getContextPath() + "/SesionIndividualServlet?error=matricula_no_existe");
                return;
            }

            Integer idCanalizacionPrincipal = registrarCanalizaciones(idMotivos, matricula);

            SesionIndividual sesion = new SesionIndividual();
            sesion.setIdTutor(tutor.getIdTutor());
            sesion.setMatricula(matricula);
            sesion.setFecha(Date.valueOf(fechaStr));
            sesion.setHora(hora);
            sesion.setTemasTratados(temasTratados);
            sesion.setAcuerdos(acuerdos);
            sesion.setIdCanalizacion(idCanalizacionPrincipal);
            sesion.setEstado("Tomada");

            guardado = sesionIndividualDao.create(sesion);
        }

        if (guardado) {
            String exito = esCompletado ? "completada" : "tutoria_guardada";
            response.sendRedirect(request.getContextPath() + "/SesionIndividualServlet?exito=" + exito);
        } else {
            request.setAttribute("error", "Ocurrió un error al guardar el registro. Intenta de nuevo.");
            cargarListas(request, tutor);
            request.getRequestDispatcher("/tutor/tutoria-individual.jsp").forward(request, response);
        }
    }

    // Crea una CANALIZACION por cada motivo seleccionado en "Vinculo Directo" y devuelve
    // la primera generada, para enlazarla como ID_CANALIZACION de la sesion nueva.
    private Integer registrarCanalizaciones(String[] idMotivos, String matricula) {
        if (idMotivos == null) {
            return null;
        }

        Integer idPrincipal = null;

        for (String idMotivoStr : idMotivos) {
            if (idMotivoStr == null || idMotivoStr.isBlank()) {
                continue;
            }

            Motivo motivo = motivoDAO.getById(Integer.parseInt(idMotivoStr.trim()));
            if (motivo == null) {
                continue;
            }

            Canalizacion c = new Canalizacion();
            c.setIdArea(motivo.getIdArea());
            c.setIdMotivo(motivo.getIdMotivo());
            c.setMatricula(matricula);
            c.setObservaciones(motivo.getNombreMotivo());

            int idGenerado = canalizacionDao.crearYObtenerId(c);
            if (idGenerado > 0 && idPrincipal == null) {
                idPrincipal = idGenerado;
            }
        }

        return idPrincipal;
    }

    private void cargarListas(HttpServletRequest request, Tutor tutor) {
        List<SesionIndividual> sesionesProgramadas = sesionIndividualDao.getSesionesProgramadasByTutor(tutor.getIdTutor());
        List<Area> areasConMotivos = areaDAO.getAllConMotivos();

        Map<String, String> nombresAlumnos = new HashMap<>();
        for (SesionIndividual s : sesionesProgramadas) {
            if (!nombresAlumnos.containsKey(s.getMatricula())) {
                Alumno alumno = alumnoDAO.getById(s.getMatricula());
                nombresAlumnos.put(s.getMatricula(), alumno != null ? alumno.getNombres() + " " + alumno.getApellidos() : s.getMatricula());
            }
        }

        request.setAttribute("sesionesProgramadas", sesionesProgramadas);
        request.setAttribute("areasConMotivos", areasConMotivos);
        request.setAttribute("nombresAlumnos", nombresAlumnos);
    }
}
