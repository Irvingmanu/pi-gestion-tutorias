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
import java.util.*;

@WebServlet(name = "HistorialServlet", value = "/historial-tutorias")
public class HistorialServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final GrupoDao grupoDao = new GrupoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Tutor tutor = tutorDao.getById((Integer) session.getAttribute("idUsuario"));
        if (tutor == null) {
            request.setAttribute("error", "No se encontró el perfil de tutor asociado a tu cuenta.");
            request.setAttribute("paginaActiva", "historial");
            request.getRequestDispatcher("/tutor/historial.jsp").forward(request, response);
            return;
        }

        String tipo = request.getParameter("tipo");
        String fechaInicio = request.getParameter("fechaInicio");
        String fechaFin = request.getParameter("fechaFin");
        if (tipo == null) tipo = "";

        List<HistorialItemDTO> historial = new ArrayList<>();

        if (tipo.isBlank() || tipo.equals("grupal")) {

            Map<Integer, Grupo> gruposCache = new HashMap<>();
            List<SesionGrupal> grupales = sesionGrupalDao.getHistorialByTutor(tutor.getNumeroEmpleado(), fechaInicio, fechaFin);
            for (SesionGrupal sg : grupales) {
                Grupo grupo = gruposCache.computeIfAbsent(sg.getIdGrupo(), grupoDao::getById);

                HistorialItemDTO item = new HistorialItemDTO();
                item.setTipo("Grupal");
                item.setFecha(sg.getFecha());
                item.setHora(sg.getHora());
                item.setReferencia(grupo != null ? grupo.getNombreGrupo() : "Grupo " + sg.getIdGrupo());
                item.setTemasTratados(sg.getTemasTratados());
                item.setAcuerdos(sg.getAcuerdos());
                item.setEstado(sg.getEstado());
                item.setIdGrupo(sg.getIdGrupo());
                historial.add(item);
            }
        }

        if (tipo.isBlank() || tipo.equals("individual")) {
            List<SesionIndividual> individuales = sesionIndividualDao.getHistorialByTutor(tutor.getNumeroEmpleado(), "Programada", fechaInicio, fechaFin);
            for (SesionIndividual si : individuales) {
                historial.add(mapearIndividual(si, "Individual"));
            }
        }

        if (tipo.isBlank() || tipo.equals("espontanea")) {
            List<SesionIndividual> espontaneas = sesionIndividualDao.getHistorialByTutor(tutor.getNumeroEmpleado(), "Espontanea", fechaInicio, fechaFin);
            for (SesionIndividual si : espontaneas) {
                historial.add(mapearIndividual(si, "Espontanea"));
            }
        }

        historial.sort(Comparator.comparing(HistorialItemDTO::getFecha).reversed()
                .thenComparing(HistorialItemDTO::getHora, Comparator.nullsLast(Comparator.reverseOrder())));

        request.setAttribute("historial", historial);
        request.setAttribute("tipoSeleccionado", tipo);
        request.setAttribute("fechaInicioSeleccionada", fechaInicio);
        request.setAttribute("fechaFinSeleccionada", fechaFin);
        request.setAttribute("paginaActiva", "historial");
        request.getRequestDispatcher("/tutor/historial.jsp").forward(request, response);
    }

    private HistorialItemDTO mapearIndividual(SesionIndividual si, String tipoEtiqueta) {
        Alumno alumno = alumnoDAO.getById(si.getMatricula());
        String nombreAlumno = alumno != null
                ? alumno.getNombres() + " " + alumno.getApellidos() + " (" + si.getMatricula() + ")"
                : si.getMatricula();

        HistorialItemDTO item = new HistorialItemDTO();
        item.setTipo(tipoEtiqueta);
        item.setFecha(si.getFecha());
        item.setHora(si.getHora());
        item.setReferencia(nombreAlumno);
        item.setTemasTratados(si.getTemasTratados());
        item.setAcuerdos(si.getAcuerdos());
        item.setEstado(si.getEstado());
        return item;
    }
}
