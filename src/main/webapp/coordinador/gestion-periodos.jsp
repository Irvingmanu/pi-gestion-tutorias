<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    request.setAttribute("paginaActiva", "periodos");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Periodos Escolares</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/asignacion.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Periodos Escolares
        </div>

        <ul class="nav nav-tabs mb-4" id="periodosTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="tab-periodos-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-periodos" type="button" role="tab" aria-selected="true">
                    Periodos Registrados
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="tab-nuevo-periodo-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-nuevo-periodo" type="button" role="tab" aria-selected="false">
                    Nuevo Periodo
                </button>
            </li>
        </ul>

        <div class="tab-content" id="periodosTabsContent">

            <!-- ==================== TAB 1: PERIODOS REGISTRADOS ==================== -->
            <div class="tab-pane fade show active" id="tab-periodos" role="tabpanel">

                <div class="row mb-3">
                    <div class="col-12 d-flex align-items-center gap-2">
                        <input type="checkbox" id="mostrarInactivos" class="form-check-input">
                        <label class="form-check-label fs-6" for="mostrarInactivos">Mostrar periodos dados de baja</label>
                    </div>
                </div>

                <div class="table-responsive mb-auto">
                    <c:if test="${empty listaPeriodos}">
                        <div class="alert alert-info text-center">
                            No hay periodos escolares registrados todavía.
                        </div>
                    </c:if>
                    <c:if test="${not empty listaPeriodos}">
                        <table class="tabla-grupos fs-6" id="tablaPeriodos">
                            <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Fecha Inicio</th>
                                <th>Fecha Fin</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="periodo" items="${listaPeriodos}">
                                <tr class="${periodo.estado == 'N' ? 'fila-inactiva' : ''}" data-activo="${periodo.estado == 'N' ? 'N' : 'S'}">
                                    <td>${periodo.nombre}</td>
                                    <td><fmt:formatDate value="${periodo.fechaInicio}" pattern="dd/MM/yyyy" /></td>
                                    <td><fmt:formatDate value="${periodo.fechaFin}" pattern="dd/MM/yyyy" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${periodo.estado == 'N'}"><span class="badge-inactivo">Baja</span></c:when>
                                            <c:otherwise>Activo</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <c:if test="${periodo.estado != 'N'}">
                                                <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacionPeriodo('${periodo.idPeriodo}')">
                                                    <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                                </button>
                                            </c:if>
                                            <c:if test="${periodo.estado == 'N'}">
                                                <button type="button" class="btn-accion btn-reactivar" onclick="prepararReactivacionPeriodo('${periodo.idPeriodo}')">
                                                    <img src="${pageContext.request.contextPath}/assets/img/coordinador/reactivar.png" width="16" alt="Reactivar">
                                                </button>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:if>
                </div>

            </div>

            <!-- ==================== TAB 2: NUEVO PERIODO ==================== -->
            <div class="tab-pane fade" id="tab-nuevo-periodo" role="tabpanel">

                <form id="formGuardar" action="${pageContext.request.contextPath}/gestion-periodos" method="POST" class="asignacion-form-wrap mt-3 needs-validation" novalidate>

                    <div class="mb-4">
                        <label class="campo-label fs-6 fw-bold" for="nombre">Nombre del periodo</label>
                        <input type="text" id="nombre" name="nombre" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Ej. Septiembre - Diciembre 2026" maxlength="50" required>
                        <div class="invalid-feedback">Ingresa un nombre para el periodo.</div>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-md-6">
                            <label class="campo-label fs-6 fw-bold" for="fechaInicio">Fecha de inicio</label>
                            <input type="date" id="fechaInicio" name="fechaInicio" class="form-control form-control-figma w-100 fs-6" required>
                            <div class="invalid-feedback">Selecciona la fecha de inicio.</div>
                        </div>
                        <div class="col-md-6">
                            <label class="campo-label fs-6 fw-bold" for="fechaFin">Fecha de fin</label>
                            <input type="date" id="fechaFin" name="fechaFin" class="form-control form-control-figma w-100 fs-6" required>
                            <div class="invalid-feedback">La fecha de fin debe ser posterior a la de inicio.</div>
                        </div>
                    </div>

                    <div class="text-center mt-4">
                        <button type="submit" id="btnGuardar" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
                    </div>

                </form>

            </div>

        </div>

    </div>

</div>

<form id="formEliminarPeriodo" action="${pageContext.request.contextPath}/gestion-periodos" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="idPeriodo" id="inputEliminarPeriodo">
</form>

<form id="formReactivarPeriodo" action="${pageContext.request.contextPath}/gestion-periodos" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="reactivar">
    <input type="hidden" name="idPeriodo" id="inputReactivarPeriodo">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/periodos.js"></script>
</body>
</html>