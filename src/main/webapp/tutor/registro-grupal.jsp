<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Registro de Tutoría Grupal</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="titulo-principal banner-grupos h5 mb-4">
            Registro de Tutoría Grupal
        </div>

        <c:choose>
            <c:when test="${empty asignaciones}">
                <div class="alert alert-warning text-center fs-5">
                    No tienes grupos asignados para tutoría.
                </div>
            </c:when>
            <c:otherwise>
                <div class="form-wrap-figma" style="max-width: 900px;">
                    <form id="formRegistroGrupal" action="${pageContext.request.contextPath}/tutoria-grupal" method="post">

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="grupoAsignado" class="form-label fs-6 fw-bold">Grupo</label>
                                <select id="grupoAsignado" name="grupoAsignado" class="form-select form-control-figma w-100 fs-6" required>
                                    <c:if test="${asignaciones.size() > 1}">
                                        <option value="" selected>Seleccione el grupo</option>
                                    </c:if>
                                    <c:forEach var="asignacion" items="${asignaciones}">
                                        <option value="${asignacion.valorOption}">${asignacion.etiqueta}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                                <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" required>
                            </div>
                            <div class="col-md-3">
                                <label for="hora" class="form-label fs-6 fw-bold">Hora</label>
                                <input type="time" id="hora" name="hora" class="form-control form-control-figma w-100 fs-6" required>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                                <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                          rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                            </div>
                            <div class="col-md-6">
                                <label for="asesoriasGrupales" class="form-label fs-6 fw-bold">Asesorías grupales (Opcional)</label>
                                <input type="text" id="asesoriasGrupales" name="asesorias" class="form-control form-control-figma w-100 fs-6"
                                       placeholder="Describe las asesorías grupales">
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-12">
                                <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                                <textarea id="temasTratados" name="temas" class="form-control form-control-figma w-100 fs-6"
                                          rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                            </div>
                        </div>

                        <div id="contenedorAsistencia" class="mb-4" style="display: none;">
                            <p class="fs-5 fw-bold text-center my-3">Lista de Asistencia</p>
                            <div class="table-responsive">
                                <table class="tabla-grupos fs-6 w-100">
                                    <thead>
                                    <tr>
                                        <th>Matrícula</th>
                                        <th>Nombre</th>
                                        <th class="text-center">Asistencia</th>
                                    </tr>
                                    </thead>
                                    <tbody id="cuerpoTablaAsistencia">
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end mt-4">
                            <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                        </div>

                    </form>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script>
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<!-- Referencia a tu nuevo script separado -->
<script src="${pageContext.request.contextPath}/assets/js/tutor/registro-grupal.js"></script>

</body>
</html>