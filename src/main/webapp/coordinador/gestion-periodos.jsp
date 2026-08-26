<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
  Autor: 20253ds074-art
  Fecha de creación: 2026-08-10
  Descripción: Vista de coordinador para gestionar los periodos escolares
  (alta, edición y activación/desactivación).
--%>
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

    <style>
        /* Ajuste para iconos de error en inputs personalizados (igual que en
           formulario-area.jsp / formulario-alumno.jsp) */
        .form-control-figma.is-invalid, .form-select.is-invalid {
            border-color: #dc3545 !important;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(.375em + .1875rem) center;
            background-size: calc(.75em + .375rem) calc(.75em + .375rem);
        }

        .btn-figma:disabled {
            background-color: #7ab899 !important;
            color: #ffffff;
            cursor: not-allowed;
            opacity: 0.6;
            box-shadow: none;
            border: none;
        }
    </style>
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
                                <th>Objetivo Grupales</th>
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
                                    <td>${periodo.asistenciasGrupales}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${periodo.estado == 'N'}"><span class="badge-inactivo">Baja</span></c:when>
                                            <c:otherwise>Activo</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <button type="button" class="btn-accion btn-editar"
                                                    onclick="prepararEdicionPeriodo(this)"
                                                    data-id="${periodo.idPeriodo}"
                                                    data-nombre="${periodo.nombre}"
                                                    data-fecha-inicio="<fmt:formatDate value='${periodo.fechaInicio}' pattern='yyyy-MM-dd'/>"
                                                    data-fecha-fin="<fmt:formatDate value='${periodo.fechaFin}' pattern='yyyy-MM-dd'/>"
                                                    data-objetivo="${periodo.asistenciasGrupales}">
                                                <img src="${pageContext.request.contextPath}/assets/img/coordinador/editar.png" width="16" alt="Editar">
                                            </button>
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

                    <input type="hidden" id="accionPeriodo" name="accion" value="">
                    <input type="hidden" id="idPeriodoEdit" name="idPeriodo" value="">

                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="fw-bold mb-0" id="tituloFormularioPeriodo">Registrar nuevo periodo</h5>
                        <button type="button" id="btnCancelarEdicionPeriodo" class="btn btn-link d-none" onclick="cancelarEdicionPeriodo()">Cancelar edición</button>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6 fw-bold" for="nombre">Nombre del periodo</label>
                        <!-- Readonly: se autocompleta a partir de la Fecha de inicio elegida
                             (ver actualizarNombreAutomatico() en periodos.js). El coordinador
                             ya no lo captura a mano, para que siempre quede en el formato
                             "Mes - Mes Año" que espera el resto del sistema. -->
                        <input type="text" id="nombre" name="nombre" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Se genera al elegir la fecha de inicio" maxlength="50"
                               pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s\-]+$"
                               data-msg-requerido="Selecciona una fecha de inicio válida para generar el nombre." readonly required>
                        <div class="invalid-feedback">Solo se permiten letras, números, espacios y guion (-).</div>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-md-6">
                            <label class="campo-label fs-6 fw-bold" for="fechaInicio">Fecha de inicio</label>
                            <input type="date" id="fechaInicio" name="fechaInicio" class="form-control form-control-figma w-100 fs-6"
                                   style="cursor: pointer;" onclick="this.showPicker()"
                                   data-msg-requerido="Selecciona la fecha de inicio." required>
                            <!-- Texto inicial = mensaje del candado de mes (unico motivo de
                                 invalidez de este campo aparte de "obligatorio"); ver
                                 actualizarValidezFechas() en periodos.js. -->
                            <div class="invalid-feedback">Los cuatrimestres solo pueden iniciar en Enero, Mayo o Septiembre.</div>
                            <!-- Siempre visible (no depende de is-invalid): para que el
                                 coordinador vea la regla de entrada ANTES de equivocarse, en
                                 vez de enterarse solo despues de que el campo se le limpie solo. -->
                            <div class="form-text text-danger mb-0">Solo se puede iniciar en Enero, Mayo o Septiembre.</div>
                        </div>
                        <div class="col-md-6">
                            <label class="campo-label fs-6 fw-bold" for="fechaFin">Fecha de fin</label>
                            <input type="date" id="fechaFin" name="fechaFin" class="form-control form-control-figma w-100 fs-6"
                                   style="cursor: pointer;" onclick="this.showPicker()"
                                   data-msg-requerido="Selecciona la fecha de fin." required>
                            <div class="invalid-feedback">La fecha de fin debe ser posterior a la de inicio.</div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6 fw-bold" for="asistenciasGrupales">Objetivo de tutorías grupales por tutor</label>
                        <input type="number" id="asistenciasGrupales" name="asistenciasGrupales" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Ej. 15" min="0" step="1"
                               data-msg-requerido="Indica el objetivo de tutorías grupales." required>
                        <div class="invalid-feedback">Indica cuántas tutorías grupales debe impartir cada tutor en este periodo.</div>
                        <div class="form-text">Se usará para medir el avance de cada tutor en el reporte de Tutorías Grupales.</div>
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

<jsp:include page="../includes/cargando.jsp" />
<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/periodos.js"></script>
</body>
</html>