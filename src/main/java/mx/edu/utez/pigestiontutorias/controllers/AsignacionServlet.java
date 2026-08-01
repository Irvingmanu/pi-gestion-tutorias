package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AsignacionServlet", value = "/asignacion")
public class AsignacionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CarreraDao carreraDao = new CarreraDao();
        List<Carrera> listaCarreras = carreraDao.getAll();

        TutorDao tutorDao = new TutorDao();
        List<Tutor> listaTutores = tutorDao.findAll();

        CuatrimestreDao cuatrimestreDao = new CuatrimestreDao();
        List<Cuatrimestre> listaCuatrimestres = cuatrimestreDao.getAll();

        LetraGrupoDao letraGrupoDao = new LetraGrupoDao();
        List<LetraGrupo> listaLetras = letraGrupoDao.getAll();

        AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
        List<AsignacionTutor> listaAsignaciones = asignacionTutorDao.getAll();

        request.setAttribute("carreras", listaCarreras);
        request.setAttribute("listaTutores", listaTutores);
        request.setAttribute("listaCuatrimestres", listaCuatrimestres);
        request.setAttribute("listaLetras", listaLetras);
        request.setAttribute("listaAsignaciones", listaAsignaciones);

        request.getRequestDispatcher("/coordinador/asignacion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        AsignacionTutorDao dao = new AsignacionTutorDao();
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idAsignacion = Integer.parseInt(request.getParameter("id_asignacion"));
            boolean eliminado = dao.delete(idAsignacion);
            String parametro = eliminado ? "exito=eliminado" : "error=true";
            response.sendRedirect(request.getContextPath() + "/asignacion?" + parametro);
            return;
        }

        int idTutor = Integer.parseInt(request.getParameter("id_tutor"));
        int idCarrera = Integer.parseInt(request.getParameter("id_carrera"));
        int idLetraGrupo = Integer.parseInt(request.getParameter("id_letra_grupo"));
        int idCuatrimestre = Integer.parseInt(request.getParameter("id_cuatrimestre"));

        if (dao.existeAsignacionActiva(idLetraGrupo, idCarrera, idCuatrimestre)) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=grupo_asignado");
            return;
        }

        AsignacionTutor nuevaAsignacion = new AsignacionTutor(idTutor, idCarrera, idLetraGrupo, idCuatrimestre);
        boolean guardado = dao.create(nuevaAsignacion);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/asignacion?exito=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=true");
        }
    }
}