<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String paginaActiva = "grupos";
    request.setAttribute("paginaActiva", paginaActiva);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Grupos</title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Gestión de Grupos
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-5">
                <label class="campo-label fs-6" for="buscarAlumno">Buscar Alumno</label>
                <input type="text" id="buscarAlumno" class="campo-buscar"
                       placeholder="Buscar por nombre o apellido">
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" class="campo-select">
                    <option selected>Seleccione la carrera</option>
                    <option>Desarrollo de Software Multiplataforma</option>
                    <option>Mantenimiento Industrial</option>
                    <option>Tecnologías de la Información</option>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-3 align-items-end">
            <div class="col-md-5">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="campo-select">
                    <option selected>Seleccione el Grupo</option>
                    <option>A</option>
                    <option>B</option>
                    <option>C</option>
                </select>
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="campo-select">
                    <option selected>Seleccione el cuatrimestre</option>
                    <option>1°</option>
                    <option>2°</option>
                    <option>3°</option>
                </select>
            </div>
            <div class="col-md-2 text-center">
                <label class="campo-label fs-6">Nuevo Alumno</label>
                <a href="formulario-alumno.jsp?accion=nueva" class="btn-figma">Agregar</a>
            </div>
        </div>

        <div class="table-responsive mb-auto">
            <table class="tabla-grupos fs-6">
                <colgroup>
                    <col class="col-matricula">
                    <col class="col-nombre">
                    <col class="col-correo">
                    <col class="col-genero">
                    <col class="col-carrera">
                    <col class="col-cuatri">
                    <col class="col-acciones">
                </colgroup>
                <thead>
                <tr>
                    <th>Matricula</th>
                    <th>Nombre Completo</th>
                    <th>Correo</th>
                    <th>Genero</th>
                    <th>Carrera</th>
                    <th>Cuatri/Grupo</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>20213TI045</td>
                    <td>Alejandro Ramírez Silva</td>
                    <td>20213TI045@utez.edu.mx</td>
                    <td>Masculino</td>
                    <td>DSM</td>
                    <td>3° C</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-alumno.jsp?accion=editar" class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>20221MN112</td>
                    <td>Valeria García López</td>
                    <td>20221MN112@utez.edu.mx</td>
                    <td>Femenino</td>
                    <td>DSM</td>
                    <td>3° C</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-alumno.jsp?accion=editar" class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>20231MC089</td>
                    <td>Roberto Carlos Hernández Díaz</td>
                    <td>20231MC089@utez.edu.mx</td>
                    <td>Masculino</td>
                    <td>DSM</td>
                    <td>3° C</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-alumno.jsp?accion=editar" class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>20213DT022</td>
                    <td>Mariana Torres Venegas</td>
                    <td>20213DT022@utez.edu.mx</td>
                    <td>Femenino</td>
                    <td>DSM</td>
                    <td>3° C</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-alumno.jsp?accion=editar" class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>20223IN056</td>
                    <td>Daniel Sánchez Castro</td>
                    <td>20223IN056@utez.edu.mx</td>
                    <td>Masculino</td>
                    <td>DSM</td>
                    <td>3° C</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-alumno.jsp?accion=editar" class="btn-accion btn-editar"><img src="../assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="../assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                        </div>
                    </td>
                </tr>
                <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
                <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
                <tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
                </tbody>
            </table>
        </div>

        <div class="d-flex justify-content-end mt-3">
            <button type="button" class="btn-figma">Guardar</button>
        </div>

    </div>

</div>

<script src="../assets/js/bootstrap.js"></script>
</body>
</html>
