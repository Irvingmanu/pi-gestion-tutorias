<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Tutor" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Academia" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.TutorDao" %>
<%
    request.setAttribute("paginaActiva", "tutores");

    List<Tutor> listaTutores = (List<Tutor>) request.getAttribute("listaTutores");
    if (listaTutores == null) {
        listaTutores = new ArrayList<>();
    }

    TutorDao tutorDao = new TutorDao();

    Map<Integer, String> nombresAcademia = new HashMap<>();
    for (Academia academia : tutorDao.getAllAcademias()) {
        nombresAcademia.put(academia.getIdAcademia(), academia.getNombre());
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Tutores</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-tutores.css" rel="stylesheet">
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
            Gestión de Tutores
        </div>

        <!-- Controles de búsqueda y botón nuevo -->
        <div class="row mb-3">
            <div class="col-12 d-flex justify-content-between align-items-end">
                <div>
                    <label class="campo-label fs-6" for="buscarTutor">Buscar tutor</label>
                    <input type="text" id="buscarTutor" class="campo-buscar campo-buscar-tutor"
                           placeholder="Buscar por nombre">
                </div>
                <div class="text-center">
                    <label class="campo-label fs-6">Nuevo Tutor</label>
                    <a href="<%= request.getContextPath() %>/TutoresServlet?accion=nuevo" class="btn-figma text-decoration-none">Agregar</a>
                </div>
            </div>
        </div>

        <!-- Tabla de tutores -->
        <div class="table-responsive mb-auto">
            <% if (listaTutores.isEmpty()) { %>
            <div class="alert alert-info text-center">
                No hay tutores registrados todavía.
            </div>
            <% } else { %>
            <table class="tabla-grupos fs-6">
                <colgroup>
                    <col class="col-nomina">
                    <col class="col-nombre-t">
                    <col class="col-correo-t">
                    <col class="col-telefono">
                    <col class="col-academia">
                    <col class="col-acciones-t">
                </colgroup>
                <thead>
                <tr>
                    <th>Nomina</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Academia</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody id="tablaTutores">
                <% for (Tutor tutor : listaTutores) { %>
                <tr data-nombre="<%= tutor.getNombres().toLowerCase() %> <%= tutor.getApellidos().toLowerCase() %>">
                    <td><%= tutor.getNomina() %></td>
                    <td><%= tutor.getNombres() %> <%= tutor.getApellidos() %></td>
                    <td><%= tutor.getCorreoInstitucional() %></td>
                    <td><%= tutor.getTelefono() %></td>
                    <td><%= nombresAcademia.get(tutor.getIdAcademia()) %></td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="<%= request.getContextPath() %>/TutoresServlet?accion=prepararEdicion&nomina=<%= tutor.getNomina() %>" class="btn-accion btn-editar">
                                <img src="<%= request.getContextPath() %>/assets/img/coordinador/editar.png" width="16" alt="Editar">
                            </a>
                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('<%= tutor.getNomina() %>')">
                                <img src="<%= request.getContextPath() %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                            </button>
                        </div>
                    </td>
                </tr>
                <% } %>
                <tr id="filaSinResultados" style="display: none;">
                    <td colspan="6" class="text-center">No se encontraron tutores con ese nombre.</td>
                </tr>
                </tbody>
            </table>
            <% } %>
        </div>


    </div>

</div>

<form id="formEliminarTutor" action="<%= request.getContextPath() %>/TutoresServlet" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" id="inputEliminarNomina" name="nomina" value="">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/tutor.js"></script>
</body>
</html>
