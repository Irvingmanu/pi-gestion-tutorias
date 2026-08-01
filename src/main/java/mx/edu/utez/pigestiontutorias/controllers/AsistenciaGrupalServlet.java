package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.dao.AsistenciaGrupalDao;
import mx.edu.utez.pigestiontutorias.models.dao.CarreraDao;
import mx.edu.utez.pigestiontutorias.models.dao.CuatrimestreDao;
import mx.edu.utez.pigestiontutorias.models.dao.LetraGrupoDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AsistenciaGrupalServlet", value = "/tutor/asistencia-grupal")
public class AsistenciaGrupalServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LetraGrupoDao grupoDao = new LetraGrupoDao();
        CarreraDao carreraDao = new CarreraDao();
        CuatrimestreDao cuatrimestreDao = new CuatrimestreDao();
        AsistenciaGrupalDao asistenciaDao = new AsistenciaGrupalDao();

        // 1. Cargar las listas para los menús desplegables (selects)
        request.setAttribute("listaGrupos", grupoDao.getAll());
        request.setAttribute("listaCarreras", carreraDao.getAll());
        request.setAttribute("listaCuatrimestres", cuatrimestreDao.getAll());

        // 2. Capturar los filtros si el usuario ya los seleccionó
        String grupoParam = request.getParameter("idLetraGrupo");
        String carreraParam = request.getParameter("idCarrera");
        String cuatrimestreParam = request.getParameter("idCuatrimestre");
        String fechaParam = request.getParameter("fecha");

// Validar que no sean nulos ni vacíos antes de parsear
        if (grupoParam != null && !grupoParam.trim().isEmpty() &&
                carreraParam != null && !carreraParam.trim().isEmpty() &&
                cuatrimestreParam != null && !cuatrimestreParam.trim().isEmpty()) {

            try {
                int idGrupo = Integer.parseInt(grupoParam);
                int idCarrera = Integer.parseInt(carreraParam);
                int idCuatrimestre = Integer.parseInt(cuatrimestreParam);

                List<Alumno> listaAlumnos = asistenciaDao.getAlumnosPorFiltros(idGrupo, idCarrera, idCuatrimestre);
                request.setAttribute("listaAlumnosGrupo", listaAlumnos);

                if (fechaParam != null && !fechaParam.trim().isEmpty()) {
                    List<Integer> idsAsistidos = asistenciaDao.getAlumnosConAsistencia(idGrupo, idCarrera, idCuatrimestre, fechaParam);
                    request.setAttribute("idsAsistidos", idsAsistidos);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Redirigir a tu vista JSP
        request.getRequestDispatcher("/tutor/asistencia-grupal.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String grupo = request.getParameter("idLetraGrupo");
        String carrera = request.getParameter("idCarrera");
        String cuatrimestre = request.getParameter("idCuatrimestre");
        String fecha = request.getParameter("fecha");

        String[] alumnosAsistentes = request.getParameterValues("asistencia");

        jakarta.servlet.http.HttpSession session = request.getSession();
        Integer idTutor = (Integer) session.getAttribute("idTutor");

        if (idTutor == null) {
            idTutor = 1;
        }

        AsistenciaGrupalDao dao = new AsistenciaGrupalDao();

        dao.registrarAsistenciaGrupal(grupo, carrera, cuatrimestre, fecha, alumnosAsistentes, idTutor);

        String redirectUrl = request.getContextPath() + "/tutor/asistencia-grupal" +
                "?idLetraGrupo=" + (grupo != null ? grupo : "") +
                "&idCarrera=" + (carrera != null ? carrera : "") +
                "&idCuatrimestre=" + (cuatrimestre != null ? cuatrimestre : "") +
                "&fecha=" + (fecha != null ? fecha : "") +
                "&exito=asistencia_guardada"; // <-- Parámetro agregado

        response.sendRedirect(redirectUrl);
    }
}
