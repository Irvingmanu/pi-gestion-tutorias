<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    request.setAttribute("paginaActiva", "areas");

    Area areaEdit = (Area) request.getAttribute("areaEdit");
    boolean esEdicion = areaEdit != null;
    request.setAttribute("esEdicion", esEdicion);

    String tituloBanner = esEdicion ? "Editar área de apoyo" : "Nueva área de Apoyo";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - <%= tituloBanner %></title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            <%= tituloBanner %>
        </div>

        <!-- Formulario de nueva/edicion de area -->
        <form class="form-wrap-figma mt-3" style="max-width: 900px;" action="<%= request.getContextPath() %>/AreaServlet" method="post">

            <input type="hidden" name="accion" value="<%= esEdicion ? "editar" : "nueva" %>">
            <% if (esEdicion) { %>
            <input type="hidden" name="idArea" value="<%= areaEdit.getIdArea() %>">
            <% } %>

            <div class="row g-4">

                <!-- Columna izquierda: datos del area -->
                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="nombreArea" class="form-label fs-6 fw-bold">Nombre Área</label>
                        <input type="text" id="nombreArea" name="nombreArea" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaEdit != null ? areaEdit.getNombre() : "" %>" placeholder="Escribe nombre" required>
                    </div>

                    <div class="mb-4">
                        <label for="encargado" class="form-label fs-6 fw-bold">Encargado</label>
                        <input type="text" id="encargado" name="encargado" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaEdit != null ? areaEdit.getEncargado() : "" %>" placeholder="Escribe nombre del encargado" required>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaEdit != null ? areaEdit.getCorreoContacto() : "" %>" placeholder="Escribe el correo del encargado" required>
                    </div>

                </div>

                <!-- Columna derecha: sub-categorias (filas dinamicas, precargadas via JSTL en edicion) -->
                <div class="col-md-6">

                    <label class="form-label fs-6 fw-bold">Sub-Categorías</label>

                    <div id="subcategoriasContainer">
                        <c:choose>
                            <c:when test="${esEdicion and not empty areaEdit.motivos}">
                                <c:forEach items="${areaEdit.motivos}" var="motivo" varStatus="status">
                                    <div class="d-flex gap-2 mb-3 subcategoria-row">
                                        <input type="text" name="motivos[]" value="${motivo.nombreMotivo}"
                                               class="form-control form-control-figma w-100 fs-6"
                                               placeholder="Escribe la subcategoria" required>
                                        <c:choose>
                                            <c:when test="${status.first}">
                                                <button type="button" class="btn-figma btn-figma-sm flex-shrink-0"
                                                        onclick="agregarSubcategoria()">+</button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0 btn-eliminar-subcategoria">-</button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="d-flex gap-2 mb-3 subcategoria-row">
                                    <input type="text" name="motivos[]" class="form-control form-control-figma w-100 fs-6"
                                           placeholder="Escribe la subcategoria" required>
                                    <button type="button" class="btn-figma btn-figma-sm flex-shrink-0"
                                            onclick="agregarSubcategoria()">+</button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <a href="<%= request.getContextPath() %>/coordinador/areas-apoyo.jsp" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2">Cancelar</a>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/subcategorias.js"></script>
</body>
</html>
