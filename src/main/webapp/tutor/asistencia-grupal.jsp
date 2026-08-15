<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Asistencia de Tutoría Grupal</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="titulo-principal banner-grupos h5 mb-4">
            Asistencia de Tutoría Grupal
        </div>

        <div class="form-wrap-figma" style="max-width: 900px;">

            <!-- Formulario 1: Filtros y Consulta (Método GET) -->
            <form action="${pageContext.request.contextPath}/tutor/asistencia-grupal" method="get">

                <!-- Fila 1: Grupo / Fecha -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="grupo" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="grupo" name="idGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="">Seleccione el grupo</option>
                            <c:forEach var="grupo" items="${listaGrupos}">
                                <option value="${grupo.idGrupo}" ${param.idGrupo == grupo.idGrupo ? 'selected' : ''}>
                                        ${grupo.nombreGrupo}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" value="${param.fecha}" required>
                    </div>
                </div>

                <div class="d-flex justify-content-end mb-4">
                    <button type="submit" id="btnConsultar" class="btn btn-secondary px-4">Consultar Alumnos</button>
                </div>
            </form>

            <!-- Formulario 2: Tabla de Alumnos y Guardado (Método POST) -->
            <form id="formGuardar" action="${pageContext.request.contextPath}/tutor/asistencia-grupal" method="post">
                <input type="hidden" name="idGrupo" value="${param.idGrupo}">
                <input type="hidden" name="fecha" value="${param.fecha}">

                <div class="table-responsive mb-4">
                    <table class="tabla-grupos fs-6 w-100">
                        <thead>
                        <tr>
                            <th>Nombres</th>
                            <th>Apellidos</th>
                            <th class="text-center">Asistencia</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty listaAlumnosGrupo}">
                                <c:forEach var="alumno" items="${listaAlumnosGrupo}">
                                    <tr>
                                        <td>${alumno.nombres}</td>
                                        <td>${alumno.apellidos}</td>
                                        <td class="text-center">
                                            <input type="checkbox" name="asistencia" value="${alumno.matricula}" ${matriculasAsistidas.contains(alumno.matricula) ? 'checked' : ''} />
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="3" class="text-center text-muted py-3">Seleccione los filtros y presione "Consultar Alumnos".</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <c:if test="${not empty listaAlumnos}">
                    <div class="d-flex justify-content-end mt-4">
                        <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                    </div>
                </c:if>

            </form>
        </div>

    </div>

</div>

<!-- Modal de Confirmación -->
<div class="modal fade" id="modalConfirmacion" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 420px;">
        <div class="modal-content alerta-card p-0 border-0">
            <div class="alerta-body">
                <div class="confirmacion-icono-wrap">
                    <div id="confirmacionIconoCirculo" class="confirmacion-icono">
                        <img id="confirmacionIcono" src="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/" alt="Confirmación">
                    </div>
                </div>
                <div class="alerta-texto">
                    <h3 id="confirmacionTitulo" class="confirmacion-titulo"></h3>
                    <p id="confirmacionMensaje" class="confirmacion-mensaje"></p>
                </div>
                <div class="confirmacion-botones" style="display: flex; gap: 12px; width: 100%;">
                    <button type="button" class="btn-confirmar-cancelar w-100" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" id="btnConfirmacionAceptar" class="btn-confirmar w-100">Sí, guardar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
</body>
</html>