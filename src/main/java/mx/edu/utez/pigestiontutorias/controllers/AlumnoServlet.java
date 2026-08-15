package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Carrera+Cuatrimestre+Letra ya no se guardan como 3 FKs sueltas en ALUMNO: el formulario
// las captura (select en cascada: Carrera -> Cuatrimestre segun NIVEL, Letra fija A-F) y
// el servlet resuelve/crea el GRUPO real correspondiente via GrupoDao, guardando solo
// ID_GRUPO en ALUMNO.
@WebServlet(name = "AlumnoServlet", value = "/gestion-grupos")
public class AlumnoServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_MATRICULA = "^[a-zA-Z0-9]{10}$";

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final AsignacionTutorDao asignacionTutorDAO = new AsignacionTutorDao();
    private final GrupoDao grupoDao = new GrupoDao();
    private final PeriodoEscolarDao periodoEscolarDao = new PeriodoEscolarDao();
    private final AcademiaDao academiaDao = new AcademiaDao();
    private final CarreraDao carreraDao = new CarreraDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("agenda".equals(accion)) {

            String matricula = request.getParameter("matricula");
            Alumno alumnoConsultado = alumnoDAO.getById(matricula);

            List<EventoAgenda> listaEventos = (alumnoConsultado != null && alumnoConsultado.getIdGrupo() != null)
                    ? alumnoDAO.getAgendaAlumno(alumnoConsultado.getMatricula(), alumnoConsultado.getIdGrupo())
                    : java.util.Collections.emptyList();

            request.setAttribute("listaEventosAgenda", listaEventos);
            request.getRequestDispatcher("/alumno/agenda.jsp").forward(request, response);
            return;
        }

        if ("eliminar".equals(accion)) {
            boolean eliminado = alumnoDAO.delete(request.getParameter("matricula"));
            String parametro = eliminado ? "exito=eliminado" : "error=alumno_en_uso";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("reactivar".equals(accion)) {
            boolean reactivado = alumnoDAO.reactivar(request.getParameter("matricula"));
            String parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("nuevo".equals(accion) || "prepararEdicion".equals(accion)) {
            Alumno alumnoEdit = "prepararEdicion".equals(accion)
                    ? alumnoDAO.getPerfilCompleto(request.getParameter("matricula"))
                    : null;
            forwardAFormulario(request, response, alumnoEdit, null, null);
            return;
        }

        // ==================== LISTADO GENERAL (con filtros) ====================
        List<Genero> listaGeneros = alumnoDAO.getAllGeneros();

        // Mapa de traduccion ID -> nombre de genero, usado por la tabla para mostrar
        // el nombre sin volver a consultar la BD por fila.
        Map<Integer, String> nombresGenero = new HashMap<>();
        for (Genero genero : listaGeneros) {
            nombresGenero.put(genero.getId(), genero.getNombre());
        }

        // Tutor asignado por grupo, para mostrarlo junto al titulo de cada tabla agrupada
        // en gestion-grupos.jsp. La clave es el ID_GRUPO directo (ya no hay que armarla
        // a mano con carrera+cuatrimestre+letra).
        Map<Integer, String> tutoresPorGrupo = new HashMap<>();
        for (AsignacionTutor asignacion : asignacionTutorDAO.getAll()) {
            tutoresPorGrupo.put(asignacion.getIdGrupo(), asignacion.getNombresTutor() + " " + asignacion.getApellidosTutor());
        }

        // Grupo (Carrera + Cuatrimestre + Letra) de cada alumno, indexado por ID_GRUPO:
        // el JSP lo usa para pintar la columna Carrera/Cuatri/Grupo y agrupar la tabla
        // sin tener que resolver el perfil completo de cada alumno.
        List<Grupo> listaGrupos = grupoDao.getAll();
        Map<Integer, Grupo> gruposPorId = new HashMap<>();
        for (Grupo grupo : listaGrupos) {
            gruposPorId.put(grupo.getIdGrupo(), grupo);
        }

        request.setAttribute("listaAlumnos", alumnoDAO.getAll());
        request.setAttribute("listaGrupos", listaGrupos);
        request.setAttribute("gruposPorId", gruposPorId);
        request.setAttribute("nombresGenero", nombresGenero);
        request.setAttribute("tutoresPorGrupo", tutoresPorGrupo);
        // Filtro Academia -> Carrera del listado (#academiaFiltroPrincipal / #carreraFiltroPrincipal):
        // listaCarreras trae TODAS las carreras del sistema, no solo las que ya tienen grupos.
        request.setAttribute("listaAcademias", academiaDao.getAll());
        request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());

        request.getRequestDispatcher("/coordinador/gestion-grupos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            boolean eliminado = alumnoDAO.delete(request.getParameter("matricula"));
            String parametro = eliminado ? "exito=eliminado" : "error=alumno_en_uso";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        if ("reactivar".equals(accion)) {
            boolean reactivado = alumnoDAO.reactivar(request.getParameter("matricula"));
            String parametro = reactivado ? "exito=reactivado" : "error=reactivacion_fallida";
            response.sendRedirect(request.getContextPath() + "/gestion-grupos?" + parametro);
            return;
        }

        String matricula = request.getParameter("matricula");

        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula != null ? matricula.trim().toUpperCase() : null);
        alumno.setNombres(request.getParameter("nombres"));
        alumno.setApellidoPaterno(request.getParameter("apellidoPaterno"));
        alumno.setApellidoMaterno(request.getParameter("apellidoMaterno"));
        alumno.setCorreoInstitucional(request.getParameter("correo"));
        alumno.setTelefono(request.getParameter("telefono"));
        alumno.setIdGenero(parseIntOrNull(request.getParameter("idGenero")));

        boolean formatoValido = alumno.getMatricula() != null && alumno.getMatricula().matches(REGEX_MATRICULA)
                && alumno.getNombres() != null && alumno.getNombres().matches(REGEX_NOMBRE)
                && alumno.getApellidoPaterno() != null && alumno.getApellidoPaterno().matches(REGEX_NOMBRE)
                && (alumno.getApellidoMaterno() == null || alumno.getApellidoMaterno().isBlank() || alumno.getApellidoMaterno().matches(REGEX_NOMBRE))
                && alumno.getTelefono() != null && alumno.getTelefono().matches(REGEX_TELEFONO)
                && alumno.getCorreoInstitucional() != null && alumno.getCorreoInstitucional().matches(REGEX_CORREO)
                && alumno.getIdGenero() != null;

        if (!formatoValido) {
            forwardAFormulario(request, response, null, alumno, "formato_invalido");
            return;
        }

        // Resolucion del grupo: Carrera (select) + Cuatrimestre (select en cascada segun
        // el NIVEL de la carrera) + Letra (fija A-F en el HTML). Se valida por separado
        // (con su propio codigo de error) para no confundirlo con un error de formato
        // en los datos personales: son fallas de naturaleza distinta.
        Integer idCarrera = parseIntOrNull(request.getParameter("idCarrera"));
        Integer cuatrimestre = parseIntOrNull(request.getParameter("cuatrimestre"));
        String letra = request.getParameter("letra");

        Carrera carrera = idCarrera != null ? carreraDao.getById(idCarrera) : null;
        boolean grupoValido = carrera != null && cuatrimestre != null && letra != null && !letra.isBlank()
                && esCuatrimestreValidoParaNivel(cuatrimestre, carrera.getNivel());

        if (!grupoValido) {
            forwardAFormulario(request, response, null, alumno, "grupo_invalido");
            return;
        }

        PeriodoEscolar periodoVigente = periodoEscolarDao.getPeriodoVigente();
        if (periodoVigente == null) {
            forwardAFormulario(request, response, null, alumno, "sin_periodo_vigente");
            return;
        }

        Integer idGrupo = grupoDao.findOrCreate(idCarrera, cuatrimestre, letra.trim(), periodoVigente.getIdPeriodo());
        if (idGrupo == null) {
            forwardAFormulario(request, response, null, alumno, "grupo_invalido");
            return;
        }
        alumno.setIdGrupo(idGrupo);

        boolean esEdicion = "editar".equals(accion);
        String errorDuplicado = null;

        if (esEdicion) {
            if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional(), alumno.getMatricula())) {
                errorDuplicado = "correo_duplicado";
            } else if (alumnoDAO.existeTelefono(alumno.getTelefono(), alumno.getMatricula())) {
                errorDuplicado = "telefono_duplicado";
            }
        } else {
            if (alumnoDAO.existeMatricula(alumno.getMatricula())) {
                errorDuplicado = "matricula_duplicada";
            } else if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional())) {
                errorDuplicado = "correo_duplicado";
            } else if (alumnoDAO.existeTelefono(alumno.getTelefono())) {
                errorDuplicado = "telefono_duplicado";
            }
        }

        if (errorDuplicado != null) {
            forwardAFormulario(request, response, null, alumno, errorDuplicado);
            return;
        }

        String parametroExito;
        if (esEdicion) {
            alumnoDAO.update(alumno);
            parametroExito = "editado";
        } else {
            alumnoDAO.create(alumno);
            parametroExito = "guardado";
        }

        response.sendRedirect(request.getContextPath() + "/gestion-grupos?exito=" + parametroExito);
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

    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Centraliza lo que antes calculaba el scriptlet de arriba en formulario-alumno.jsp
    // (alumnoFormulario, esEdicion, tituloBanner, mensajeError): esa logica no le
    // corresponde a la vista, vive aqui junto con el resto de las reglas del formulario.
    private void forwardAFormulario(HttpServletRequest request, HttpServletResponse response,
                                     Alumno alumnoEdit, Alumno alumnoConError, String codigoError)
            throws ServletException, IOException {
        Alumno alumnoFormulario = alumnoEdit != null ? alumnoEdit : alumnoConError;
        boolean esEdicion = alumnoEdit != null || "editar".equals(request.getParameter("accion"));

        request.setAttribute("alumnoFormulario", alumnoFormulario);
        request.setAttribute("esEdicion", esEdicion);
        request.setAttribute("tituloBanner", esEdicion ? "Editar Alumno" : "Nuevo Alumno");
        request.setAttribute("mensajeError", resolverMensajeError(codigoError));
        request.setAttribute("listaGeneros", alumnoDAO.getAllGeneros());
        request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());
        request.setAttribute("listaAcademias", academiaDao.getAll());

        request.getRequestDispatcher("/coordinador/formulario-alumno.jsp").forward(request, response);
    }

    private String resolverMensajeError(String codigoError) {
        if ("matricula_duplicada".equals(codigoError)) {
            return "Esta matrícula ya está registrada en el sistema.";
        }
        if ("correo_duplicado".equals(codigoError)) {
            return "Este correo ya está registrado en el sistema.";
        }
        if ("telefono_duplicado".equals(codigoError)) {
            return "Este número de teléfono ya está registrado en el sistema.";
        }
        if ("formato_invalido".equals(codigoError)) {
            return "Verifica los datos. El formato de uno o más campos es incorrecto.";
        }
        if ("grupo_invalido".equals(codigoError)) {
            return "Selecciona una Carrera, Cuatrimestre y Grupo válidos. El cuatrimestre debe corresponder al nivel de la carrera elegida (TSU: 1° a 6°, Ingeniería: 7° a 10°).";
        }
        if ("sin_periodo_vigente".equals(codigoError)) {
            return "No hay un periodo escolar vigente que incluya la fecha de hoy. Ve a Periodos Escolares y crea uno antes de registrar alumnos.";
        }
        return null;
    }

}
