<%--
  Created by IntelliJ IDEA.
  User: irving
  Date: 30/06/2026
  Time: 09:17 p. m.
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - UTEZ</title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">

    <style>
        @font-face {
            font-family: 'SN Pro';
            src: url('../assets/fonts/SNPro-Regular.woff2') format('woff2');
            font-weight: 400;
        }
        @font-face {
            font-family: 'SN Pro';
            src: url('../assets/fonts/SNPro-Medium.woff2') format('woff2');
            font-weight: 500;
        }
        @font-face {
            font-family: 'SN Pro';
            src: url('../assets/fonts/SNPro-Semibold.woff2') format('woff2');
            font-weight: 600;
        }

        /* se aplica a todo el body */
        body {
            background-color: #F2F2F2;
            font-family: 'SN Pro', system-ui, -apple-system, sans-serif !important;
        }

    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <div class="bg-white rounded-4 shadow-sm d-flex flex-column align-items-center py-4 px-2" style="width: 140px; height: calc(100vh - 3rem);">

        <div class="mb-5 mt-2">
            <img src="../assets/img/alumno/logo-utez.png" alt="UTEZ" width="85">
        </div>

        <div class="nav flex-column w-100 text-center gap-3">
            <a href="#" class="nav-link text-dark rounded-4 p-3 d-flex flex-column align-items-center" style="background-color: #F2F2F2;">
                <img src="../assets/img/alumno/calendario.png" alt="Agenda" width="40" class="mb-2">
                <span class="fs-5 fw-semibold">Agenda</span>
            </a>

            <a href="solicitud.jsp" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
                <img src="../assets/img/alumno/solicitud.png" alt="Solicitud" width="40" class="mb-2">
                <span class="fs-5 fw-medium">Solicitud</span>
            </a>

            <a href="acuerdos.jsp" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
                <img src="../assets/img/alumno/acuerdos.png" alt="Acuerdos" width="40" class="mb-2">
                <span class="fs-5 fw-medium">Acuerdos</span>
            </a>
        </div>

        <div class="mt-auto w-100 text-center mb-3">
            <a href="perfil.jsp" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
                <img src="../assets/img/alumno/perfil.png" alt="Perfil" width="40" class="mb-2">
                <span class="fs-5 fw-medium">Perfil</span>
            </a>
        </div>
    </div>

    <div class="flex-grow-1 px-4 py-2">

        <h2 class="text-center mb-4 mt-2 fw-medium">Sistema de Gestión de Tutorías</h2>

        <div class="text-white text-center rounded-3 py-3 mb-5 shadow-sm" style="background-color: #008B74;">
            <h4 class="m-0 fw-semibold">Agenda MAYO-AGOSTO 2026</h4>
        </div>

        <div class="d-flex flex-column gap-4 mx-auto" style="max-width: 900px;">

            <div class="card border-0 rounded-4 shadow-sm px-4 py-3">
                <div class="d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center gap-4">
                        <div>
                            <img src="../assets/img/alumno/perfil.png" alt="Usuario" width="45">
                        </div>
                        <div>
                            <h5 class="mb-1 fw-bold">Tutoría Individual</h5>
                            <p class="mb-0 text-dark fw-medium">Flores Torrescano Irving Manuel</p>
                            <p class="mb-0 text-dark" style="font-size: 0.95rem;">24 Mayo 2026 - 2:00pm</p>
                        </div>
                    </div>
                    <div class="fw-bold fs-5 text-dark">
                        Próximo
                    </div>
                </div>
            </div>

            <div class="card border-0 rounded-4 shadow-sm px-4 py-3">
                <div class="d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center gap-4">
                        <div>
                            <img src="../assets/img/alumno/grupo.png" alt="Grupo" width="55">
                        </div>
                        <div>
                            <h5 class="mb-1 fw-bold">Tutoría Grupal</h5>
                            <p class="mb-0 text-dark fw-medium">Grupo 3°C</p>
                            <p class="mb-0 text-dark" style="font-size: 0.95rem;">2 Junio 2026 - 12:00pm</p>
                        </div>
                    </div>
                    <div class="fw-bold fs-5 text-dark">
                        Próximo
                    </div>
                </div>
            </div>

            <div class="card border-0 rounded-4 shadow-sm px-4 py-3">
                <div class="d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center gap-4">
                        <div>
                            <img src="../assets/img/alumno/grupo.png" alt="Nuevo Acuerdo" width="55">
                        </div>
                        <div>
                            <h5 class="mb-1 fw-bold">Nuevo Acuerdo Grupal</h5>
                            <p class="mb-0 text-dark fw-medium">Grupo 3°C</p>
                            <p class="mb-0 text-dark" style="font-size: 0.95rem;">2 Junio 2026 - 12:00pm</p>
                        </div>
                    </div>
                    <div class="fw-bold fs-5 text-dark">
                        Próximo
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>

<script src="../assets/js/bootstrap.js"></script>
</body>
</html>