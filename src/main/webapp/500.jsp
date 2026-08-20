<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Error del servidor</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/auth.css" rel="stylesheet">

    <style>
        /* Icono grande + numero de error, propios de esta pagina: no ameritan su propio
           archivo CSS para dos reglas. */
        .error-page-icono {
            font-size: 4rem;
            color: #CC5052;
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

        <i class="bi bi-exclamation-octagon-fill error-page-icono" aria-hidden="true"></i>

        <div class="error-page-codigo">500</div>
        <h1 class="auth-title mb-2">Error interno del servidor</h1>
        <p class="auth-subtitle mb-4">
            Tuvimos un problema técnico al procesar tu solicitud. Por favor, intenta de nuevo en unos
            minutos o regresa a la página de inicio.
        </p>

        <a href="${pageContext.request.contextPath}/index.jsp" class="btn-figma text-decoration-none px-4 py-2">
            Volver al inicio
        </a>

    </div>

</div>

</body>
</html>
