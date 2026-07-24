<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Solicitud" %>
<%
    Solicitud solicitud = (Solicitud) request.getAttribute("solicitud");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Detalle de Solicitud</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Tutor) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/tutor/registro-individual.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaIndividual.png" alt="Tutoría Individual">
            <span>Tutoría Individual</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/registro-grupal.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaGrupal.png" alt="Tutoría Grupal">
            <span>Tutoría Grupal</span>
        </a>
        <a href="<%= request.getContextPath() %>/SolicitudServlet" class="nav-item-grupos active">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/solicitudes.png" alt="Solicitudes">
            <span>Solicitudes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/reportes.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </aside>

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Detalle de Solicitud
        </div>

        <% if (solicitud == null) { %>
        <div class="alert alert-danger" role="alert">
            No se encontró la solicitud solicitada.
        </div>
        <% } else { %>

        <div class="form-wrap-figma" style="max-width: 100%;">
            <div class="bg-white rounded-figma shadow-sm border p-4">

                <div class="d-flex align-items-center gap-3 mb-4">
                    <div class="bg-light rounded-circle d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                        <i class="bi bi-person fs-1"></i>
                    </div>
                    <div>
                        <div class="fw-bold fs-5">
                            <%= solicitud.getNombreAlumno() %> <%= solicitud.getApellidosAlumno() %>
                        </div>
                        <div class="text-muted"><%= solicitud.getMatricula() %></div>
                    </div>
                    <div class="ms-auto">
                        <%
                            String badge;
                            switch (solicitud.getEstatus()) {
                                case "Confirmada": badge = "success"; break;
                                case "Rechazada": badge = "danger"; break;
                                default: badge = "warning";
                            }
                        %>
                        <span class="badge text-bg-<%= badge %> fs-6"><%= solicitud.getEstatus() %></span>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Asunto</label>
                    <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                        <%= solicitud.getAsunto() %>
                    </div>
                </div>

                <div class="mb-4">
                    <label class="form-label fw-bold">Descripción</label>
                    <div class="form-control form-control-figma" style="background-color:#f8f9fa; min-height: 100px;">
                        <%= solicitud.getDescripcion() %>
                    </div>
                </div>

                <% if ("Pendiente".equals(solicitud.getEstatus())) { %>
                <div class="d-flex justify-content-end gap-2">
                    <button type="button" class="btn-cancelar-figma fw-medium px-4 py-2" id="btnNegar">Negar</button>
                    <button type="button" class="btn-figma fw-medium px-4 py-2" id="btnAceptar">Aceptar</button>
                </div>
                <% } else { %>
                <div class="d-flex justify-content-end">
                    <a href="<%= request.getContextPath() %>/SolicitudServlet" class="btn-figma fw-medium px-4 py-2">Volver</a>
                </div>
                <% } %>

            </div>
        </div>

        <!-- Formularios ocultos: el servlet ya maneja accion=aceptar / accion=rechazar -->
        <form id="formAceptar" method="post" action="<%= request.getContextPath() %>/SolicitudServlet" class="d-none">
            <input type="hidden" name="accion" value="aceptar">
            <input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>">
        </form>

        <form id="formRechazar" method="post" action="<%= request.getContextPath() %>/SolicitudServlet" class="d-none">
            <input type="hidden" name="accion" value="rechazar">
            <input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>">
        </form>

        <% } %>

    </div>

</div>

<!-- ==================== MODALES DE ALERTA (incluye tal cual del proyecto) ==================== -->
<jsp:include page="/includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>

<% if (solicitud != null && "Pendiente".equals(solicitud.getEstatus())) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var modalEl = document.getElementById('modalConfirmacion');
        var modalConfirmacion = new bootstrap.Modal(modalEl);

        var tituloEl = document.getElementById('confirmacionTitulo');
        var mensajeEl = document.getElementById('confirmacionMensaje');
        var iconoEl = document.getElementById('confirmacionIcono');
        var btnAceptarModal = document.getElementById('btnConfirmacionAceptar');

        var accionPendiente = null; // 'aceptar' | 'rechazar'

        function abrirConfirmacion(accion) {
            accionPendiente = accion;

            if (accion === 'aceptar') {
                tituloEl.textContent = '¿Aceptar solicitud?';
                mensajeEl.textContent = 'El alumno será notificado de que su tutoría fue confirmada.';
                iconoEl.src = iconoEl.getAttribute('data-base-path') + 'exito.png';
            } else {
                tituloEl.textContent = '¿Negar solicitud?';
                mensajeEl.textContent = 'El alumno será notificado de que su solicitud fue rechazada.';
                iconoEl.src = iconoEl.getAttribute('data-base-path') + 'advertencia.png';
            }

            modalConfirmacion.show();
        }

        document.getElementById('btnAceptar').addEventListener('click', function () {
            abrirConfirmacion('aceptar');
        });

        document.getElementById('btnNegar').addEventListener('click', function () {
            abrirConfirmacion('rechazar');
        });

        btnAceptarModal.addEventListener('click', function () {
            if (accionPendiente === 'aceptar') {
                document.getElementById('formAceptar').submit();
            } else if (accionPendiente === 'rechazar') {
                document.getElementById('formRechazar').submit();
            }
        });
    });
</script>
<% } %>

</body>
</html>
