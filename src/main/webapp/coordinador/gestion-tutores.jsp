<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Tutor" %>
<%
    String paginaActiva = "tutores";
    request.setAttribute("paginaActiva", paginaActiva);
    String ctx = request.getContextPath();

    List<Tutor> listaTutores = (List<Tutor>) request.getAttribute("listaTutores");
    if (listaTutores == null) {
        listaTutores = new ArrayList<>();
    }

    Map<Integer, String> nombresAcademia = (Map<Integer, String>) request.getAttribute("nombresAcademia");
    if (nombresAcademia == null) {
        nombresAcademia = new HashMap<>();
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Tutores</title>
    <link href="<%= ctx %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/coordinador/gestion-tutores.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/alertas.css" rel="stylesheet">
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

        <!-- Buscar tutor / Nuevo Tutor -->
        <div class="row mb-3">
            <div class="col-12 d-flex justify-content-between align-items-end">
                <div>
                    <label class="campo-label fs-6" for="buscarTutor">Buscar tutor</label>
                    <input type="text" id="buscarTutor" class="campo-buscar campo-buscar-tutor"
                           placeholder="Buscar por nombre">
                </div>
                <div class="text-center">
                    <label class="campo-label fs-6">Nuevo Tutor</label>
                    <a href="<%= ctx %>/TutoresServlet?accion=nuevo" class="btn-figma">Agregar</a>
                </div>
            </div>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-6 d-flex align-items-center gap-2">
                <input type="checkbox" id="mostrarInactivos" class="form-check-input">
                <label class="form-check-label fs-6" for="mostrarInactivos">Mostrar tutores dados de baja</label>
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
                <% for (Tutor tutor : listaTutores) {
                    boolean tutorActivo = !"N".equals(tutor.getActivo());
                %>
                <tr class="<%= tutorActivo ? "" : "fila-inactiva" %>"
                    data-nombre="<%= tutor.getNombres().toLowerCase() %> <%= tutor.getApellidos().toLowerCase() %>"
                    data-activo="<%= tutorActivo ? "S" : "N" %>">
                    <td><%= tutor.getNomina() %></td>
                    <td><%= tutor.getNombres() %> <%= tutor.getApellidos() %><% if (!tutorActivo) { %> <span class="badge-inactivo">(Baja)</span><% } %></td>
                    <td><%= tutor.getCorreoInstitucional() %></td>
                    <td><%= tutor.getTelefono() %></td>
                    <td><%= nombresAcademia.get(tutor.getIdAcademia()) %></td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <% if (tutorActivo) { %>
                            <a href="<%= ctx %>/TutoresServlet?accion=prepararEdicion&nomina=<%= tutor.getNomina() %>" class="btn-accion btn-editar">
                                <img src="<%= ctx %>/assets/img/coordinador/editar.png" width="16" alt="Editar">
                            </a>
                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('<%= tutor.getNomina() %>')">
                                <img src="<%= ctx %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                            </button>
                            <% } else { %>
                            <button type="button" class="btn-accion btn-reactivar" onclick="prepararReactivacion('<%= tutor.getNomina() %>')">
                                <img src="<%= ctx %>/assets/img/coordinador/reactivar.png" width="16" alt="Reactivar">
                            </button>
                            <% } %>
                        </div>
                    </td>
                </tr>
                <% } %>
                <tr id="filaSinResultados" style="display: none;">
                    <td colspan="6" class="text-center">No se encontraron tutores con los filtros seleccionados.</td>
                </tr>
                </tbody>
            </table>
            <% } %>
        </div>

    </div>

</div>

<form id="formEliminarTutor" action="<%= ctx %>/TutoresServlet" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="nomina" id="inputEliminarNomina">
</form>

<form id="formReactivarTutor" action="<%= ctx %>/TutoresServlet" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="reactivar">
    <input type="hidden" name="nomina" id="inputReactivarNomina">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= ctx %>/assets/js/bootstrap.js"></script>
<script src="<%= ctx %>/assets/js/alertas.js"></script>
<script src="<%= ctx %>/assets/js/coordinador/tutor.js"></script>
</body>
</html>
