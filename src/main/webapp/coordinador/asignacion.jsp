<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
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

                <div class="table-responsive mb-auto">
                    <c:if test="${empty listaAsignaciones}">
                        <div class="alert alert-info text-center">
                            No hay asignaciones registradas todavía.
                        </div>
                    </c:if>
                    <c:if test="${not empty listaAsignaciones}">
                        <table class="tabla-grupos fs-6">
                            <thead>
                            <tr>
                                <th>Tutor</th>
                                <th>Carrera</th>
                                <th>Cuatrimestre</th>
                                <th>Grupo</th>
                                <th>Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="asignacion" items="${listaAsignaciones}">
                                <tr>
                                    <td>${asignacion.nombresTutor} ${asignacion.apellidosTutor}</td>
                                    <td>${asignacion.nombreCarrera}</td>
                                    <td>${asignacion.numeroCuatrimestre}&deg;</td>
                                    <td>${asignacion.letraGrupo}</td>
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacionAsignacion('${asignacion.idAsignacion}')">
                                                <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:if>
                </div>

            </div>

            <!-- ==================== TAB 2: NUEVA ASIGNACIÓN ==================== -->
            <div class="tab-pane fade" id="tab-nueva" role="tabpanel">

                <form action="${pageContext.request.contextPath}/asignacion" method="POST" class="asignacion-form-wrap mt-3">

                    <div class="mb-4">
                        <label class="campo-label fs-6" for="tutor">Tutor</label>
                        <select id="tutor" name="id_tutor" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected disabled>Seleccione el tutor</option>
                            <c:forEach var="tutor" items="${listaTutores}">
                                <option value="${tutor.idTutor}">${tutor.nombres} ${tutor.apellidos}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6" for="carrera">Carrera</label>
                        <select id="carrera" name="id_carrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected disabled>Seleccione la carrera</option>
                            <c:forEach var="carrera" items="${carreras}">
                                <option value="${carrera.idCarrera}">${carrera.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                        <select id="cuatrimestre" name="id_cuatrimestre" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected disabled>Seleccione el cuatrimestre</option>
                            <c:forEach var="cuatrimestre" items="${listaCuatrimestres}">
                                <option value="${cuatrimestre.idCuatrimestre}">${cuatrimestre.numero}°</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label class="campo-label fs-6" for="grupo">Grupo</label>
                        <select id="grupo" name="id_letra_grupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected disabled>Seleccione el Grupo</option>
                            <c:forEach var="letraGrupo" items="${listaLetras}">
                                <option value="${letraGrupo.idLetra}">${letraGrupo.letra}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="text-center mt-4">
                        <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Agregar</button>
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

<!-- 1. PRIMERO cargamos el script base de Bootstrap para que inicialice objetos como Toast y Modal -->
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>

<!-- 2. DESPUÉS incluimos el HTML de los modales y toasts -->
<jsp:include page="../includes/alertas.jsp" />

<!-- 3. Script de alertas compartido: expone mostrarConfirmacion() para el borrado -->
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>

<!-- 4. Script dedicado a esta vista -->
<script src="${pageContext.request.contextPath}/assets/js/coordinador/asignacion.js"></script>
</body>
</html>