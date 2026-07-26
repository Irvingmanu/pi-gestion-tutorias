<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Solicitud" %>
<%
    List<Solicitud> listaSolicitudes = (List<Solicitud>) request.getAttribute("listaSolicitudes");
    String error = (String) request.getAttribute("error");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "MX"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Solicitudes</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
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
        <a href="<%= request.getContextPath() %>/tutor/solicitudes.jsp" class="nav-item-grupos active">
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
            Solicitudes
        </div>

        <% if (error != null) { %>
        <div class="alert alert-danger" role="alert"><%= error %></div>
        <% } %>

        <!-- Lista de solicitudes -->
        <div class="form-wrap-figma" style="max-width: 100%;">
            <div id="listaSolicitudes">

                <% if (listaSolicitudes == null || listaSolicitudes.isEmpty()) { %>
                <div class="d-flex align-items-center justify-content-center p-4 bg-white rounded shadow-sm border text-muted">
                    No tienes solicitudes por ahora.
                </div>
                <% } else { %>
                <% for (Solicitud s : listaSolicitudes) { %>
                <div class="d-flex align-items-center justify-content-between p-3 mb-3 bg-white rounded shadow-sm border">
                    <div class="d-flex align-items-center gap-3">
                        <div class="bg-light rounded-circle d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                            <i class="bi bi-person fs-1"></i>
                        </div>
                        <div>
                            <p class="mb-0 fw-bold text-dark">
                                <%= s.getNombreAlumno() %> <%= s.getApellidosAlumno() %>
                            </p>
                            <p class="mb-0 small text-secondary">
                                <%= s.getAsunto() %>
                                <% if (s.getFechaPropuesta() != null) { %>
                                &middot; <%= formatoFecha.format(s.getFechaPropuesta()) %>
                                <% } %>
                            </p>
                        </div>
                    </div>

                    <div class="d-flex align-items-center gap-3">
                        <%
                            String badge;
                            switch (s.getEstatus()) {
                                case "Confirmada": badge = "success"; break;
                                case "Rechazada": badge = "danger"; break;
                                default: badge = "warning";
                            }
                        %>
                        <span class="badge text-bg-<%= badge %>"><%= s.getEstatus() %></span>

                        <a href="<%= request.getContextPath() %>/SolicitudServlet?accion=detalle&idSolicitud=<%= s.getIdSolicitud() %>"
                           class="btn-figma fw-medium px-4 py-2">Ver</a>
                    </div>
                </div>
                <% } %>
                <% } %>

            </div>

        </div>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
</body>
</html>
