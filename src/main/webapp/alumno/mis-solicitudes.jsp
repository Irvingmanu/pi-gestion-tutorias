<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-07-28
  Descripción: Vista de alumno "Mis Solicitudes" que lista el historial de sus
  solicitudes de tutoría individual junto con su estado.
--%>
<c:set var="paginaActiva" value="misSolicitudes" scope="request" />
<%
    // Unico bloque Java que queda: solo construye los formateadores de fecha
    // (no hay forma de dar formato "dd MMMM yyyy" en español con JSTL puro
    // sin agregar la libreria fmt:, que no usamos en el resto del proyecto).
    // Se exponen a EL via pageContext para que el resto de la vista sea
    // 100% JSTL/EL, sin bucles ni impresiones en Java.
    java.text.SimpleDateFormat formatoFechaHora = new java.text.SimpleDateFormat("dd MMMM yyyy, hh:mm a", new java.util.Locale("es", "MX"));
    java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("es", "MX"));
    pageContext.setAttribute("formatoFechaHora", formatoFechaHora);
    pageContext.setAttribute("formatoFecha", formatoFecha);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Mis Solicitudes</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-alumno.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Mis Solicitudes
        </div>

        <div class="form-wrap-figma" style="max-width: 100%;">

            <c:choose>
                <c:when test="${empty listaSolicitudes}">
                    <div class="p-4 text-center bg-white rounded shadow-sm border">
                        <p class="fs-5 text-muted mb-0">No has enviado ninguna solicitud todavía.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="d-flex flex-column gap-3">
                        <c:forEach items="${listaSolicitudes}" var="s">
                            <c:choose>
                                <c:when test="${s.estatus == 'Confirmada'}"><c:set var="badge" value="success" /></c:when>
                                <c:when test="${s.estatus == 'Rechazada'}"><c:set var="badge" value="danger" /></c:when>
                                <c:when test="${s.estatus == 'Reprogramada'}"><c:set var="badge" value="info" /></c:when>
                                <c:otherwise><c:set var="badge" value="secondary" /></c:otherwise>
                            </c:choose>
                            <div class="bg-white rounded-figma shadow-sm border p-4">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="fw-bold fs-6">${s.asunto}</div>
                                        <div class="text-muted small mb-2">
                                            Enviada el: ${empty s.fechaRegistro ? 'N/D' : formatoFechaHora.format(s.fechaRegistro)}
                                        </div>

                                        <p class="mb-1">
                                            <strong>Fecha propuesta:</strong>
                                                ${empty s.fechaPropuesta ? 'No especificada' : formatoFecha.format(s.fechaPropuesta)}
                                        </p>
                                        <p class="mb-1">
                                            <strong>Hora de inicio:</strong>
                                                ${empty s.horaPropuesta ? 'No especificada' : s.horaPropuesta}
                                        </p>
                                        <p class="mb-0">
                                            <strong>Duración:</strong>
                                            <c:choose>
                                                <c:when test="${empty s.duracion}">No especificada</c:when>
                                                <c:otherwise>${s.duracion} hora(s)</c:otherwise>
                                            </c:choose>
                                        </p>
                                    </div>

                                    <span class="badge text-bg-${badge} fs-6 px-3 py-2">${s.estatus}</span>
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
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
</body>
</html>