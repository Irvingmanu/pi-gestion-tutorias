package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mx.edu.utez.pigestiontutorias.models.Carrera;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.Cuatrimestre;
import mx.edu.utez.pigestiontutorias.models.LetraGrupo;
import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;

import mx.edu.utez.pigestiontutorias.models.dao.CarreraDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.CuatrimestreDao;
import mx.edu.utez.pigestiontutorias.models.dao.LetraGrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AsignacionServlet", value = "/asignacion")
public class AsignacionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CarreraDao carreraDao = new CarreraDao();
        List<Carrera> listaCarreras = carreraDao.findAll();

        TutorDao tutorDao = new TutorDao();
        List<Tutor> listaTutores = tutorDao.findAll();

        CuatrimestreDao cuatrimestreDao = new CuatrimestreDao();
        List<Cuatrimestre> listaCuatrimestres = cuatrimestreDao.findAll();

        LetraGrupoDao letraGrupoDao = new LetraGrupoDao();
        List<LetraGrupo> listaLetras = letraGrupoDao.findAll();

        request.setAttribute("carreras", listaCarreras);
        request.setAttribute("listaTutores", listaTutores);
        request.setAttribute("listaCuatrimestres", listaCuatrimestres);
        request.setAttribute("listaLetras", listaLetras);

        request.getRequestDispatcher("/coordinador/asignacion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        int idTutor = Integer.parseInt(request.getParameter("id_tutor"));
        int idLetraGrupo = Integer.parseInt(request.getParameter("id_letra_grupo"));
        int idCuatrimestre = Integer.parseInt(request.getParameter("id_cuatrimestre"));

        AsignacionTutor nuevaAsignacion = new AsignacionTutor(idTutor, idLetraGrupo, idCuatrimestre);

        AsignacionTutorDao dao = new AsignacionTutorDao();
        boolean guardado = dao.insertar(nuevaAsignacion);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/asignacion?exito=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=true");
        }
    }
}