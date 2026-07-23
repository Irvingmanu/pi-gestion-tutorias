<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    request.setAttribute("paginaActiva", "tutores");

    // accion=editar -> formulario de edicion (con valores precargados)
    // cualquier otro valor (o ausente) -> formulario de nuevo tutor (vacio)
    boolean esEdicion = "editar".equals(request.getParameter("accion"));

    String tituloBanner = esEdicion ? "Editar Tutor" : "Nuevo Tutor";

    // Valores de ejemplo para simular la carga de un tutor existente.
    // En una integracion real, estos vendrian de un bean/servicio consultado
    // por el id del tutor (ej. request.getParameter("id")).
    String valorNombres = esEdicion ? "Derick Axel" : "";
    String valorApellidos = esEdicion ? "Lagunes Ramirez" : "";
    String valorAcademia = esEdicion ? "DATID" : "";
    String valorTelefono = esEdicion ? "777 243 3456" : "";
    String valorCorreo = esEdicion ? "dericklagunes@utez.edu.mx" : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - <%= tituloBanner %></title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-tutores.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            <%= tituloBanner %>
        </div>

        <!-- Formulario de nuevo/edicion de tutor -->
        <form class="form-wrap-tutor mt-3">

            <div class="mb-4">
                <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                <input type="text" id="nombres" class="form-control form-control-tutor fs-6"
                       value="<%= valorNombres %>" placeholder="Escribe los nombres">
            </div>

            <div class="mb-4">
                <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                <input type="text" id="apellidos" class="form-control form-control-tutor fs-6"
                       value="<%= valorApellidos %>" placeholder="Escribe los apellidos">
            </div>

            <div class="mb-4">
                <label for="academia" class="form-label fs-6 fw-bold">Academía</label>
                <select id="academia" class="form-select form-control-figma fs-6 w-100">
                    <option value="" <%= valorAcademia.isEmpty() ? "selected" : "" %>>Seleccione la academia</option>
                    <option <%= "DATID".equals(valorAcademia) ? "selected" : "" %>>DATID</option>
                    <option <%= "Mantenimiento Industrial".equals(valorAcademia) ? "selected" : "" %>>Mantenimiento Industrial</option>
                    <option <%= "Tecnologías de la Información".equals(valorAcademia) ? "selected" : "" %>>Tecnologías de la Información</option>
                </select>
            </div>

            <div class="mb-4">
                <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                <input type="tel" id="telefono" class="form-control form-control-tutor fs-6"
                       value="<%= valorTelefono %>" placeholder="+52 ...">
            </div>

            <div class="mb-4">
                <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                <input type="email" id="correo" class="form-control form-control-tutor fs-6"
                       value="<%= valorCorreo %>" placeholder="Escribe el correo">
            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <a href="gestion-tutores.jsp" class="btn-cancelar-tutor fw-medium fs-5 px-4 py-2">Cancelar</a>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

    </div>

</div>

<script src="../assets/js/bootstrap.js"></script>
</body>
</html>
