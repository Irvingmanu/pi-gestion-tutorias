<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    request.setAttribute("paginaActiva", "perfil");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Perfil Coordinador</title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="../assets/css/coordinador/perfil.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Perfil coordinador
        </div>

        <div class="perfil-card p-4">

            <div class="d-flex align-items-center gap-3 mb-4">
                <div class="perfil-avatar">
                    <img src="../assets/css/bi/person-fill.svg" alt="">
                </div>
                <h5 class="fw-bold mb-0">Irving Manuel Flores Torrescano</h5>
            </div>

            <div class="mb-4">
                <p class="fs-5 fw-bold mb-2">Información Personal</p>
                <ul class="fs-6">
                    <li>Nombre: Flores Torrescano Irving Manuel</li>
                    <li>Matrícula: 2025DS071</li>
                    <li>Email: 2025DS071@utez.edu.mx</li>
                    <li>Teléfono: +52 777 255 01 80</li>
                </ul>
            </div>

            <div>
                <p class="fs-5 fw-bold mb-2">Información Académica</p>
                <ul class="fs-6">
                    <li>Coordinador Académico</li>
                    <li>Docencia 4</li>
                </ul>
            </div>

        </div>

    </div>

</div>

<script src="../assets/js/bootstrap.js"></script>
</body>
</html>
