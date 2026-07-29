<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Solicitud" %>
<%
    List<Solicitud> listaSolicitudes = (List<Solicitud>) request.getAttribute("listaSolicitudes");
    SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", new Locale("es", "MX"));
    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "MX"));

    request.setAttribute("paginaActiva", "misSolicitudes");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Mis Solicitudes</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-alumno.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Mis Solicitudes
        </div>

        <div class="form-wrap-figma" style="max-width: 100%;">

            <% if (listaSolicitudes == null || listaSolicitudes.isEmpty()) { %>
            <div class="p-4 text-center bg-white rounded shadow-sm border">
                <p class="fs-5 text-muted mb-0">No has enviado ninguna solicitud todavía.</p>
            </div>
            <% } else { %>
            <div class="d-flex flex-column gap-3">
                <% for (Solicitud s : listaSolicitudes) { %>
                <div class="bg-white rounded-figma shadow-sm border p-4">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="fw-bold fs-6"><%= s.getAsunto() %></div>
                            <div class="text-muted small mb-2">
                                Enviada el: <%= s.getFechaRegistro() != null ? formatoFechaHora.format(s.getFechaRegistro()) : "N/D" %>
                            </div>

                            <p class="mb-1">
                                <strong>Fecha propuesta:</strong>
                                <%= s.getFechaPropuesta() != null ? formatoFecha.format(s.getFechaPropuesta()) : "No especificada" %>
                            </p>
                            <p class="mb-1">
                                <strong>Hora de inicio:</strong>
                                <%= s.getHoraPropuesta() != null ? s.getHoraPropuesta() : "No especificada" %>
                            </p>
                            <p class="mb-0">
                                <strong>Duración:</strong>
                                <%= s.getDuracion() != null ? s.getDuracion() + " hora(s)" : "No especificada" %>
                            </p>
                        </div>

                        <%
                            String badge;
                            switch (s.getEstatus()) {
                                case "Confirmada": badge = "success"; break;
                                case "Rechazada": badge = "danger"; break;
                                case "Reprogramada": badge = "info"; break;
                                default: badge = "secondary";
                            }
                        %>
                        <span class="badge text-bg-<%= badge %> fs-6 px-3 py-2"><%= s.getEstatus() %></span>
                    </div>
                </div>
                <% } %>
            </div>
            <% } %>

        </div>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
</body>
</html>
