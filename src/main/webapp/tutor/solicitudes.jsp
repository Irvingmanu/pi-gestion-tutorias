<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%-- Configuramos la región para que los meses salgan en español (ej. "agosto") --%>
<fmt:setLocale value="es_MX" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Solicitudes</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Solicitudes
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">${error}</div>
        </c:if>

        <!-- Lista de solicitudes -->
        <div class="form-wrap-figma" style="max-width: 100%;">
            <div id="listaSolicitudes">

                <c:choose>
                    <c:when test="${empty listaSolicitudes}">
                        <div class="d-flex align-items-center justify-content-center p-4 bg-white rounded shadow-sm border text-muted">
                            No tienes solicitudes por ahora.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="s" items="${listaSolicitudes}">
                            <div class="d-flex align-items-center justify-content-between p-3 mb-3 bg-white rounded shadow-sm border">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="bg-light rounded-circle d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                                        <i class="bi bi-person fs-1"></i>
                                    </div>
                                    <div>
                                        <p class="mb-0 fw-bold text-dark">
                                                ${s.nombreAlumno} ${s.apellidosAlumno}
                                        </p>
                                        <p class="mb-0 small text-secondary">
                                                ${s.asunto}
                                            <c:if test="${not empty s.fechaPropuesta}">
                                                &middot; <fmt:formatDate value="${s.fechaPropuesta}" pattern="dd MMMM yyyy" />
                                            </c:if>
                                        </p>
                                    </div>
                                </div>

                                <div class="d-flex align-items-center gap-3">

                                        <%-- Lógica para determinar el color del badge en JSTL --%>
                                    <c:set var="badge" value="warning" />
                                    <c:choose>
                                        <c:when test="${s.estatus == 'Confirmada'}">
                                            <c:set var="badge" value="success" />
                                        </c:when>
                                        <c:when test="${s.estatus == 'Rechazada'}">
                                            <c:set var="badge" value="danger" />
                                        </c:when>
                                        <c:when test="${s.estatus == 'Reprogramada'}">
                                            <c:set var="badge" value="info" />
                                        </c:when>
                                    </c:choose>

                                    <span class="badge text-bg-${badge}">${s.estatus}</span>

                                    <a href="${pageContext.request.contextPath}/solicitudes?accion=detalle&idSolicitud=${s.idSolicitud}"
                                       class="btn-figma fw-medium px-4 py-2">Ver</a>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>

    </div>

</div>

<jsp:include page="../includes/cargando.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
</body>
</html>