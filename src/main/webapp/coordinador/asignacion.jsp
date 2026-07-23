<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    request.setAttribute("paginaActiva", "asignacion");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Asignación de Tutores</title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="../assets/css/coordinador/asignacion.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Asignación de Tutores
        </div>

        <!-- Formulario de asignacion -->
        <form class="asignacion-form-wrap mt-3">

            <div class="mb-4">
                <label class="campo-label fs-6" for="buscarTutor">Buscar tutor</label>
                <input type="text" id="buscarTutor" class="campo-buscar"
                       placeholder="Buscar por nombre o apellido">
            </div>

            <div class="mb-4">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" class="form-select campo-select">
                    <option selected>Seleccione la carrera</option>
                    <option>Desarrollo de Software Multiplataforma</option>
                    <option>Mantenimiento Industrial</option>
                    <option>Tecnologías de la Información</option>
                </select>
            </div>

            <div class="mb-4">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="form-select campo-select">
                    <option selected>Seleccione el cuatrimestre</option>
                    <option>1°</option>
                    <option>2°</option>
                    <option>3°</option>
                </select>
            </div>

            <div class="mb-4">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="form-select campo-select">
                    <option selected>Seleccione el Grupo</option>
                    <option>A</option>
                    <option>B</option>
                    <option>C</option>
                </select>
            </div>

            <div class="text-center mt-4">
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Agregar</button>
            </div>

        </form>

    </div>

</div>

<script src="../assets/js/bootstrap.js"></script>
</body>
</html>
