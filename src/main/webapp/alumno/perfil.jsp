<%--
  Created by IntelliJ IDEA.
  User: irving
  Date: 30/06/2026
  Time: 09:18 p. m.
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Perfil alumno</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<jsp:include page="../includes/alertas.jsp" />

<h1 style="text-align: center">PERFIL</h1>

<div style="text-align: center; margin-top: 20px;">
    <button type="button" id="btnCerrarSesion">Cerrar sesión</button>
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