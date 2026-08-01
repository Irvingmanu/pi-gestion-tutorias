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
            data-bs-target="#sidebarOffcanvasCoordinador" aria-controls="sidebarOffcanvasCoordinador" aria-label="Abrir menú">
        <img src="<%= ctx %>/assets/img/menu.png" alt="" class="mobile-topbar-menu-icon icono-abrir">
        <img src="<%= ctx %>/assets/img/ocultarmenu.png" alt="" class="mobile-topbar-menu-icon icono-cerrar">
    </button>
    <img src="<%= ctx %>/assets/img/coordinador/logo-utez.png" alt="UTEZ" class="mobile-topbar-logo">
</nav>

<aside class="offcanvas-md offcanvas-start" tabindex="-1" id="sidebarOffcanvasCoordinador" aria-label="Menú de navegación">
    <div class="offcanvas-body sidebar-grupos">
        <div class="sidebar-logo d-none d-md-flex">
            <img src="<%= ctx %>/assets/img/coordinador/logo-utez.png" alt="UTEZ">
        </div>

        <a href="<%= ctx %>/gestion-tutores" class="nav-item-grupos<%= "tutores".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/coordinador/tutores.png" alt="Tutores">
            <span>Tutores</span>
        </a>
        <a href="<%= ctx %>/gestion-grupos" class="nav-item-grupos<%= "grupos".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/coordinador/grupos.png" alt="Grupos">
            <span>Grupos</span>
        </a>
        <a href="<%= ctx %>/asignacion" class="nav-item-grupos<%= "asignacion".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/coordinador/asignacion.png" alt="Asignación">
            <span>Asignación</span>
        </a>
        <a href="<%= ctx %>/coordinador/reportes-globales.jsp" class="nav-item-grupos<%= "reportes".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/coordinador/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="<%= ctx %>/coordinador/areas-apoyo.jsp" class="nav-item-grupos<%= "areas".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/coordinador/areas.png" alt="Áreas">
            <span>Áreas</span>
        </a>
        <a href="<%= ctx %>/perfil" class="nav-item-grupos mt-auto<%= "perfil".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/coordinador/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </div>
</aside>