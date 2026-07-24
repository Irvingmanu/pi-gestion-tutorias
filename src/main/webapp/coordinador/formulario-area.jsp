<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    request.setAttribute("paginaActiva", "areas");

    Area areaEdit = (Area) request.getAttribute("areaEdit");
    Area areaConError = (Area) request.getAttribute("area");
    boolean esEdicion = areaEdit != null;
    request.setAttribute("esEdicion", esEdicion);

    String tituloBanner = esEdicion ? "Editar área de apoyo" : "Nueva área de Apoyo";

    String codigoError = (String) request.getAttribute("error");
    String mensajeError = null;
    if ("formato_invalido".equals(codigoError)) {
        mensajeError = "Verifica los datos. El formato de uno o más campos es incorrecto.";
    } else if ("nombre_duplicado".equals(codigoError)) {
        mensajeError = "Ya existe un área de apoyo registrada con ese nombre.";
    }
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
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
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

        <% if (!esEdicion) { %>
        <!-- ==================== MODO NUEVA AREA: un solo form ==================== -->
        <form class="form-wrap-figma mt-3" style="max-width: 900px;" action="<%= request.getContextPath() %>/AreaServlet"
              method="post" onsubmit="return confirmarGuardarArea(event)">

            <input type="hidden" name="accion" value="guardarArea">

            <div class="row g-4">

                <!-- Columna izquierda: datos del area -->
                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="nombreArea" class="form-label fs-6 fw-bold">Nombre Área</label>
                        <input type="text" id="nombreArea" name="nombreArea" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaConError != null ? areaConError.getNombre() : "" %>" placeholder="Escribe nombre"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>
                    </div>

                    <div class="mb-4">
                        <label for="encargado" class="form-label fs-6 fw-bold">Encargado</label>
                        <input type="text" id="encargado" name="encargado" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaConError != null ? areaConError.getEncargado() : "" %>" placeholder="Escribe nombre del encargado"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaConError != null ? areaConError.getCorreoContacto() : "" %>" placeholder="Escribe el correo del encargado"
                               pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$" title="El correo debe terminar en @utez.edu.mx" required>
                    </div>

                </div>

                <!-- Columna derecha: motivos dinamicos, se insertan junto con el area -->
                <div class="col-md-6">

                    <label class="form-label fs-6 fw-bold">Motivos de Canalización</label>

                    <div id="motivosContainer">
                        <div class="d-flex gap-2 mb-3 motivo-row">
                            <input type="text" name="motivos[]" class="form-control form-control-figma w-100 fs-6"
                                   placeholder="Escribe el motivo de atención"
                                   pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>
                            <button type="button" class="btn-figma btn-figma-sm flex-shrink-0"
                                    onclick="agregarMotivo()">+</button>
                        </div>
                    </div>

                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <button type="button" id="btnCancelarFormularioArea" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="<%= request.getContextPath() %>/coordinador/areas-apoyo.jsp"
                        onclick="confirmarCancelacionArea()">Cancelar</button>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

        <% } else { %>
        <!-- ==================== MODO EDICION: maestro-detalle en dos columnas ==================== -->
        <div class="row g-4 mt-3 mx-auto" style="max-width: 900px;">

            <!-- Columna izquierda: form del AREA (maestro), solo actualiza sus datos -->
            <div class="col-md-6">
                <form id="formEditarArea" class="form-wrap-figma" action="<%= request.getContextPath() %>/AreaServlet"
                      method="post" onsubmit="return confirmarGuardarArea(event)">

                    <input type="hidden" name="accion" value="guardarArea">
                    <input type="hidden" name="idArea" value="<%= areaEdit.getIdArea() %>">

                    <div class="mb-4">
                        <label for="nombreArea" class="form-label fs-6 fw-bold">Nombre Área</label>
                        <input type="text" id="nombreArea" name="nombreArea" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaEdit.getNombre() %>" placeholder="Escribe nombre"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>
                    </div>

                    <div class="mb-4">
                        <label for="encargado" class="form-label fs-6 fw-bold">Encargado</label>
                        <input type="text" id="encargado" name="encargado" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaEdit.getEncargado() %>" placeholder="Escribe nombre del encargado"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= areaEdit.getCorreoContacto() %>" placeholder="Escribe el correo del encargado"
                               pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$" title="El correo debe terminar en @utez.edu.mx" required>
                    </div>

                </form>
            </div>

            <!-- Columna derecha: detalle de MOTIVOS, alta y baja individual (sin JS/AJAX) -->
            <div class="col-md-6">

                <label class="form-label fs-6 fw-bold">Motivos de Canalización</label>

                <!-- Alta: form independiente que agrega un solo motivo a esta area -->
                <form class="d-flex gap-2 mb-3" action="<%= request.getContextPath() %>/AreaServlet" method="post">
                    <input type="hidden" name="accion" value="agregarMotivo">
                    <input type="hidden" name="idArea" value="<%= areaEdit.getIdArea() %>">
                    <input type="text" name="nuevoMotivo" class="form-control form-control-figma w-100 fs-6"
                           placeholder="Escribe el motivo de atención"
                           pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>
                    <button type="submit" class="btn-figma btn-figma-sm flex-shrink-0">+</button>
                </form>

                <!-- Baja: tabla con boton por fila que dispara prepararEliminacionMotivo() -->
                <div class="border shadow-sm" style="border-radius: 20px; overflow: hidden;">
                    <table class="table align-middle mb-0">
                        <tbody>
                        <c:forEach items="${areaEdit.motivos}" var="motivo">
                            <tr>
                                <td>
                                    <span id="lbl-motivo-${motivo.idMotivo}"><c:out value="${motivo.nombreMotivo}"/></span>

                                    <form id="form-edit-${motivo.idMotivo}" class="d-none d-flex gap-2"
                                          action="<%= request.getContextPath() %>/AreaServlet" method="post">
                                        <input type="hidden" name="accion" value="editarMotivo">
                                        <input type="hidden" name="idArea" value="<%= areaEdit.getIdArea() %>">
                                        <input type="hidden" name="idMotivo" value="${motivo.idMotivo}">
                                        <input type="text" name="nombreMotivo" value="${motivo.nombreMotivo}"
                                               class="form-control form-control-figma w-100 fs-6"
                                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s./]+$"
                                               title="Solo se permiten letras, espacios, puntos y diagonales" required>
                                        <button type="submit" class="btn-figma btn-figma-sm flex-shrink-0" title="Guardar motivo">
                                            <img src="<%= request.getContextPath() %>/assets/img/coordinador/check.png" width="16" alt="Guardar">
                                        </button>
                                    </form>
                                </td>
                                <td class="text-end" style="width: 120px;">
                                    <div class="d-flex justify-content-end gap-2 align-items-center">
                                        <button type="button" class="btn-figma btn-figma-sm" title="Editar motivo"
                                                onclick="toggleEditarMotivo(${motivo.idMotivo})">
                                            <img src="<%= request.getContextPath() %>/assets/img/coordinador/editar.png" width="16" alt="Editar">
                                        </button>
                                        <button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm" title="Eliminar motivo"
                                                onclick="prepararEliminacionMotivo(${motivo.idMotivo}, <%= areaEdit.getIdArea() %>)">
                                            <img src="<%= request.getContextPath() %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty areaEdit.motivos}">
                            <tr>
                                <td class="text-muted">Sin motivos registrados.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>

            </div>

        </div>

        <!-- Botones del AREA: fuera del contenedor de 900px, centrados en toda la pantalla -->
        <div class="d-flex justify-content-center gap-3 w-100 mt-5">
            <button type="button" id="btnCancelarFormularioArea" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                    data-url-cancelar="<%= request.getContextPath() %>/coordinador/areas-apoyo.jsp"
                    onclick="confirmarCancelacionArea()">Cancelar</button>
            <button type="submit" form="formEditarArea" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
        </div>

        <!-- Form oculto compartido: elimina un motivo tras confirmar en prepararEliminacionMotivo() -->
        <form id="formEliminarMotivo" action="<%= request.getContextPath() %>/AreaServlet" method="post" style="display:none;">
            <input type="hidden" name="accion" value="eliminarMotivo">
            <input type="hidden" name="idArea" id="inputEliminarMotivoIdArea">
            <input type="hidden" name="idMotivo" id="inputEliminarMotivoId">
        </form>
        <% } %>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/motivos.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/areas.js"></script>

<% if (mensajeError != null) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        mostrarAlerta('error', 'Error', '<%= mensajeError %>');
    });
</script>
<% } %>
</body>
</html>
