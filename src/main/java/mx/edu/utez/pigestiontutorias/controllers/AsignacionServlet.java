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

// Un tutor solo se puede asignar a un GRUPO que ya existe (creado previamente al dar de
// alta a un alumno), nunca a uno inventado en el formulario: por eso "Nueva Asignacion"
// elige de la lista real de GrupoDao.getAll() en vez de armar Carrera+Cuatrimestre+Letra
// libres y resolver/crear el grupo via findOrCreate (eso permitia "asignar" tutores a
// combinaciones fantasma que nunca tuvieron un alumno).
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
        // Regla de negocio: los grupos de 6° y 10° cuatrimestre no llevan tutor asignado,
        // salvo en la carrera "Terapia Fisica" (unica excepcion). Se filtran aqui para que
        // ni siquiera aparezcan como opcion en el <select> de "Nueva Asignacion".
        List<Grupo> listaGrupos = grupoDao.getAll().stream()
                .filter(grupo -> !esCuatrimestreBloqueado(grupo))
                .collect(Collectors.toList());

        request.setAttribute("listaTutores", listaTutores);
        request.setAttribute("listaAsignaciones", listaAsignaciones);
        request.setAttribute("listaGrupos", listaGrupos);
        // Alimenta filtroAcademiaTabla (tab "Asignaciones Actuales") y academiaFormulario
        // (tab "Nueva Asignación") en asignacion.jsp.
        request.setAttribute("listaAcademias", academiaDao.getAll());

        request.getRequestDispatcher("/coordinador/asignacion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            int idAsignacion = Integer.parseInt(request.getParameter("id_asignacion"));
            boolean eliminado = asignacionTutorDao.delete(idAsignacion);
            String parametro = eliminado ? "exito=eliminado" : "error=true";
            response.sendRedirect(request.getContextPath() + "/asignacion?" + parametro);
            return;
        }

        int idTutor = Integer.parseInt(request.getParameter("id_tutor"));
        int idGrupo = Integer.parseInt(request.getParameter("id_grupo"));

        // Blindaje de servidor: el grupo enviado debe ser uno que realmente existe en BD
        // (y sigue activo), nunca una combinacion armada a mano en el request.
        Grupo grupo = grupoDao.getById(idGrupo);
        if (grupo == null || !"S".equals(grupo.getEstado())) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=true");
            return;
        }

        // Blindaje de servidor: un tutor solo puede asignarse a grupos de su propia
        // academia (ej. un tutor de DATIT no puede recibir un grupo de otra academia),
        // sin confiar en que el filtro del <select> del formulario no fue manipulado.
        Tutor tutor = tutorDao.getById(idTutor);
        if (tutor == null || tutor.getIdAcademia() != grupo.getIdAcademia()) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=academia_no_coincide");
            return;
        }

        // Blindaje de servidor: los grupos de 6° y 10° cuatrimestre no se asignan a un
        // tutor, salvo en la carrera "Terapia Fisica" (unica excepcion), sin confiar en que
        // el <select> del formulario no fue manipulado (esos grupos ya vienen ocultos ahi).
        if (esCuatrimestreBloqueado(grupo)) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=cuatrimestre_no_permitido");
            return;
        }

        if (asignacionTutorDao.existeAsignacionActiva(idGrupo)) {
            response.sendRedirect(request.getContextPath() + "/asignacion?error=grupo_asignado");
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

    // TSU: cuatrimestres 1-6. Ingenieria (ING): cuatrimestres 7-10.
    private boolean esCuatrimestreValidoParaNivel(int cuatrimestre, String nivel) {
        if ("TSU".equals(nivel)) {
            return cuatrimestre >= 1 && cuatrimestre <= 6;
        }
        if ("ING".equals(nivel)) {
            return cuatrimestre >= 7 && cuatrimestre <= 10;
        }
        return false;
    }

    // Unica carrera exceptuada de la regla de abajo: sus grupos de 6° y 10° si pueden
    // llevar tutor asignado. Comparacion sin acentos/mayusculas para no depender de como
    // este capturado el nombre exacto en el catalogo CARRERA (poblado a mano via SQL).
    private static final String CARRERA_EXCEPCION_CUATRIMESTRE = "terapia fisica";

    private boolean esCarreraExceptuada(String nombreCarrera) {
        if (nombreCarrera == null) return false;
        String normalizado = Normalizer.normalize(nombreCarrera.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalizado.equals(CARRERA_EXCEPCION_CUATRIMESTRE);
    }

    // Regla de negocio: un grupo de 6° o 10° cuatrimestre no puede tener tutor asignado,
    // salvo que su carrera sea la excepcion (Terapia Fisica). La usan tanto el <select> de
    // "Nueva Asignacion" (doGet, para que esos grupos ni aparezcan) como el guardado (doPost,
    // como blindaje de servidor).
    private boolean esCuatrimestreBloqueado(Grupo grupo) {
        int cuatrimestre = grupo.getCuatrimestre();
        boolean esCuatrimestreRestringido = cuatrimestre == 6 || cuatrimestre == 10;
        return esCuatrimestreRestringido && !esCarreraExceptuada(grupo.getNombreCarrera());
    }
}
