<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%
    request.setAttribute("paginaActiva", "areas");

    Area areaEdit = (Area) request.getAttribute("areaEdit");
    boolean esEdicion = areaEdit != null;

    String tituloBanner = esEdicion ? "Editar área de apoyo" : "Nueva área de Apoyo";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - <%= tituloBanner %></title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            <%= tituloBanner %>
        </div>

        <form class="form-wrap-figma mt-3" action="<%= request.getContextPath() %>/AreaServlet" method="post">

            <input type="hidden" name="accion" value="<%= esEdicion ? "editar" : "nueva" %>">
            <% if (esEdicion) { %>
            <input type="hidden" name="idArea" value="<%= areaEdit.getIdArea() %>">
            <% } %>

            <div class="mb-4">
                <label for="nombreArea" class="form-label fs-6 fw-bold">Nombre Área</label>
                <input type="text" id="nombreArea" name="nombreArea" class="form-control form-control-figma fs-6"
                       value="<%= areaEdit != null ? areaEdit.getNombre() : "" %>" placeholder="Escribe nombre">
            </div>

            <div class="mb-4">
                <label for="encargado" class="form-label fs-6 fw-bold">Encargado</label>
                <input type="text" id="encargado" name="encargado" class="form-control form-control-figma fs-6"
                       value="<%= areaEdit != null ? areaEdit.getEncargado() : "" %>" placeholder="Escribe nombre del encargado">
            </div>

            <div class="mb-4">
                <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                <input type="email" id="correo" name="correo" class="form-control form-control-figma fs-6"
                       value="<%= areaEdit != null ? areaEdit.getCorreoContacto() : "" %>" placeholder="Escribe el correo del encargado">
            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <a href="<%= request.getContextPath() %>/coordinador/areas-apoyo.jsp" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2">Cancelar</a>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
</body>
</html>
