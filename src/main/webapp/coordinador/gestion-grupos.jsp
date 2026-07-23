<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Alumno" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Genero" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO" %>
<%
    request.setAttribute("paginaActiva", "grupos");

    List<Alumno> listaAlumnos = (List<Alumno>) request.getAttribute("listaAlumnos");
    if (listaAlumnos == null) {
        listaAlumnos = new ArrayList<>();
    }

    AlumnoDAO alumnoDAO = new AlumnoDAO();

    Map<Integer, String> nombresGenero = new HashMap<>();
    for (Genero genero : alumnoDAO.getAllGeneros()) {
        nombresGenero.put(genero.getId(), genero.getNombre());
    }

    Map<Integer, String> nombresCarrera = new HashMap<>();
    for (Carrera carrera : alumnoDAO.getAllCarreras()) {
        nombresCarrera.put(carrera.getIdCarrera(), carrera.getNombre());
    }

    Map<Integer, Integer> numerosCuatrimestre = new HashMap<>();
    for (Cuatrimestre cuatrimestre : alumnoDAO.getAllCuatrimestres()) {
        numerosCuatrimestre.put(cuatrimestre.getIdCuatrimestre(), cuatrimestre.getNumero());
    }

    Map<Integer, String> nombresLetra = new HashMap<>();
    for (LetraGrupo letraGrupo : alumnoDAO.getAllLetrasGrupo()) {
        nombresLetra.put(letraGrupo.getIdLetra(), letraGrupo.getLetra());
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Grupos</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Gestión de Grupos
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-5">
                <label class="campo-label fs-6" for="buscarAlumno">Buscar Alumno</label>
                <input type="text" id="buscarAlumno" class="campo-buscar"
                       placeholder="Buscar por nombre o apellido">
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" class="campo-select">
                    <option value="" selected>Seleccione la carrera</option>
                    <% for (Carrera carrera : alumnoDAO.getAllCarreras()) { %>
                    <option value="<%= carrera.getNombre() %>"><%= carrera.getNombre() %></option>
                    <% } %>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-3 align-items-end">
            <div class="col-md-5">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="campo-select">
                    <option value="" selected>Seleccione el Grupo</option>
                    <% for (LetraGrupo letraGrupo : alumnoDAO.getAllLetrasGrupo()) { %>
                    <option value="<%= letraGrupo.getLetra() %>"><%= letraGrupo.getLetra() %></option>
                    <% } %>
                </select>
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="campo-select">
                    <option value="" selected>Seleccione el cuatrimestre</option>
                    <% for (Cuatrimestre cuatrimestre : alumnoDAO.getAllCuatrimestres()) { %>
                    <option value="<%= cuatrimestre.getNumero() %>"><%= cuatrimestre.getNumero() %>&deg;</option>
                    <% } %>
                </select>
            </div>
            <div class="col-md-2 text-center">
                <label class="campo-label fs-6">Nuevo Alumno</label>
                <a href="<%= request.getContextPath() %>/AlumnoServlet?accion=nuevo" class="btn-figma text-decoration-none">Agregar</a>
            </div>
        </div>

        <div class="table-responsive mb-auto">
            <% if (listaAlumnos.isEmpty()) { %>
            <div class="alert alert-info text-center">
                No hay alumnos registrados todavía.
            </div>
            <% } else { %>
            <table class="tabla-grupos fs-6">
                <colgroup>
                    <col class="col-matricula">
                    <col class="col-nombre">
                    <col class="col-correo">
                    <col class="col-genero">
                    <col class="col-carrera">
                    <col class="col-carrera">
                    <col class="col-acciones">
                </colgroup>
                <thead>
                <tr>
                    <th>Matricula</th>
                    <th>Nombre Completo</th>
                    <th>Correo</th>
                    <th>Genero</th>
                    <th>Carrera</th>
                    <th>Cuatri/Grupo</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody id="tablaAlumnos">
                <% for (Alumno alumno : listaAlumnos) { %>
                <tr data-nombre="<%= alumno.getNombres().toLowerCase() %> <%= alumno.getApellidos().toLowerCase() %>"
                    data-carrera="<%= nombresCarrera.get(alumno.getIdCarrera()) %>"
                    data-cuatri="<%= numerosCuatrimestre.get(alumno.getIdCuatrimestre()) %>"
                    data-grupo="<%= nombresLetra.get(alumno.getIdLetraGrupo()) %>">
                    <td><%= alumno.getMatricula() %></td>
                    <td><%= alumno.getNombres() %> <%= alumno.getApellidos() %></td>
                    <td><%= alumno.getCorreoInstitucional() %></td>
                    <td><%= nombresGenero.get(alumno.getIdGenero()) %></td>
                    <td><%= nombresCarrera.get(alumno.getIdCarrera()) %></td>
                    <td><%= numerosCuatrimestre.get(alumno.getIdCuatrimestre()) %>&deg; <%= nombresLetra.get(alumno.getIdLetraGrupo()) %></td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="<%= request.getContextPath() %>/AlumnoServlet?accion=prepararEdicion&matricula=<%= alumno.getMatricula() %>" class="btn-accion btn-editar">
                                <img src="<%= request.getContextPath() %>/assets/img/coordinador/editar.png" width="16" alt="Editar">
                            </a>
                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('<%= alumno.getMatricula() %>')">
                                <img src="<%= request.getContextPath() %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                            </button>
                        </div>
                    </td>
                </tr>
                <% } %>
                <tr id="filaSinResultados" style="display: none;">
                    <td colspan="7" class="text-center">No se encontraron alumnos con los filtros seleccionados.</td>
                </tr>
                </tbody>
            </table>
            <% } %>
        </div>

    </div>

</div>

<form id="formEliminarAlumno" action="<%= request.getContextPath() %>/AlumnoServlet" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="matricula" id="inputEliminarMatricula">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/alumnos.js"></script>
</body>
</html>
