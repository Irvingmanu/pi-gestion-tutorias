<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Autor: 20253ds074-art
  Fecha de creación: 2026-08-09
  Descripción: Vista de Historial del Tutor. Muestra el historial de solicitudes,
  sesiones y canalizaciones atendidas por el tutor autenticado, con filtros de
  búsqueda.
--%>
<!--
gdbg
-->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Historial</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="titulo-principal banner-grupos h5 mb-4">
            Historial / Agenda General
        </div>

        <div class="form-wrap-figma mb-4">
            <form id="formFiltrosHistorial" action="${pageContext.request.contextPath}/historial-tutorias" method="get">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label for="tipo" class="form-label fs-6 fw-bold">Tipo de tutoría</label>
                        <select id="tipo" name="tipo" class="form-select form-control-figma w-100 fs-6">
                            <option value="" ${empty tipoSeleccionado ? 'selected' : ''}>Todos</option>
                            <option value="grupal" ${tipoSeleccionado == 'grupal' ? 'selected' : ''}>Grupal</option>
                            <option value="individual" ${tipoSeleccionado == 'individual' ? 'selected' : ''}>Individual</option>
                            <option value="espontanea" ${tipoSeleccionado == 'espontanea' ? 'selected' : ''}>Espontánea</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="fechaInicio" class="form-label fs-6 fw-bold">Desde</label>
                        <input type="date" id="fechaInicio" name="fechaInicio" class="form-control form-control-figma w-100 fs-6"
                               value="${fechaInicioSeleccionada}">
                    </div>
                    <div class="col-md-3">
                        <label for="fechaFin" class="form-label fs-6 fw-bold">Hasta</label>
                        <input type="date" id="fechaFin" name="fechaFin" class="form-control form-control-figma w-100 fs-6"
                               value="${fechaFinSeleccionada}">
                    </div>
                    <div class="col-md-3">
                        <button type="submit" class="btn-figma fw-medium fs-6 px-4 py-2 w-100">Filtrar</button>
                    </div>
                </div>
            </form>
        </div>

        <c:choose>
            <c:when test="${empty historial}">
                <div class="alert alert-info text-center fs-6">
                    No hay tutorías registradas con los filtros seleccionados.
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="tabla-grupos fs-6 w-100">
                        <thead>
                        <tr>
                            <th>Tipo</th>
                            <th>Fecha</th>
                            <th>Hora</th>
                            <th>Grupo / Alumno</th>
                            <th>Temas Tratados</th>
                            <th>Acuerdos</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${historial}">
                            <tr>
                                <td>${item.tipo}</td>
                                <td>${item.fecha}</td>
                                <td>${item.hora}</td>
                                <td>${item.referencia}</td>
                                <td>${item.temasTratados}</td>
                                <td>${item.acuerdos}</td>
                                <td>${item.estado}</td>
                                <td>
                                    <c:if test="${item.tipo == 'Grupal'}">
                                        <a class="btn btn-sm btn-secondary"
                                           href="${pageContext.request.contextPath}/tutoria-grupal?idGrupo=${item.idGrupo}">
                                            Ver asistencia
                                        </a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

</div>

<jsp:include page="../includes/cargando.jsp" />
<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>

</body>

</html>
