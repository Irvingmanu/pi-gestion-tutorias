package mx.edu.utez.pigestiontutorias.controllers;

import mx.edu.utez.pigestiontutorias.models.Alumno;
import mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/AlumnoServlet")
public class AlumnoServlet extends HttpServlet {

    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^\\d{10}$";
    private static final String REGEX_CORREO = "^[a-zA-Z0-9._-]+@utez\\.edu\\.mx$";

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            alumnoDAO.delete(request.getParameter("matricula"));
            response.sendRedirect(request.getContextPath() + "/AlumnoServlet?exito=eliminado");
            return;
        }

        if ("nuevo".equals(accion) || "prepararEdicion".equals(accion)) {
            // Mandamos los 4 catálogos a la vista
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

        request.setAttribute("listaAlumnos", alumnoDAO.getAll());
        request.getRequestDispatcher("/coordinador/gestion-grupos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            alumnoDAO.delete(request.getParameter("matricula"));
            response.sendRedirect(request.getContextPath() + "/AlumnoServlet?exito=eliminado");
            return;
        }

        Alumno alumno = new Alumno();
        alumno.setMatricula(request.getParameter("matricula"));
        alumno.setNombres(request.getParameter("nombres"));
        alumno.setApellidos(request.getParameter("apellidos"));
        alumno.setCorreoInstitucional(request.getParameter("correo"));
        alumno.setTelefono(request.getParameter("telefono"));

        // Recibimos los 4 IDs del formulario
        alumno.setIdGenero(Integer.parseInt(request.getParameter("idGenero")));
        alumno.setIdCarrera(Integer.parseInt(request.getParameter("idCarrera")));
        alumno.setIdCuatrimestre(Integer.parseInt(request.getParameter("idCuatrimestre")));
        alumno.setIdLetraGrupo(Integer.parseInt(request.getParameter("idLetraGrupo")));

        boolean formatoValido = alumno.getNombres() != null && alumno.getNombres().matches(REGEX_NOMBRE)
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

        response.sendRedirect(request.getContextPath() + "/AlumnoServlet?exito=" + parametroExito);
    }
}