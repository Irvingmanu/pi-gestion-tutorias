<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    request.setAttribute("paginaActiva", "grupos");

    // accion=editar -> formulario de edicion (con valores precargados)
    // cualquier otro valor (o ausente) -> formulario de nuevo alumno (vacio)
    boolean esEdicion = "editar".equals(request.getParameter("accion"));

    String tituloBanner = esEdicion ? "Editar Alumno" : "Nuevo Alumno";

    //  valores de ejemplo para simular la carga de un alumno existente.
    // En una integracion real, estos vendrian de un bean/servicio consultado
    // por el id del alumno (ejemplo request.getParameter("id")).
    String valorNombres = esEdicion ? "Juan Pablo" : "";
    String valorApellidos = esEdicion ? "Hernández Cruz" : "";
    String valorCorreo = esEdicion ? "juanhernandez@utez.edu.mx" : "";
    String valorMatricula = esEdicion ? "20231234" : "";
    String valorCarrera = esEdicion ? "Desarrollo de Software Multiplataforma" : "";
    String valorGrupo = esEdicion ? "A" : "";
    String valorCuatrimestre = esEdicion ? "1°" : "";
    String valorGenero = esEdicion ? "Masculino" : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - <%= tituloBanner %></title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            <%= tituloBanner %>
        </div>

        <form class="form-wrap-figma mt-3" style="max-width: 720px;">

            <div class="row">

                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                        <input type="text" id="nombres" class="form-control form-control-figma w-100 fs-6"
                               value="<%= valorNombres %>" placeholder="Escribe los nombres">
                    </div>

                    <div class="mb-4">
                        <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                        <input type="text" id="apellidos" class="form-control form-control-figma w-100 fs-6"
                               value="<%= valorApellidos %>" placeholder="Escribe los apellidos">
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= valorCorreo %>" placeholder="Escribe el correo">
                    </div>

                    <div class="mb-4">
                        <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                        <input type="text" id="matricula" class="form-control form-control-figma w-100 fs-6"
                               value="<%= valorMatricula %>" placeholder="Escribe la matrícula">
                    </div>

                </div>

                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="carrera" class="form-label fs-6 fw-bold">Carrera</label>
                        <select id="carrera" class="form-select form-control-figma w-100 fs-6">
                            <option value="" <%= valorCarrera.isEmpty() ? "selected" : "" %>>Seleccione la carrera</option>
                            <option <%= "Desarrollo de Software Multiplataforma".equals(valorCarrera) ? "selected" : "" %>>Desarrollo de Software Multiplataforma</option>
                            <option <%= "Mantenimiento Industrial".equals(valorCarrera) ? "selected" : "" %>>Mantenimiento Industrial</option>
                            <option <%= "Tecnologías de la Información".equals(valorCarrera) ? "selected" : "" %>>Tecnologías de la Información</option>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="grupo" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="grupo" class="form-select form-control-figma w-100 fs-6">
                            <option value="" <%= valorGrupo.isEmpty() ? "selected" : "" %>>Seleccione el grupo</option>
                            <option <%= "A".equals(valorGrupo) ? "selected" : "" %>>A</option>
                            <option <%= "B".equals(valorGrupo) ? "selected" : "" %>>B</option>
                            <option <%= "C".equals(valorGrupo) ? "selected" : "" %>>C</option>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="cuatrimestre" class="form-label fs-6 fw-bold">Cuatrimestre</label>
                        <select id="cuatrimestre" class="form-select form-control-figma w-100 fs-6">
                            <option value="" <%= valorCuatrimestre.isEmpty() ? "selected" : "" %>>Seleccione el cuatrimestre</option>
                            <option <%= "1°".equals(valorCuatrimestre) ? "selected" : "" %>>1°</option>
                            <option <%= "2°".equals(valorCuatrimestre) ? "selected" : "" %>>2°</option>
                            <option <%= "3°".equals(valorCuatrimestre) ? "selected" : "" %>>3°</option>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="genero" class="form-label fs-6 fw-bold">Género</label>
                        <select id="genero" class="form-select form-control-figma w-100 fs-6">
                            <option value="" <%= valorGenero.isEmpty() ? "selected" : "" %>>Seleccione el género</option>
                            <option <%= "Masculino".equals(valorGenero) ? "selected" : "" %>>Masculino</option>
                            <option <%= "Femenino".equals(valorGenero) ? "selected" : "" %>>Femenino</option>
                        </select>
                    </div>

                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <a href="gestion-grupos.jsp" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2">Cancelar</a>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

    </div>

</div>

<script src="../assets/js/bootstrap.js"></script>
</body>
</html>
