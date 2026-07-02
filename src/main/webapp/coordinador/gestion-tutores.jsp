<%--
  Created by IntelliJ IDEA.
  User: irving
  Date: 01/07/2026
  Time: 09:13 p. m.
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

        body {
            background-color: #F2F2F2;
            font-family: 'SN Pro', system-ui, -apple-system, sans-serif !important;
        }

        .rounded-20 {
            border-radius: 20px !important;
        }

        .search-input:focus {
            outline: none;
        }

        .tabla-custom {
            width: 100%;
            background-color: #ffffff;
            border-collapse: separate;
            border-spacing: 0;
            border-radius: 20px;
            border: 1px solid #e0e0e0;
        }

        .tabla-custom th, .tabla-custom td {
            padding: 12px 15px;
            border-bottom: 1px solid #e0e0e0;
            border-right: 1px solid #e0e0e0;
            vertical-align: middle;
        }

        .tabla-custom th {
            background-color: #f0f0f0;
            font-weight: 600;
            color: #000;
            text-align: left;
        }

        .tabla-custom td {
            font-size: 0.9rem;
        }

        .tabla-custom th:last-child, .tabla-custom td:last-child {
            border-right: none;
        }
        .tabla-custom tr:last-child td {
            border-bottom: none;
        }

        .tabla-custom th:first-child { border-top-left-radius: 20px; }
        .tabla-custom th:last-child { border-top-right-radius: 20px; }
        .tabla-custom tr:last-child td:first-child { border-bottom-left-radius: 20px; }
        .tabla-custom tr:last-child td:last-child { border-bottom-right-radius: 20px; }

        .btn-accion {
            border-radius: 20px;
            border: none;
            padding: 4px 20px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn-editar {
            background-color: #e0e0e0;
        }
        .btn-eliminar {
            background-color: #d15b5b;
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <div class="bg-white rounded-20 shadow-sm d-flex flex-column py-4 px-2" style="width: 140px;">

        <div class="mb-5 mt-2 text-center">
            <img src="../assets/img/coordinador/logo-utez.png" alt="UTEZ" width="85">
        </div>

        <div class="w-100 d-flex flex-column gap-3">

            <a href="#" class="text-decoration-none text-dark rounded-20 p-3 w-100 d-flex flex-column align-items-center" style="background-color: #F2F2F2;">
                <img src="../assets/img/coordinador/tutores.png" alt="Tutores" width="40" class="mb-2">
                <span class="fs-6 fw-semibold">Tutores</span>
            </a>

            <a href="#" class="text-decoration-none text-dark rounded-20 p-3 w-100 d-flex flex-column align-items-center">
                <img src="../assets/img/coordinador/grupos.png" alt="Grupos" width="40" class="mb-2">
                <span class="fs-6 fw-medium">Grupos</span>
            </a>

            <a href="#" class="text-decoration-none text-dark rounded-20 p-3 w-100 d-flex flex-column align-items-center">
                <img src="../assets/img/coordinador/asignacion.png" alt="Asignación" width="40" class="mb-2">
                <span class="fs-6 fw-medium">Asignación</span>
            </a>

            <a href="#" class="text-decoration-none text-dark rounded-20 p-3 w-100 d-flex flex-column align-items-center">
                <img src="../assets/img/coordinador/reportes.png" alt="Reportes" width="40" class="mb-2">
                <span class="fs-6 fw-medium">Reportes</span>
            </a>

            <a href="#" class="text-decoration-none text-dark rounded-20 p-3 w-100 d-flex flex-column align-items-center">
                <img src="../assets/img/coordinador/areas.png" alt="Áreas" width="40" class="mb-2">
                <span class="fs-6 fw-medium">Áreas</span>
            </a>
        </div>

        <div class="mt-auto w-100 text-center mb-3">
            <a href="#" class="text-decoration-none text-dark rounded-20 p-3 w-100 d-flex flex-column align-items-center">
                <img src="../assets/img/coordinador/perfil.png" alt="Perfil" width="40" class="mb-2">
                <span class="fs-6 fw-medium">Perfil</span>
            </a>
        </div>
    </div>

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="text-center mb-4 mt-2 fw-medium">Sistema de Gestión de Tutorías</h2>

        <div class="text-white text-center rounded-20 py-3 mb-4 shadow-sm" style="background-color: #008B74;">
            <h4 class="m-0 fw-semibold">Gestión de Tutores</h4>
        </div>

        <div class="d-flex justify-content-between align-items-end mb-3">
            <div>
                <label class="fw-semibold mb-1 fs-5">Buscar tutor</label>
                <div class="d-flex align-items-center bg-white border border-secondary px-3 py-2 rounded-20 shadow-sm" style="width: 350px;">
                    <img src="../assets/img/coordinador/buscar.png" width="20" alt="Buscar">
                    <input type="text" placeholder="Buscar por nombre" class="search-input border-0 bg-transparent w-100 ms-2 text-secondary">
                </div>
            </div>

            <div class="text-center">
                <label class="fw-semibold mb-1 fs-5 d-block">Nuevo Tutor</label>
                <button class="btn text-white rounded-20 px-4 shadow-sm fw-medium" style="background-color: #008B74;">Agregar</button>
            </div>
        </div>

        <table class="tabla-custom shadow-sm mb-auto">
            <thead>
            <tr>
                <th style="width: 8%;">Nomina</th>
                <th style="width: 25%;">Nombre</th>
                <th style="width: 25%;">Correo</th>
                <th style="width: 15%;">Teléfono</th>
                <th style="width: 12%;">Academia</th>
                <th style="width: 15%; text-align: center;">Acciones</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>1000</td>
                <td>Derick Axel Lagunes Ramirez</td>
                <td>dericklagunes@utez.edu.mx</td>
                <td>777 243 3456</td>
                <td>DATID</td>
                <td class="text-center">
                    <div class="d-flex justify-content-center gap-2">
                        <button class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></button>
                        <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                    </div>
                </td>
            </tr>
            <tr>
                <td>1001</td>
                <td>Nelida Baron Perez</td>
                <td>nelidaperez@utez.edu.mx</td>
                <td>777 759 2045</td>
                <td>DATID</td>
                <td class="text-center">
                    <div class="d-flex justify-content-center gap-2">
                        <button class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></button>
                        <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                    </div>
                </td>
            </tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td></tr>
            </tbody>
        </table>

        <div class="d-flex justify-content-end mt-4">
            <button class="btn text-white rounded-20 px-5 py-2 shadow-sm fw-medium fs-5" style="background-color: #008B74;">Guardar</button>
        </div>

    </div>

</div> <script src="../assets/js/bootstrap.js"></script>
</body>
</html>