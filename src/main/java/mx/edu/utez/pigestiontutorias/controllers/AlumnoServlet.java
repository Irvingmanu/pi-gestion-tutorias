package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.pigestiontutorias.models.*;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;
import mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AlumnoServlet", value = "/gestion-grupos")
public class AlumnoServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";
    private static final String REGEX_MATRICULA = "^[a-zA-Z0-9]{10}$";

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final AsignacionTutorDao asignacionTutorDAO = new AsignacionTutorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("agenda".equals(accion)) {

            String matricula = request.getParameter("matricula");
            Alumno alumnoConsultado = alumnoDAO.getById(matricula);

            List<EventoAgenda> listaEventos = (alumnoConsultado != null)
                    ? alumnoDAO.getAgendaAlumno(alumnoConsultado.getMatricula(), alumnoConsultado.getIdCarrera(),
                    alumnoConsultado.getIdCuatrimestre(), alumnoConsultado.getIdLetraGrupo())
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
            request.setAttribute("listaGeneros", alumnoDAO.getAllGeneros());
            request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());
            request.setAttribute("listaCuatrimestres", alumnoDAO.getAllCuatrimestres());
            request.setAttribute("listaLetrasGrupo", alumnoDAO.getAllLetrasGrupo());

            if ("prepararEdicion".equals(accion)) {
                Alumno alumnoEdit = alumnoDAO.getById(request.getParameter("matricula"));
                request.setAttribute("alumnoEdit", alumnoEdit);
            }

            request.getRequestDispatcher("/coordinador/formulario-alumno.jsp").forward(request, response);
            return;
        }

        // ==================== LISTADO GENERAL (con filtros) ====================
        List<Genero> listaGeneros = alumnoDAO.getAllGeneros();
        List<Carrera> listaCarreras = alumnoDAO.getAllCarreras();
        List<Cuatrimestre> listaCuatrimestres = alumnoDAO.getAllCuatrimestres();
        List<LetraGrupo> listaLetrasGrupo = alumnoDAO.getAllLetrasGrupo();

        // Mapas de traduccion ID -> texto, usados por la tabla para mostrar
        // nombre de genero/carrera/cuatrimestre/grupo sin volver a consultar la BD por fila.
        Map<Integer, String> nombresGenero = new HashMap<>();
        for (Genero genero : listaGeneros) {
            nombresGenero.put(genero.getId(), genero.getNombre());
        }

        Map<Integer, String> nombresCarrera = new HashMap<>();
        for (Carrera carrera : listaCarreras) {
            nombresCarrera.put(carrera.getIdCarrera(), carrera.getNombre());
        }

        Map<Integer, Integer> numerosCuatrimestre = new HashMap<>();
        for (Cuatrimestre cuatrimestre : listaCuatrimestres) {
            numerosCuatrimestre.put(cuatrimestre.getIdCuatrimestre(), cuatrimestre.getNumero());
        }

        Map<Integer, String> nombresLetra = new HashMap<>();
        for (LetraGrupo letraGrupo : listaLetrasGrupo) {
            nombresLetra.put(letraGrupo.getIdLetra(), letraGrupo.getLetra());
        }

        // Tutor asignado por grupo (Carrera + Cuatrimestre + Letra), para mostrarlo junto
        // al titulo de cada tabla agrupada en gestion-grupos.jsp. La clave se arma igual que
        // en alumnos.js (carrera + '|' + cuatrimestre + '|' + letra) para poder cruzarlos ahi.
        Map<String, String> tutoresPorGrupo = new HashMap<>();
        for (AsignacionTutor asignacion : asignacionTutorDAO.getAll()) {
            String clave = asignacion.getNombreCarrera() + "|" + asignacion.getNumeroCuatrimestre() + "|" + asignacion.getLetraGrupo();
            tutoresPorGrupo.put(clave, asignacion.getNombresTutor() + " " + asignacion.getApellidosTutor());
        }

        request.setAttribute("listaAlumnos", alumnoDAO.getAll());
        request.setAttribute("listaCarreras", listaCarreras);
        request.setAttribute("listaCuatrimestres", listaCuatrimestres);
        request.setAttribute("listaLetrasGrupo", listaLetrasGrupo);
        request.setAttribute("nombresGenero", nombresGenero);
        request.setAttribute("nombresCarrera", nombresCarrera);
        request.setAttribute("numerosCuatrimestre", numerosCuatrimestre);
        request.setAttribute("nombresLetra", nombresLetra);
        request.setAttribute("tutoresPorGrupoJson", aJson(tutoresPorGrupo));

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
        alumno.setApellidos(request.getParameter("apellidos"));
        alumno.setCorreoInstitucional(request.getParameter("correo"));
        alumno.setTelefono(request.getParameter("telefono"));

        alumno.setIdGenero(Integer.parseInt(request.getParameter("idGenero")));
        alumno.setIdCarrera(Integer.parseInt(request.getParameter("idCarrera")));
        alumno.setIdCuatrimestre(Integer.parseInt(request.getParameter("idCuatrimestre")));
        alumno.setIdLetraGrupo(Integer.parseInt(request.getParameter("idLetraGrupo")));

        boolean formatoValido = alumno.getMatricula() != null && alumno.getMatricula().matches(REGEX_MATRICULA)
                && alumno.getNombres() != null && alumno.getNombres().matches(REGEX_NOMBRE)
                && alumno.getApellidos() != null && alumno.getApellidos().matches(REGEX_NOMBRE)
                && alumno.getTelefono() != null && alumno.getTelefono().matches(REGEX_TELEFONO)
                && alumno.getCorreoInstitucional() != null && alumno.getCorreoInstitucional().matches(REGEX_CORREO);

        if (!formatoValido) {
            request.setAttribute("error", "formato_invalido");
            request.setAttribute("alumno", alumno);
            request.setAttribute("listaGeneros", alumnoDAO.getAllGeneros());
            request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());
            request.setAttribute("listaCuatrimestres", alumnoDAO.getAllCuatrimestres());
            request.setAttribute("listaLetrasGrupo", alumnoDAO.getAllLetrasGrupo());
            request.getRequestDispatcher("/coordinador/formulario-alumno.jsp").forward(request, response);
            return;
        }

        boolean esEdicion = "editar".equals(accion);
        String errorDuplicado = null;

        if (esEdicion) {
            Alumno alumnoActual = alumnoDAO.getById(alumno.getMatricula());
            int idAlumnoActual = alumnoActual != null ? alumnoActual.getIdAlumno() : -1;
            alumno.setIdAlumno(idAlumnoActual);
            alumno.setIdUsuario(alumnoActual != null ? alumnoActual.getIdUsuario() : null);

            if (alumnoDAO.existeMatricula(alumno.getMatricula(), idAlumnoActual)) {
                errorDuplicado = "matricula_duplicada";
            } else if (alumnoDAO.existeCorreo(alumno.getCorreoInstitucional(), idAlumnoActual)) {
                errorDuplicado = "correo_duplicado";
            } else if (alumnoDAO.existeTelefono(alumno.getTelefono(), idAlumnoActual)) {
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
            request.setAttribute("error", errorDuplicado);
            request.setAttribute("alumno", alumno);
            request.setAttribute("listaGeneros", alumnoDAO.getAllGeneros());
            request.setAttribute("listaCarreras", alumnoDAO.getAllCarreras());
            request.setAttribute("listaCuatrimestres", alumnoDAO.getAllCuatrimestres());
            request.setAttribute("listaLetrasGrupo", alumnoDAO.getAllLetrasGrupo());
            request.getRequestDispatcher("/coordinador/formulario-alumno.jsp").forward(request, response);
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

    // Serializa un Map<String,String> a JSON simple para incrustarlo en un <script> del JSP;
    // no se usa una libreria porque el proyecto no trae ninguna y este es el unico caso.
    private static String aJson(Map<String, String> mapa) {
        StringBuilder json = new StringBuilder("{");
        boolean primero = true;
        for (Map.Entry<String, String> entrada : mapa.entrySet()) {
            if (!primero) json.append(",");
            primero = false;
            json.append(escaparJson(entrada.getKey())).append(":").append(escaparJson(entrada.getValue()));
        }
        json.append("}");
        return json.toString();
    }

    private static String escaparJson(String texto) {
        String seguro = texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + seguro + "\"";
    }
}