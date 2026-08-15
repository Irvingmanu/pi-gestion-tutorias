<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="paginaActiva" value="acuerdos" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Acuerdos</title>
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
            Acuerdos Grupales e Individuales
        </div>

        <div class="form-wrap-figma" style="max-width: 100%;">

            <c:choose>
                <c:when test="${empty listaAcuerdos}">
                    <div class="p-4 text-center bg-white rounded shadow-sm border">
                        <p class="fs-5 text-muted mb-0">No hay acuerdos registrados.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="d-flex flex-column gap-3">

                        <c:forEach items="${listaAcuerdos}" var="acuerdo">
                            <div class="d-flex align-items-center gap-4 p-4 bg-white rounded-figma shadow-sm border">
                                <img src="${pageContext.request.contextPath}/assets/img/alumno/acuerdos.png" alt="Acuerdo ${acuerdo.tipo}" style="width: 40px;">
                                <div>
                                    <div class="fw-bold fs-6">Acuerdo ${acuerdo.tipo}</div>
                                    <div class="text-muted">${acuerdo.fecha}</div>
                                    <p class="mb-0 mt-2">${acuerdo.acuerdos}</p>
                                </div>
                            </div>
                        </c:forEach>

                    </div>
                </c:otherwise>
            </c:choose>

        </div>

    </div>

</div>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
</body>
</html>