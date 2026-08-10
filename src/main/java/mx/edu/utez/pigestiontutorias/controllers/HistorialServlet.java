package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.SesionGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.SesionIndividualDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "HistorialServlet", value = "/historial-tutorias")
public class HistorialServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final SesionGrupalDao sesionGrupalDao = new SesionGrupalDao();
    private final SesionIndividualDao sesionIndividualDao = new SesionIndividualDao();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

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
            request.setAttribute("paginaActiva", "historial");
            request.getRequestDispatcher("/tutor/historial.jsp").forward(request, response);
            return;
        }

        // Filtros: tipo = "grupal" | "individual" | "espontanea" | "" (todos)
        String tipo = request.getParameter("tipo");
        String fechaInicio = request.getParameter("fechaInicio");
        String fechaFin = request.getParameter("fechaFin");
        if (tipo == null) tipo = "";

        // Mapas ID -> texto para armar etiquetas legibles del grupo (mismo patron que AlumnoServlet)
        Map<Integer, String> nombresCarrera = new HashMap<>();
        for (Carrera c : alumnoDAO.getAllCarreras()) {
            nombresCarrera.put(c.getIdCarrera(), c.getNombre());
        }
        Map<Integer, Integer> numerosCuatrimestre = new HashMap<>();
        for (Cuatrimestre c : alumnoDAO.getAllCuatrimestres()) {
            numerosCuatrimestre.put(c.getIdCuatrimestre(), c.getNumero());
        }
        Map<Integer, String> nombresLetra = new HashMap<>();
        for (LetraGrupo l : alumnoDAO.getAllLetrasGrupo()) {
            nombresLetra.put(l.getIdLetra(), l.getLetra());
        }

        List<HistorialItemDTO> historial = new ArrayList<>();

        if (tipo.isBlank() || tipo.equals("grupal")) {
            List<SesionGrupal> grupales = sesionGrupalDao.getHistorialByTutor(tutor.getIdTutor(), fechaInicio, fechaFin);
            for (SesionGrupal sg : grupales) {
                HistorialItemDTO item = new HistorialItemDTO();
                item.setTipo("Grupal");
                item.setFecha(sg.getFecha());
                item.setHora(sg.getHora());
                item.setReferencia(etiquetaGrupo(sg.getIdCarrera(), sg.getIdCuatrimestre(), sg.getIdLetraGrupo(),
                        nombresCarrera, numerosCuatrimestre, nombresLetra));
                item.setTemasTratados(sg.getTemasTratados());
                item.setAcuerdos(sg.getAcuerdos());
                item.setEstado(sg.getEstado());
                historial.add(item);
            }
        }

        if (tipo.isBlank() || tipo.equals("individual")) {
            List<SesionIndividual> individuales = sesionIndividualDao.getHistorialByTutor(tutor.getIdTutor(), "Programada", fechaInicio, fechaFin);
            for (SesionIndividual si : individuales) {
                historial.add(mapearIndividual(si, "Individual"));
            }
        }

        if (tipo.isBlank() || tipo.equals("espontanea")) {
            List<SesionIndividual> espontaneas = sesionIndividualDao.getHistorialByTutor(tutor.getIdTutor(), "Espontanea", fechaInicio, fechaFin);
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

    private String etiquetaGrupo(int idCarrera, int idCuatrimestre, int idLetra,
                                 Map<Integer, String> nombresCarrera, Map<Integer, Integer> numerosCuatrimestre,
                                 Map<Integer, String> nombresLetra) {
        String carrera = nombresCarrera.getOrDefault(idCarrera, "Carrera " + idCarrera);
        Integer numero = numerosCuatrimestre.get(idCuatrimestre);
        String letra = nombresLetra.getOrDefault(idLetra, "?");
        return carrera + " - " + (numero != null ? numero + "°" : "Cuatri " + idCuatrimestre) + " " + letra;
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