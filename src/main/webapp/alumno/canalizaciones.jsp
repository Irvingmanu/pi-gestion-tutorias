<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-08-07
  Descripción: Vista de alumno "Mis Canalizaciones" que lista las
  canalizaciones a áreas de apoyo registradas para él, con su estado y fecha.
--%>
<c:set var="paginaActiva" value="canalizaciones" scope="request" />
<%
    java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("es", "MX"));
    pageContext.setAttribute("formatoFecha", formatoFecha);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Mis Canalizaciones</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-alumno.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Mis Canalizaciones
        </div>

        <div class="form-wrap-figma" style="max-width: 100%;">

            <c:choose>
                <c:when test="${empty listaCanalizaciones}">
                    <div class="p-4 text-center bg-white rounded shadow-sm border">
                        <p class="fs-5 text-muted mb-0">No tienes canalizaciones registradas.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="d-flex flex-column gap-3">
                        <c:forEach items="${listaCanalizaciones}" var="c">
                            <c:set var="badge" value="${c.estatus == 'Pendiente' ? 'warning' : 'success'}" />
                            <div class="bg-white rounded-figma shadow-sm border p-4">
                                <div class="d-flex justify-content-between align-items-start gap-3">
                                    <div>
                                        <div class="fw-bold fs-6">${c.nombreArea}</div>
                                        <div class="text-muted small mb-2">
                                            Canalización del: ${empty c.fechaCanalizacion ? 'N/D' : formatoFecha.format(c.fechaCanalizacion)}
                                        </div>

                                        <c:if test="${not empty c.nombreMotivo}">
                                            <p class="mb-1"><strong>Motivo:</strong> ${c.nombreMotivo}</p>
                                        </c:if>
                                        <c:if test="${not empty c.observaciones}">
                                            <p class="mb-1"><strong>Observaciones:</strong> ${c.observaciones}</p>
                                        </c:if>
                                        <c:if test="${not empty c.encargadoArea}">
                                            <p class="mb-0"><strong>Encargado:</strong> ${c.encargadoArea}</p>
                                        </c:if>
                                    </div>

                                    <span class="badge text-bg-${badge} fs-6 px-3 py-2">${c.estatus}</span>
                                </div>

                                <hr>

                                <div class="d-flex align-items-center gap-3">
                                    <c:choose>
                                        <c:when test="${not empty c.enlaceCitaArea}">
                                            <a href="${c.enlaceCitaArea}" target="_blank" rel="noopener"
                                               class="btn-figma fw-medium text-decoration-none px-3 py-2">Agendar cita</a>
                                        </c:when>
                                        <c:when test="${not empty c.correoContactoArea}">
                                            <a href="mailto:${c.correoContactoArea}"
                                               onclick="mostrarToast('exito', '¡Espera!', 'Abriendo correo...')"
                                               class="btn-figma fw-medium text-decoration-none px-3 py-2">Contactar por correo</a>
                                        </c:when>
                                    </c:choose>
                                    <c:if test="${not empty c.correoContactoArea}">
                                        <span class="text-muted small">${c.correoContactoArea}</span>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>

    </div>

</div>

<jsp:include page="../includes/cargando.jsp" />
<jsp:include page="../includes/alertas.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
</body>
</html>
