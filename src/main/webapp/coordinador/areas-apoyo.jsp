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
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="../assets/css/coordinador/areas-apoyo.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!--  BARRA LATERAL -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- CONTENIDO PRINCIPAL -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Áreas de Apoyo
        </div>

        <div class="d-flex justify-content-end mb-3">
            <a href="formulario-area.jsp?accion=nueva" class="btn btn-utez-primary fw-medium px-3 py-1">Nueva área</a>
        </div>

        <% if (listaAreas.isEmpty()) { %>
        <div class="alert alert-info text-center">
            No hay áreas de apoyo registradas todavía.
        </div>
        <% } else { %>
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">

            <% for (Area area : listaAreas) { %>
            <div class="col">
                <div class="card-area p-3 text-center d-flex flex-column">
                    <img src="../assets/img/coordinador/areas.png" alt="<%= area.getNombre() %>" class="icon-area mx-auto mb-2">
                    <p class="fs-6 fw-bold mb-1"><%= area.getNombre() %></p>
                    <p class="small text-muted mb-2">
                        <%= area.getEncargado() %><br>
                        <%= area.getCorreoContacto() %>
                    </p>
                    <div class="mt-auto d-flex flex-column gap-2">
                        <a href="<%= request.getContextPath() %>/AreaServlet?accion=prepararEdicion&idArea=<%= area.getIdArea() %>" class="btn btn-utez-primary fw-medium">Editar</a>
                        <button type="button" class="btn btn-utez-danger fw-medium" onclick="prepararEliminacion(<%= area.getIdArea() %>)">Eliminar</button>
                    </div>
                </div>
            </div>
            <% } %>

        </div>
        <% } %>

    </div>

</div>

<div class="modal fade" id="modalEliminar" tabindex="-1" aria-labelledby="modalEliminarLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form action="<%= request.getContextPath() %>/AreaServlet" method="POST">
                <input type="hidden" name="accion" value="eliminar">
                <input type="hidden" name="idArea" id="deleteId">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalEliminarLabel">Eliminar área de apoyo</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    ¿Estás seguro de que deseas eliminar esta área de apoyo?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-utez-danger fw-medium">Eliminar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="../assets/js/bootstrap.js"></script>
<script src="../assets/js/coordinador/areas.js"></script>
</body>
</html>
