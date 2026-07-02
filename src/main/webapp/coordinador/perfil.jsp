<%--
  Created by IntelliJ IDEA.
  User: Sebastian
  Date: 01/07/2026
  Time: 10:57 p.m.
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sistema de Gestión de Tutorías - UTEZ</title>
  <link href="../assets/css/bootstrap.css" rel="stylesheet">

  <style>
    @font-face {
      font-family: 'SN Pro';
      src: url('../assets/fonts/SNPro-Regular.woff2') format('woff2'); /* Cambia la ruta a donde guardes tu fuente */
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

  <div class="bg-white rounded-4 shadow-sm d-flex flex-column align-items-center py-4 px-2" style="width: 140px; min-height: calc(100vh - 2rem);">

    <div class="mb-5 mt-2">
      <img src="../assets/img/coordinador/logo-utez.png" alt="UTEZ" width="85">
    </div>

    <div class="nav flex-column w-100 text-center gap-3">
      <a href="#" class="nav-link text-dark rounded-4 p-3 d-flex flex-column align-items-center">
        <img src="../assets/img/coordinador/tutores.png" alt="Agenda" width="40" class="mb-2">
        <span class="fs-6 fw-semibold">Tutores</span>
      </a>

      <a href="#" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
        <img src="../assets/img/coordinador/grupos.png" alt="Solicitud" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Grupos</span>
      </a>

      <a href="#" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
        <img src="../assets/img/coordinador/asignacion.png" alt="Acuerdos" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Asignación</span>
      </a>

      <a href="#" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
        <img src="../assets/img/coordinador/reportes.png" alt="Acuerdos" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Reportes</span>
      </a>

      <a href="#" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
        <img src="../assets/img/coordinador/areas.png" alt="Acuerdos" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Áreas</span>
      </a>
    </div>

    <div class="mt-auto w-100 text-center mb-3">
      <a href="perfil.jsp" class="nav-link text-dark p-3 d-flex flex-column align-items-center rounded-4" style="background-color: #F2F2F2;">
        <img src="../assets/img/coordinador/perfil.png" alt="Perfil" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Perfil</span>
      </a>
    </div>
  </div>

  <div class="flex-grow-1 px-4 py-2">

    <h2 class="text-center mb-4 mt-2 fw-medium">Sistema de Gestión de Tutorías</h2>

    <div class="text-white text-center rounded-3 py-3 mb-5 shadow-sm" style="background-color: #008B74;">
      <h4 class="m-0 fw-semibold">Perfil Coordinador</h4>
    </div>

    <div class="card border-0 rounded-4 shadow-sm p-5 mx-auto" style="max-width: 80%; height: 60%; background-color: #FFFFFF;">

      <div class="d-flex align-items-center gap-4 mb-4">
        <div class="bg-light rounded-circle d-flex align-items-center justify-content-center" style="width: 70px; height: 70px">
          <img src="../assets/img/coordinador/perfil.png" alt="Perfil" width="40">
        </div>
        <h4 class="mb-0 fw-semibold text-dark fs-2">Irving Manuel Flores Torrescano</h4>
      </div>

      <div class="text-dark">

        <h5 class="fw-bold mb-3 fs-3" style="font-size: 1.1rem;">Información Personal</h5>
        <ul class="custom-bullets mb-4">
          <li><span class="fw-medium">Nombre:</span> Flores Torrescano Irving Manuel</li>
          <li><span class="fw-medium">Matrícula:</span> 2025DS071</li>
          <li><span class="fw-medium">Email:</span> 2025DS071@utez.edu.mx</li>
          <li><span class="fw-medium">Teléfono:</span> +52 777 255 01 80</li>
        </ul>

        <h5 class="fw-bold mb-3 fs-3" style="font-size: 1.1rem;">Información Académica</h5>
        <ul class="custom-bullets">
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