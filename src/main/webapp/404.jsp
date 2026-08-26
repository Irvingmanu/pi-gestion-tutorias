<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-08-20
  Descripción: Página de error 404 (recurso no encontrado), configurada como
  errorPage del sitio. Ofrece un enlace de regreso a la vista de inicio
  correspondiente según el rol de la sesión activa (coordinador, tutor, alumno
  o visitante sin sesión).
--%>
<c:choose>
    <c:when test="${sessionScope.rol == 'Coordinador'}">
        <c:set var="urlInicio" value="${pageContext.request.contextPath}/gestion-grupos" />
        <c:set var="textoInicio" value="Volver a Gestión de Grupos" />
    </c:when>
    <c:when test="${sessionScope.rol == 'Tutor'}">
        <c:set var="urlInicio" value="${pageContext.request.contextPath}/tutoria-individual" />
        <c:set var="textoInicio" value="Volver a Tutoría Individual" />
    </c:when>
    <c:when test="${sessionScope.rol == 'Alumno'}">
        <c:set var="urlInicio" value="${pageContext.request.contextPath}/agenda" />
        <c:set var="textoInicio" value="Volver a mi Agenda" />
    </c:when>
    <c:otherwise>
        <c:set var="urlInicio" value="${pageContext.request.contextPath}/login.jsp" />
        <c:set var="textoInicio" value="Ir a Iniciar Sesión" />
    </c:otherwise>
</c:choose>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Página no encontrada</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/auth.css" rel="stylesheet">

    <style>
        /* Icono grande + numero de error, propios de esta pagina: no ameritan su propio
           archivo CSS para dos reglas. */
        .error-page-icono {
            font-size: 4rem;
            color: var(--utez-green);
            margin-bottom: 0.5rem;
        }
        .error-page-codigo {
            font-weight: 600;
            font-size: 3.5rem;
            color: var(--texto);
            margin-bottom: 0.25rem;
            letter-spacing: 2px;
        }
    </style>
</head>
<body class="auth-body">

<div class="min-vh-100 d-flex align-items-center justify-content-center p-3">

    <div class="auth-card text-center">

        <img src="${pageContext.request.contextPath}/assets/img/login/logoUtez.png" alt="Logotipo UTEZ" class="auth-logo mb-4">

        <i class="bi bi-compass error-page-icono" aria-hidden="true"></i>

        <div class="error-page-codigo">404</div>
        <h1 class="auth-title mb-2">Página no encontrada</h1>
        <p class="auth-subtitle mb-4">
            La página que buscas no existe, se movió o la dirección está mal escrita.
        </p>

        <a href="${urlInicio}" class="btn-figma text-decoration-none px-4 py-2">
            ${textoInicio}
        </a>

    </div>

</div>

</body>
</html>
