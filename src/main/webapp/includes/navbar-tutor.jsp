<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String paginaActiva = (String) request.getAttribute("paginaActiva");
    if (paginaActiva == null) {
        paginaActiva = "";
    }

    String ctx = request.getContextPath();
%>
<!-- ==================== BARRA SUPERIOR MOVIL (solo <768px) ==================== -->
<nav class="navbar mobile-topbar d-md-none">
    <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas"
            data-bs-target="#sidebarOffcanvasTutor" aria-controls="sidebarOffcanvasTutor" aria-label="Abrir menú">
        <img src="<%= ctx %>/assets/img/menu.png" alt="" class="mobile-topbar-menu-icon icono-abrir">
        <img src="<%= ctx %>/assets/img/ocultarmenu.png" alt="" class="mobile-topbar-menu-icon icono-cerrar">
    </button>
    <img src="<%= ctx %>/assets/img/tutor/logoUtez.png" alt="UTEZ" class="mobile-topbar-logo">
</nav>

<!-- ==================== BARRA LATERAL (Tutor) ==================== -->
<aside class="offcanvas-md offcanvas-start" tabindex="-1" id="sidebarOffcanvasTutor" aria-label="Menú de navegación">
    <div class="offcanvas-body sidebar-grupos">
        <div class="sidebar-logo d-none d-md-flex">
            <img src="<%= ctx %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>

        <a href="<%= ctx %>/tutoria-individual" class="nav-item-grupos<%= "individual".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/tutor/tutoriaIndividual.png" alt="Tutoría Individual">
            <span>Tutoría Individual</span>
        </a>
        <a href="<%= ctx %>/tutoria-grupal" class="nav-item-grupos<%= "grupal".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/tutor/tutoriaGrupal.png" alt="Tutoría Grupal">
            <span>Tutoría Grupal</span>
        </a>
        <a href="<%= ctx %>/solicitudes" class="nav-item-grupos<%= "solicitudes".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/tutor/solicitudes.png" alt="Solicitudes">
            <span>Solicitudes</span>
        </a>
        <a href="<%= ctx %>/ReportesServlet" class="nav-item-grupos<%= "reportes".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/tutor/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="<%= ctx %>/historial-tutorias" class="nav-item-grupos<%= "historial".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/tutor/historial.png" alt="Historial">
            <span>Historial</span>
        </a>
        <a href="<%= ctx %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto<%= "perfil".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/tutor/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>

    </div>
</aside>

<script>
    (function () {
        var navType = "";
        var navEntries = performance.getEntriesByType("navigation");

        if (navEntries.length > 0) {
            navType = navEntries[0].type;
        } else if (performance.navigation) {
            navType = (performance.navigation.type === 2) ? "back_forward" : "";
        }

        if (navType === "back_forward") {
            window.location.replace("<%= ctx %>/logout");
        }
    })();
</script>