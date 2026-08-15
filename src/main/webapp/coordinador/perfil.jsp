<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="paginaActiva" value="perfil" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Perfil Coordinador</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<jsp:include page="../includes/alertas.jsp" />

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Perfil Coordinador
        </div>

        <div class="bg-white p-4 rounded-figma shadow-sm border">

            <div class="d-flex align-items-center gap-3 mb-4">
                <div class="rounded-circle bg-light d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                    <i class="bi bi-person fs-1"></i>
                </div>
                <h4 class="fs-4 mb-0">${coordinador.nombres} ${coordinador.apellidos}</h4>
            </div>

            <p class="fw-bold fs-5 mt-4 mb-3">Información Personal</p>
            <ul class="ms-3">
                <li class="mb-2"><strong>Nombre:</strong> ${coordinador.nombres} ${coordinador.apellidos}</li>
                <li class="mb-2"><strong>Email:</strong> ${coordinador.correoInstitucional}</li>
            </ul>

            <div class="d-flex justify-content-end mt-4">
                <button type="button" class="btn btn-cancelar-figma rounded-figma px-4"
                        id="btnCerrarSesion"
                        data-context-path="${pageContext.request.contextPath}">
                    Cerrar sesión
                </button>
            </div>

        </div>

    </div>

</div>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/logout.js"></script>
</body>
</html>