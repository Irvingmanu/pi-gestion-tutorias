<%--
  Created by IntelliJ IDEA.
  User: Sebastian
  Date: 01/07/2026
  Time: 11:05 p.m.
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
      <a href="#" class="nav-link text-dark rounded-4 p-3 d-flex flex-column align-items-center"">
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

      <a href="#" class="nav-link text-dark p-3 d-flex flex-column align-items-center rounded-4" style="background-color: #F2F2F2;">
        <img src="../assets/img/coordinador/areas.png" alt="Acuerdos" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Áreas</span>
      </a>
    </div>

    <div class="mt-auto w-100 text-center mb-3">
      <a href="perfil.jsp" class="nav-link text-dark p-3 d-flex flex-column align-items-center">
        <img src="../assets/img/coordinador/perfil.png" alt="Perfil" width="40" class="mb-2">
        <span class="fs-6 fw-medium">Perfil</span>
      </a>
    </div>
  </div>

  <div class="flex-grow-1 px-4 py-2">

    <h2 class="text-center mb-4 mt-2 fw-medium">Sistema de Gestión de Tutorías</h2>

    <div class="text-white text-center rounded-4 py-3 mb-5 shadow-sm" style="background-color: #008B74;">
      <h4 class="m-0 fw-semibold">Áreas de Apoyo</h4>
    </div>

    <div class="d-flex justify-content-end mx-auto mb-3" style="max-width: 1100px;">
      <button class="btn text-white rounded-4 px-4 py-2 fw-medium shadow-sm" style="background-color: #008B74;">Nueva área</button>
    </div>

    <div class="d-flex justify-content-between align-items-stretch gap-3 mx-auto mt-4" style="max-width: 1100px;">

      <div class="card border-0 rounded-4 shadow-sm py-4 text-center flex-fill" style="width: 220px;">
        <div class="card-body d-flex flex-column align-items-center justify-content-between">
          <div>
            <img src="../assets/img/coordinador/asesoríasacademicas.png" alt="Asesorías" width="45" class="mb-3">
            <h5 class="card-title fw-bold fs-5 mb-2">Asesorías Académicas</h5>
            <p class="card-text text-secondary mb-1" style="font-size: 0.9rem;">Dr. Alberto Soto</p>
            <p class="card-text text-secondary mb-3" style="font-size: 0.85rem;">asesorias@utez.edu.mx</p>
          </div>
          <div class="w-100 d-flex flex-column gap-2 mt-auto px-2">
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #CC5052;">Eliminar</button>
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #008B74;">Editar</button>
          </div>
        </div>
      </div>

      <div class="card border-0 rounded-4 shadow-sm py-4 text-center flex-fill" style="width: 220px;">
        <div class="card-body d-flex flex-column align-items-center justify-content-between">
          <div>
            <img src="../assets/img/coordinador/psicopedagogía.png" alt="Psicopedagogía" width="45" class="mb-3">
            <h5 class="card-title fw-bold fs-5 mb-2">Psicopedagogía</h5>
            <p class="card-text text-secondary mb-1" style="font-size: 0.9rem;">Mtra. Rosa Díaz</p>
            <p class="card-text text-secondary mb-3" style="font-size: 0.85rem;">psico@utez.edu.mx</p>
          </div>
          <div class="w-100 d-flex flex-column gap-2 mt-auto px-2">
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #D35252;">Eliminar</button>
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #008B74;">Editar</button>
          </div>
        </div>
      </div>

      <div class="card border-0 rounded-4 shadow-sm py-4 text-center flex-fill" style="width: 220px;">
        <div class="card-body d-flex flex-column align-items-center justify-content-between">
          <div>
            <img src="../assets/img/coordinador/serviciomedico.png" alt="Médico" width="45" class="mb-3">
            <h5 class="card-title fw-bold fs-5 mb-2">Servicio Médico</h5>
            <p class="card-text text-secondary mb-1" style="font-size: 0.9rem;">Dr. Hugo Leal</p>
            <p class="card-text text-secondary mb-3" style="font-size: 0.85rem;">medico@utez.edu.mx</p>
          </div>
          <div class="w-100 d-flex flex-column gap-2 mt-auto px-2">
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #D35252;">Eliminar</button>
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #008B74;">Editar</button>
          </div>
        </div>
      </div>

      <div class="card border-0 rounded-4 shadow-sm py-4 text-center flex-fill" style="width: 220px;">
        <div class="card-body d-flex flex-column align-items-center justify-content-between">
          <div>
            <img src="../assets/img/coordinador/becas.png" alt="Becas" width="25" class="mb-3">
            <h5 class="card-title fw-bold fs-5 mb-2">Becas</h5>
            <p class="card-text text-secondary mb-1" style="font-size: 0.9rem;">Lic. Ana Flores</p>
            <p class="card-text text-secondary mb-3" style="font-size: 0.85rem;">becas@utez.edu.mx</p>
          </div>
          <div class="w-100 d-flex flex-column gap-2 mt-auto px-2">
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #D35252;">Eliminar</button>
            <button class="btn text-white rounded-3 py-1 fw-medium" style="background-color: #008B74;">Editar</button>
          </div>
        </div>
      </div>

    </div>

  </div>

</div>

<script src="../assets/js/bootstrap.js"></script>

</body>
</html>
