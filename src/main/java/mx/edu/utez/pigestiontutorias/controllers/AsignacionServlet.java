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

/**
 * Servlet que gestiona la asignación de tutores a grupos: listado de asignaciones
 * vigentes, alta de una nueva asignación validando academia, cuatrimestre permitido
 * y disponibilidad del grupo, y eliminación de asignaciones sin pendientes.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-22
 */
@WebServlet(name = "AsignacionServlet", value = "/asignacion")
public class AsignacionServlet extends HttpServlet {

    private final TutorDao tutorDao = new TutorDao();
    private final AsignacionTutorDao asignacionTutorDao = new AsignacionTutorDao();
    private final GrupoDao grupoDao = new GrupoDao();
    private final AcademiaDao academiaDao = new AcademiaDao();

    /**
     * Atiende la petición GET, cargando el listado de tutores, asignaciones vigentes,
     * grupos disponibles para asignar (excluyendo cuatrimestres bloqueados) y academias,
     * para la vista de gestión de asignaciones.
     * @param request petición HTTP en curso
     * @param response respuesta HTTP usada para reenviar a la vista JSP
     * @throws ServletException si ocurre un error al reenviar la petición
     * @throws IOException si ocurre un error de entrada/salida al procesar la petición
     */
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

    /**
     * Atiende la petición POST del módulo de asignaciones: elimina una asignación
     * existente si no tiene pendientes en el grupo, o bien crea una nueva asignación
     * de tutor a grupo validando que coincidan de academia, que el cuatrimestre no
     * esté bloqueado, que el grupo no tenga ya una asignación activa y que cuente
     * con alumnos activos.
     * @param request petición HTTP con el parámetro "accion" y los datos de la asignación
     * @param response respuesta HTTP usada para redirigir con el resultado de la operación
     * @throws ServletException si ocurre un error al procesar la petición
     * @throws IOException si ocurre un error de entrada/salida al redirigir la respuesta
     */
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

    /**
     * Determina si el nombre de una carrera corresponde a la carrera exceptuada de
     * la restricción de cuatrimestre (comparación normalizada, sin acentos y en minúsculas).
     * @param nombreCarrera el nombre de la carrera a evaluar
     * @return {@code true} si la carrera es la exceptuada; {@code false} en caso contrario
     */
    private boolean esCarreraExceptuada(String nombreCarrera) {
        if (nombreCarrera == null) return false;
        String normalizado = Normalizer.normalize(nombreCarrera.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalizado.equals(CARRERA_EXCEPCION_CUATRIMESTRE);
    }

    /**
     * Determina si un grupo tiene un cuatrimestre restringido (6 o 10) para la
     * asignación de tutores, salvo que su carrera esté exceptuada.
     * @param grupo el grupo a evaluar
     * @return {@code true} si el cuatrimestre del grupo está bloqueado para asignación; {@code false} en caso contrario
     */
    private boolean esCuatrimestreBloqueado(Grupo grupo) {
        int cuatrimestre = grupo.getCuatrimestre();
        boolean esCuatrimestreRestringido = cuatrimestre == 6 || cuatrimestre == 10;
        return esCuatrimestreRestringido && !esCarreraExceptuada(grupo.getNombreCarrera());
    }
}
