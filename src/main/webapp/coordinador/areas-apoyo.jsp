<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AreaDAO" %>
<%
    request.setAttribute("paginaActiva", "areas");

    AreaDAO areaDAO = new AreaDAO();
    List<Area> listaAreas = areaDAO.getAll();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Áreas de Apoyo</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/areas-apoyo.css" rel="stylesheet">
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
            Áreas de Apoyo
        </div>

        <div class="d-flex justify-content-end mb-3">
            <a href="<%= request.getContextPath() %>/coordinador/formulario-area.jsp" class="btn btn-utez-primary fw-medium px-3 py-1">Nueva área</a>
        </div>

        <!-- Tarjetas de areas de apoyo, pintadas dinamicamente desde AREA_APOYO -->
        <% if (listaAreas.isEmpty()) { %>
        <div class="alert alert-info text-center">
            No hay áreas de apoyo registradas todavía.
        </div>
        <% } else { %>
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">

            <% for (Area area : listaAreas) { %>
            <div class="col">
                <div class="card-area p-3 text-center d-flex flex-column">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/personas.png" alt="<%= area.getNombre() %>" class="icon-area mx-auto mb-2">
                    <p class="fs-6 fw-bold mb-1"><%= area.getNombre() %></p>
                    <p class="small text-muted mb-2">
                        <%= area.getEncargado() %><br>
                        <%= area.getCorreoContacto() %>
                    </p>
                    <div class="mt-auto d-flex flex-column gap-2">
                        <a href="<%= request.getContextPath() %>/AreaServlet?accion=prepararEdicion&idArea=<%= area.getIdArea() %>" class="btn btn-utez-primary fw-medium">Editar</a>
                        <button type="button" class="btn btn-utez-danger fw-medium" onclick="prepararEliminacionArea(<%= area.getIdArea() %>)">Eliminar</button>
                    </div>
                </div>
            </div>
            <% } %>

        </div>
        <% } %>

    </div>

</div>

<!-- ==================== ELIMINAR AREA (confirmacion via mostrarConfirmacion) ==================== -->
<form id="formEliminarArea" action="<%= request.getContextPath() %>/AreaServlet" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="idArea" id="inputEliminarIdArea">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/areas.js"></script>
</body>
</html>
