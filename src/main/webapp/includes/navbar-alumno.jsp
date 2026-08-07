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
            data-bs-target="#sidebarOffcanvasAlumno" aria-controls="sidebarOffcanvasAlumno" aria-label="Abrir menú">
        <img src="<%= ctx %>/assets/img/menu.png" alt="" class="mobile-topbar-menu-icon icono-abrir">
        <img src="<%= ctx %>/assets/img/ocultarmenu.png" alt="" class="mobile-topbar-menu-icon icono-cerrar">
    </button>
    <img src="<%= ctx %>/assets/img/alumno/logo-utez.png" alt="UTEZ" class="mobile-topbar-logo">
</nav>

<!-- ==================== BARRA LATERAL (Alumno) ==================== -->
<aside class="offcanvas-md offcanvas-start" tabindex="-1" id="sidebarOffcanvasAlumno" aria-label="Menú de navegación">
    <div class="offcanvas-body sidebar-grupos">
        <div class="sidebar-logo d-none d-md-flex">
            <img src="<%= ctx %>/assets/img/alumno/logo-utez.png" alt="UTEZ">
        </div>

        <a href="<%= ctx %>/agenda" class="nav-item-grupos<%= "agenda".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/alumno/calendario.png" alt="Agenda">
            <span>Agenda</span>
        </a>
        <a href="<%= ctx %>/solicitudes?accion=nueva" class="nav-item-grupos<%= "solicitud".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/alumno/solicitud.png" alt="Solicitud">
            <span>Solicitud</span>
        </a>
        <a href="<%= ctx %>/solicitudes?accion=historial" class="nav-item-grupos<%= "misSolicitudes".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/alumno/mis-solicitudes.png" alt="Mis Solicitudes">
            <span>Mis Solicitudes</span>
        </a>
        <a href="<%= ctx %>/alumno/acuerdos" class="nav-item-grupos<%= "acuerdos".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/alumno/acuerdos.png" alt="Acuerdos">
            <span>Acuerdos</span>
        </a>
        <a href="<%= ctx %>/alumno/canalizaciones" class="nav-item-grupos<%= "canalizaciones".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/alumno/canalizaciones.png" alt="Canalizaciones">
            <span>Canalizaciones</span>
        </a>
        <a href="<%= ctx %>/alumno/perfil.jsp" class="nav-item-grupos mt-auto<%= "perfil".equals(paginaActiva) ? " active" : "" %>">
            <img src="<%= ctx %>/assets/img/alumno/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </div>
</aside>
