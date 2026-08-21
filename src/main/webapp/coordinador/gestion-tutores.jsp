<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    request.setAttribute("paginaActiva", "tutores");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Tutores</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-tutores.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Gestión de Tutores
        </div>

        <!-- Buscar tutor / Filtro Academia / Nuevo Tutor -->
        <div class="row mb-3">
            <div class="col-12 d-flex justify-content-between align-items-end">
                <!-- 1. Buscar tutor -->
                <div>
                    <label class="campo-label fs-6" for="buscarTutor">Buscar tutor</label>
                    <input type="text" id="buscarTutor" class="campo-buscar campo-buscar-tutor"
                           placeholder="Buscar por nombre">
                </div>

                <!-- 2. Select Academia -->
                <div class="flex-grow-1 mx-3">
                    <label class="campo-label fs-6" for="academiaFiltroTutores">Academia</label>
                    <select id="academiaFiltroTutores" class="campo-select w-100">
                        <option value="">Todas las academias</option>
                        <c:forEach var="academia" items="${listaAcademias}">
                            <option value="${academia.idAcademia}">${academia.nombre}</option>
                        </c:forEach>
                    </select>
                </div>

                <!-- 3. Acciones (Nuevo Tutor) -->
                <div class="d-flex align-items-end gap-3">
                    <!-- Nuevo Tutor -->
                    <div class="text-center">
                        <label class="campo-label fs-6 d-block">Nuevo Tutor</label>
                        <a href="${pageContext.request.contextPath}/gestion-tutores?accion=nuevo" class="btn-figma text-decoration-none d-block">Agregar</a>
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
                <c:if test="${empty listaTutores}">
                    <div class="alert alert-info text-center">
                        No hay tutores registrados todavía.
                    </div>
                </c:if>
                <c:if test="${not empty listaTutores}">
                    <table class="tabla-grupos fs-6">
                        <colgroup>
                            <col class="col-nomina">
                            <col class="col-nombre-t">
                            <col class="col-correo-t">
                            <col class="col-telefono">
                            <col class="col-academia">
                            <col class="col-grupo-t">
                            <col class="col-acciones-t">
                        </colgroup>
                        <thead>
                        <tr>
                            <th>Nomina</th>
                            <th>Nombre</th>
                            <th>Correo</th>
                            <th>Teléfono</th>
                            <th>Academia</th>
                            <th>Grupo</th>
                            <th>Acciones</th>
                        </tr>
                        </thead>
                        <tbody id="tablaTutores">
                        <c:forEach var="tutor" items="${listaTutores}">
                            <tr class="${tutor.estado == 'N' ? 'fila-inactiva' : ''}"
                                data-nombre="${fn:toLowerCase(tutor.nombres)} ${fn:toLowerCase(tutor.apellidos)}"
                                data-academia="${tutor.idAcademia}"
                                data-activo="${tutor.estado == 'N' ? 'N' : 'S'}">
                                <td>${tutor.numeroEmpleado}</td>
                                <td>
                                        ${tutor.nombres} ${tutor.apellidos}
                                    <c:if test="${tutor.estado == 'N'}">
                                        <span class="badge-inactivo">(Baja)</span>
                                    </c:if>
                                </td>
                                <td>${tutor.correoInstitucional}</td>
                                <td>${tutor.telefono}</td>
                                <td>${nombresAcademia[tutor.idAcademia]}</td>
                                <td>${tutor.grupoAsignadoTexto}</td>
                                <td>
                                    <div class="d-flex justify-content-center gap-2">
                                        <c:if test="${tutor.estado != 'N'}">
                                            <a href="${pageContext.request.contextPath}/gestion-tutores?accion=prepararEdicion&nomina=${tutor.numeroEmpleado}" class="btn-accion btn-editar">
                                                <img src="${pageContext.request.contextPath}/assets/img/coordinador/editar.png" width="16" alt="Editar">
                                            </a>
                                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('${tutor.numeroEmpleado}')">
                                                <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                            </button>
                                        </c:if>
                                        <c:if test="${tutor.estado == 'N'}">
                                            <button type="button" class="btn-accion btn-reactivar" onclick="prepararReactivacion('${tutor.numeroEmpleado}')">
                                                <img src="${pageContext.request.contextPath}/assets/img/coordinador/reactivar.png" width="16" alt="Reactivar">
                                            </button>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <tr id="filaSinResultados" style="display: none;">
                            <td colspan="7" class="text-center">No se encontraron tutores con los filtros seleccionados.</td>
                        </tr>
                        </tbody>
                    </table>
                </c:if>
            </div>

        </div>

    </div>

    <form id="formEliminarTutor" action="${pageContext.request.contextPath}/gestion-tutores" method="POST" style="display:none;">
        <input type="hidden" name="accion" value="eliminar">
        <input type="hidden" name="nomina" id="inputEliminarNomina">
    </form>

    <form id="formReactivarTutor" action="${pageContext.request.contextPath}/gestion-tutores" method="POST" style="display:none;">
        <input type="hidden" name="accion" value="reactivar">
        <input type="hidden" name="nomina" id="inputReactivarNomina">
    </form>

    <jsp:include page="../includes/alertas.jsp" />

    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/coordinador/tutor.js"></script>
</body>
</html>