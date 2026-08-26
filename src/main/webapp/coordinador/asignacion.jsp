<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-07-17
  Descripción: Vista de coordinador para asignar tutores a grupos, con
  validaciones de formulario sobre el tutor y el grupo seleccionados.
--%>
<%
    request.setAttribute("paginaActiva", "asignacion");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Asignación de Tutores</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/asignacion.css" rel="stylesheet">

    <style>
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
            Asignación de Tutores
        </div>

        <ul class="nav nav-tabs mb-4" id="asignacionTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="tab-asignaciones-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-asignaciones" type="button" role="tab" aria-selected="true">
                    Asignaciones Actuales
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="tab-nueva-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-nueva" type="button" role="tab" aria-selected="false">
                    Nueva Asignación
                </button>
            </li>
        </ul>

        <div class="tab-content" id="asignacionTabsContent">

            <!-- ==================== TAB 1: ASIGNACIONES ACTUALES ==================== -->
            <div class="tab-pane fade show active" id="tab-asignaciones" role="tabpanel">

                <!-- Filtro OPCIONAL: solo oculta filas de la tabla por JS, no bloquea nada. -->
                <select id="filtroAcademiaTabla" class="form-select form-control-figma w-auto mb-3">
                    <option value="">Todas las academias</option>
                    <c:forEach var="academia" items="${listaAcademias}">
                        <option value="${academia.idAcademia}">${academia.nombre}</option>
                    </c:forEach>
                </select>

                <div class="table-responsive mb-auto">
                    <c:if test="${empty listaAsignaciones}">
                        <div class="alert alert-info text-center">
                            No hay asignaciones registradas todavía.
                        </div>
                    </c:if>
                    <c:if test="${not empty listaAsignaciones}">
                        <table class="tabla-grupos fs-6" id="tablaAsignaciones">
                            <thead>
                            <tr>
                                <th>Tutor</th>
                                <th>Grupo</th>
                                <th>Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="asignacion" items="${listaAsignaciones}">
                                <tr data-academia-id="${asignacion.idAcademia}">
                                    <td>${asignacion.nombresTutor} ${asignacion.apellidosTutor}</td>
                                    <td>${asignacion.nombreGrupo}</td>
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacionAsignacion('${asignacion.idAsignacion}')">
                                                <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <tr id="filaSinResultados" style="display: none;">
                                <td colspan="3" class="text-center text-muted">No se encontraron asignaciones para la academia seleccionada.</td>
                            </tr>
                            </tbody>
                        </table>
                    </c:if>
                </div>

            </div>

            <!-- ==================== TAB 2: NUEVA ASIGNACIÓN ==================== -->
            <div class="tab-pane fade" id="tab-nueva" role="tabpanel">

                <c:if test="${empty listaGrupos}">
                    <div class="alert alert-warning text-center">
                        Todavía no hay grupos registrados.
                        Ve a <a href="${pageContext.request.contextPath}/gestion-grupos">Gestión de Grupos</a> y registra al menos un alumno antes de asignar tutores.
                    </div>
                </c:if>

                <form id="formGuardar" action="${pageContext.request.contextPath}/asignacion" method="POST" class="asignacion-form-wrap mt-3 needs-validation" novalidate>

                    <div class="mb-4">
                        <label class="campo-label fs-6 fw-bold" for="academiaFormulario">Academia</label>
                        <!-- Filtro OBLIGATORIO en cascada: no lleva "name" (no viaja en el POST),
                             solo oculta opciones de Tutor y Grupo hasta que se elige una academia. -->
                        <select id="academiaFormulario" class="form-select form-control-figma mb-3" required>
                            <option value="" selected disabled>Seleccione la academia</option>
                            <c:forEach var="academia" items="${listaAcademias}">
                                <option value="${academia.idAcademia}">${academia.nombre}</option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione una academia.</div>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6 fw-bold" for="tutor">Tutor</label>
                        <!-- Deshabilitado hasta elegir Academia; todas las opciones ya vienen
                             renderizadas con data-academia-id (ver asignacion.js). -->
                        <select id="tutor" name="id_tutor" class="form-select form-control-figma w-100 fs-6" required disabled>
                            <option value="" selected disabled>Seleccione primero la academia</option>
                            <c:forEach var="tutor" items="${listaTutores}">
                                <option value="${tutor.numeroEmpleado}" data-academia-id="${tutor.idAcademia}">${tutor.nombres} ${tutor.apellidos}</option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un tutor.</div>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6 fw-bold" for="grupo">Grupo</label>
                        <!-- Regla de negocio: un tutor solo puede asignarse a grupos de su misma
                             academia (ej. un tutor de DATIT no puede recibir un grupo de otra
                             academia). Deshabilitado hasta elegir Academia, igual que Tutor. -->
                        <select id="grupo" name="id_grupo" class="form-select form-control-figma w-100 fs-6" required disabled>
                            <option value="" selected disabled>Seleccione primero la academia</option>
                            <c:forEach var="grupo" items="${listaGrupos}">
                                <option value="${grupo.idGrupo}" data-academia-id="${grupo.idAcademia}">${grupo.nombreGrupo}</option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un grupo.</div>
                    </div>

                    <div class="text-center mt-4">
                        <button type="submit" id="btnGuardar" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Agregar</button>
                    </div>

                </form>

            </div>

        </div>

    </div>

</div>

<form id="formEliminarAsignacion" action="${pageContext.request.contextPath}/asignacion" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id_asignacion" id="inputEliminarAsignacion">
</form>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<jsp:include page="../includes/cargando.jsp" />
<jsp:include page="../includes/alertas.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/asignacion.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/formulario-asignacion.js"></script>

</body>
</html>