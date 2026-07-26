<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Coordinador" %>
<%
    request.setAttribute("paginaActiva", "perfil");

    Coordinador coordinador = (Coordinador) request.getAttribute("coordinador");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Perfil Coordinador</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/perfil.css" rel="stylesheet">
</head>
<body>

<jsp:include page="../includes/alertas.jsp" />

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Perfil coordinador
        </div>

        <div class="perfil-card p-4">

            <div class="d-flex align-items-center gap-3 mb-4">
                <div class="perfil-avatar">
                    <img src="<%= request.getContextPath() %>/assets/css/bi/person-fill.svg" alt="">
                </div>
                <h5 class="fw-bold mb-0"><%= coordinador.getNombres() %> <%= coordinador.getApellidos() %></h5>
            </div>

            <div class="mb-4">
                <p class="fs-5 fw-bold mb-2">Información Personal</p>
                <ul class="fs-6">
                    <li>Nombre: <%= coordinador.getApellidos() %> <%= coordinador.getNombres() %></li>
                    <li>Email: <%= coordinador.getCorreoInstitucional() %></li>
                </ul>
            </div>

            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-cancelar-figma rounded-figma px-4" id="btnCerrarSesion">
                    Cerrar sesión
                </button>
            </div>

        </div>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script>
    document.getElementById('btnCerrarSesion').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Cerrar sesión?',
            'Tendrás que iniciar sesión de nuevo para continuar.',
            'Cerrar sesión',
            function () {
                window.location.href = '<%= request.getContextPath() %>/logout';
            }
        );
    });
</script>
</body>
</html>