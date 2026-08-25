package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.AsignacionTutor;
import mx.edu.utez.pigestiontutorias.models.Grupo;
import mx.edu.utez.pigestiontutorias.models.Tutor;
import mx.edu.utez.pigestiontutorias.models.dao.AcademiaDao;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;
import mx.edu.utez.pigestiontutorias.models.dao.GrupoDao;
import mx.edu.utez.pigestiontutorias.models.dao.TutorDao;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "AsignacionServlet", value = "/asignacion")
public class AsignacionServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final GrupoDao grupoDao = new GrupoDao();
    private final AcademiaDao academiaDao = new AcademiaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Tutor> listaTutores = tutorDao.findAll();
        List<AsignacionTutor> listaAsignaciones = asignacionTutorDao.getAll();

        List<Grupo> listaGrupos = grupoDao.getDisponiblesParaAsignacion().stream()
                .filter(grupo -> !esCuatrimestreBloqueado(grupo))
                .collect(Collectors.toList());

        request.setAttribute("listaTutores", listaTutores);
        request.setAttribute("listaAsignaciones", listaAsignaciones);
        request.setAttribute("listaGrupos", listaGrupos);

        request.setAttribute("listaAcademias", academiaDao.getAll());

        request.getRequestDispatcher("/coordinador/asignacion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idAsignacion = Integer.parseInt(request.getParameter("id_asignacion"));

            AsignacionTutor asignacion = asignacionTutorDao.getById(idAsignacion);
            if (asignacion == null) {
                response.sendRedirect(request.getContextPath() + "/asignacion?error=true");
                return;
            }

            if (asignacionTutorDao.tienePendientesEnGrupo(asignacion.getIdTutor(), asignacion.getIdGrupo())) {
                response.sendRedirect(request.getContextPath() + "/asignacion?error=asignacion_con_pendientes");
                return;
            }

            boolean eliminado = asignacionTutorDao.delete(idAsignacion);
            String parametro = eliminado ? "exito=eliminado" : "error=true";
            response.sendRedirect(request.getContextPath() + "/asignacion?" + parametro);
            return;
        }

        int idTutor = Integer.parseInt(request.getParameter("id_tutor"));
        int idGrupo = Integer.parseInt(request.getParameter("id_grupo"));

        Grupo grupo = grupoDao.getById(idGrupo);
        if (grupo == null || !"S".equals(grupo.getEstado())) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=true");
            return;
        }

        Tutor tutor = tutorDao.getById(idTutor);
        if (tutor == null || tutor.getIdAcademia() != grupo.getIdAcademia()) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=academia_no_coincide");
            return;
        }

        if (esCuatrimestreBloqueado(grupo)) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=cuatrimestre_no_permitido");
            return;
        }

        if (asignacionTutorDao.existeAsignacionActiva(idGrupo)) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=grupo_asignado");
            return;
        }

        if (!grupoDao.tieneAlumnosActivos(idGrupo)) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=grupo_sin_alumnos");
            return;
        }

        AsignacionTutor nuevaAsignacion = new AsignacionTutor(idTutor, idGrupo);
        boolean guardado = asignacionTutorDao.create(nuevaAsignacion);

        if (guardado) {
            response.sendRedirect(request.getContextPath() + "/asignacion?exito=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=true");
        }
    }

    private static final String CARRERA_EXCEPCION_CUATRIMESTRE = "terapia fisica";

    private boolean esCarreraExceptuada(String nombreCarrera) {
        if (nombreCarrera == null) return false;
        String normalizado = Normalizer.normalize(nombreCarrera.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalizado.equals(CARRERA_EXCEPCION_CUATRIMESTRE);
    }

    private boolean esCuatrimestreBloqueado(Grupo grupo) {
        int cuatrimestre = grupo.getCuatrimestre();
        boolean esCuatrimestreRestringido = cuatrimestre == 6 || cuatrimestre == 10;
        return esCuatrimestreRestringido && !esCarreraExceptuada(grupo.getNombreCarrera());
    }
}
