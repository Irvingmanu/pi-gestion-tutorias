<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%
    request.setAttribute("paginaActiva", "areas");

    Area areaEdit = (Area) request.getAttribute("areaEdit");
    boolean esEdicion = (areaEdit != null);

    String errorMsg = (String) session.getAttribute("errorSession");
    if (errorMsg != null) {
        session.removeAttribute("errorSession");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - <%= esEdicion ? "Editar Área" : "Nueva Área" %></title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="../assets/css/coordinador/areas-apoyo.css" rel="stylesheet">

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <style>
        .form-container {
            max-width: 600px;
            margin: 0 auto;
        }
        .rounded-input {
            border-radius: 20px;
            border: 1px solid #000;
            padding: 10px 20px;
        }
        .btn-custom-cancel {
            background-color: #c95151;
            color: white;
            border-radius: 20px;
            padding: 10px 30px;
            border: none;
            transition: background-color 0.2s ease;
        }
        .btn-custom-cancel:hover {
            background-color: #b04343;
            color: white;
        }
        .btn-custom-save {
            background-color: #007f66;
            color: white;
            border-radius: 20px;
            padding: 10px 30px;
            border: none;
            transition: background-color 0.2s ease;
        }
        .btn-custom-save:hover {
            background-color: #006652;
            color: white;
        }

        /* FUERZA PARA EL MODAL EN ROJO Y CON REDONDEADO DE 20px */
        .swal2-styled.swal2-confirm {
            background-color: #c95151 !important;
            border-color: #c95151 !important;
            border-radius: 20px !important;
            padding: 10px 50px !important;
            font-weight: 500 !important;
            box-shadow: none !important;
        }
        .swal2-icon.swal2-error {
            border-color: #c95151 !important;
            color: #c95151 !important;
        }
        .swal2-icon.swal2-error [class^='swal2-x-mark-line'] {
            background-color: #c95151 !important;
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column align-items-center">

        <h2 class="titulo-principal h5 mb-4 mt-2 text-center">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-5 w-100 text-center" style="background-color: #007f66; color: white; border-radius: 20px; padding: 12px;">
            <%= esEdicion ? "Editar área de Apoyo" : "Nueva área de Apoyo" %>
        </div>

        <div class="w-100 form-container">
            <form action="<%= request.getContextPath() %>/AreaServlet" method="POST" class="d-flex flex-column gap-4">

                <input type="hidden" name="accion" value="<%= esEdicion ? "editar" : "registrar" %>">
                <input type="hidden" name="idArea" value="<%= esEdicion ? areaEdit.getIdArea() : (request.getParameter("idArea") != null ? request.getParameter("idArea") : "") %>">

                <div class="form-group">
                    <label class="fw-bold mb-2">Nombre Área</label>
                    <input type="text" name="nombreArea"
                           value="<%= (request.getParameter("nombreArea") != null) ? request.getParameter("nombreArea") : (esEdicion ? areaEdit.getNombre() : "") %>"
                           class="form-control rounded-input shadow-sm">
                </div>

                <div class="form-group">
                    <label class="fw-bold mb-2">Encargado</label>
                    <input type="text" name="encargado"
                           value="<%= (request.getParameter("encargado") != null) ? request.getParameter("encargado") : (esEdicion ? areaEdit.getEncargado() : "") %>"
                           class="form-control rounded-input shadow-sm">
                </div>

                <div class="form-group">
                    <label class="fw-bold mb-2">Correo</label>
                    <input type="text" name="correo"
                           value="<%= (request.getParameter("correo") != null) ? request.getParameter("correo") : (esEdicion ? areaEdit.getCorreoContacto() : "") %>"
                           class="form-control rounded-input shadow-sm">
                </div>

                <div class="d-flex justify-content-center gap-3 mt-4">
                    <a href="areas-apoyo.jsp" class="btn btn-custom-cancel fw-medium px-4 text-decoration-underline">Cancelar</a>
                    <button type="submit" class="btn btn-custom-save fw-medium px-4">Guardar</button>
                </div>

            </form>
        </div>
    </div>
</div>

<script src="../assets/js/bootstrap.js"></script>

<% if (errorMsg != null) { %>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        Swal.fire({
            title: 'Error',
            text: '<%= errorMsg %>',
            icon: 'error',
            confirmButtonText: 'OK',
            buttonsStyling: true
        });
    });
</script>
<% } %>

</body>
</html>