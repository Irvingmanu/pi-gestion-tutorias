<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!-- ==================== BARRA SUPERIOR MOVIL (solo <768px) ==================== -->
<nav class="navbar mobile-topbar d-md-none">
    <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas"
            data-bs-target="#sidebarOffcanvasCoordinador" aria-controls="sidebarOffcanvasCoordinador" aria-label="Abrir menú">
        <img src="${pageContext.request.contextPath}/assets/img/menu.png" alt="" class="mobile-topbar-menu-icon icono-abrir">
        <img src="${pageContext.request.contextPath}/assets/img/ocultarmenu.png" alt="" class="mobile-topbar-menu-icon icono-cerrar">
    </button>
    <img src="${pageContext.request.contextPath}/assets/img/coordinador/logo-utez.png" alt="UTEZ" class="mobile-topbar-logo">
</nav>

<aside class="offcanvas-md offcanvas-start" tabindex="-1" id="sidebarOffcanvasCoordinador" aria-label="Menú de navegación">
    <div class="offcanvas-body sidebar-grupos">
        <div class="sidebar-logo d-none d-md-flex">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/logo-utez.png" alt="UTEZ">
        </div>

        <a href="${pageContext.request.contextPath}/gestion-tutores" class="nav-item-grupos${paginaActiva == 'tutores' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/tutores.png" alt="Tutores">
            <span>Tutores</span>
        </a>
        <a href="${pageContext.request.contextPath}/gestion-grupos" class="nav-item-grupos${paginaActiva == 'grupos' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/grupos.png" alt="Grupos">
            <span>Grupos</span>
        </a>
        <a href="${pageContext.request.contextPath}/asignacion" class="nav-item-grupos${paginaActiva == 'asignacion' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/asignacion.png" alt="Asignación">
            <span>Asignación</span>
        </a>
        <a href="${pageContext.request.contextPath}/reportes-globales" class="nav-item-grupos${paginaActiva == 'reportes' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="${pageContext.request.contextPath}/areas-apoyo" class="nav-item-grupos${paginaActiva == 'areas' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/areas.png" alt="Áreas">
            <span>Áreas</span>
        </a>
        <a href="${pageContext.request.contextPath}/gestion-periodos" class="nav-item-grupos${paginaActiva == 'periodos' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/periodoEsco.png" alt="Periodos Escolares">
            <span>Periodos Escolares</span>
        </a>
        <a href="${pageContext.request.contextPath}/perfil" class="nav-item-grupos mt-auto${paginaActiva == 'perfil' ? ' active' : ''}">
            <img src="${pageContext.request.contextPath}/assets/img/coordinador/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </div>
</aside>