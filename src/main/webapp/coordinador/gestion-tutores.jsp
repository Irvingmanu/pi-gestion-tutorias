<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Tutor" %>
<%@ page import="java.util.Map" %>
<%
    String paginaActiva = "tutores";
    request.setAttribute("paginaActiva", paginaActiva);
    String ctx = request.getContextPath();
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

    <!-- BARRA LATERAL -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- CONTENIDO PRINCIPAL -->
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

        <!-- Tabla de tutores -->
        <div class="table-responsive mb-auto">
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
                    <th>Nómina</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Academia</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody id="tablaTutores">
                <%
                    List<Tutor> listaTutores = (List<Tutor>) request.getAttribute("listaTutores");
                    Map<Integer, String> nombresAcademia = (Map<Integer, String>) request.getAttribute("nombresAcademia");

                    if (listaTutores != null && !listaTutores.isEmpty()) {
                        for (Tutor tutor : listaTutores) {
                            String nombreAc = (nombresAcademia != null && nombresAcademia.containsKey(tutor.getIdAcademia()))
                                    ? nombresAcademia.get(tutor.getIdAcademia()) : "N/A";
                %>
                <tr data-nombre="<%= tutor.getNombres().toLowerCase() + " " + tutor.getApellidos().toLowerCase() %>">
                    <td><%= tutor.getNomina() %></td>
                    <td><%= tutor.getNombres() %> <%= tutor.getApellidos() %></td>
                    <td><%= tutor.getCorreoInstitucional() %></td>
                    <td><%= tutor.getTelefono() %></td>
                    <td><%= nombreAc %></td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="<%= ctx %>/TutoresServlet?accion=prepararEdicion&nomina=<%= tutor.getNomina() %>" class="btn-accion btn-editar">
                                <img src="<%= ctx %>/assets/img/coordinador/editar.png" width="16" alt="Editar">
                            </a>
                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('<%= tutor.getNomina() %>')">
                                <img src="<%= ctx %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                            </button>
                        </div>
                    </td>
                </tr>
                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="6" class="text-center py-4">No hay tutores registrados en el sistema.</td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>

    </div>

</div>

<form id="formEliminarTutor" action="<%= ctx %>/TutoresServlet" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="nomina" id="inputEliminarNomina">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= ctx %>/assets/js/bootstrap.js"></script>
<script src="<%= ctx %>/assets/js/alertas.js"></script>
<script src="<%= ctx %>/assets/js/coordinador/tutor.js"></script>
</body>
</html>