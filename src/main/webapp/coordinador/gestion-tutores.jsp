<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String paginaActiva = "tutores";
    request.setAttribute("paginaActiva", paginaActiva);
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Tutores</title>
    <link href="<%= ctx %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/coordinador/gestion-tutores.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Gestión de Tutores
        </div>

        <!-- Buscar tutor / Nuevo Tutor -->
        <div class="row mb-3">
            <div class="col-12 d-flex justify-content-between align-items-end">
                <div>
                    <label class="campo-label fs-6" for="buscarTutor">Buscar tutor</label>
                    <input type="text" id="buscarTutor" class="campo-buscar campo-buscar-tutor"
                           placeholder="Buscar por nombre">
                </div>
                <div class="text-center">
                    <label class="campo-label fs-6">Nuevo Tutor</label>
                    <a href="formulario-tutor.jsp?accion=nueva" class="btn-figma">Agregar</a>
                </div>
            </div>
        </div>

        <!-- Tabla de tutores -->
        <div class="table-responsive mb-auto">
            <table class="tabla-grupos fs-6">
                <colgroup>
                    <col class="col-nomina">
                    <col class="col-nombre-t">
                    <col class="col-correo-t">
                    <col class="col-telefono">
                    <col class="col-academia">
                    <col class="col-acciones-t">
                </colgroup>
                <thead>
                <tr>
                    <th>Nomina</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Academia</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>1000</td>
                    <td>Derick Axel Lagunes Ramirez</td>
                    <td>dericklagunes@utez.edu.mx</td>
                    <td>777 243 3456</td>
                    <td>DATID</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-tutor.jsp?accion=editar" class="btn-accion btn-editar"><img src="<%= ctx %>/assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="<%= ctx %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>1001</td>
                    <td>Nelida Baron Perez</td>
                    <td>nelidaperez@utez.edu.mx</td>
                    <td>777 759 2045</td>
                    <td>DATID</td>
                    <td>
                        <div class="d-flex justify-content-center gap-2">
                            <a href="formulario-tutor.jsp?accion=editar" class="btn-accion btn-editar"><img src="<%= ctx %>/assets/img/coordinador/editar.png" width="16" alt="Editar"></a>
                            <button class="btn-accion btn-eliminar"><img src="<%= ctx %>/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar"></button>
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
        </div>

        <div class="d-flex justify-content-end mt-3">
            <button type="button" class="btn-figma">Guardar</button>
        </div>

    </div>

</div>

<script src="<%= ctx %>/assets/js/bootstrap.js"></script>
</body>
</html>