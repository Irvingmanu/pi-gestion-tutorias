<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    // Cada vista debe definir `paginaActiva` (y publicarla via request.setAttribute)
    // antes de incluir este navbar, para que el item correspondiente reciba
    // la clase "active" (rectángulo con radius:20px definido en gestion-grupos.css).
    String paginaActiva = (String) request.getAttribute("paginaActiva");
    if (paginaActiva == null) {
        paginaActiva = "";
    }

    String ctx = request.getContextPath();
%>
<aside class="sidebar-grupos">
    <div class="sidebar-logo">
        <img src="<%= ctx %>/assets/img/coordinador/logo-utez.png" alt="UTEZ">
    </div>

    <a href="<%= ctx %>/coordinador/gestion-tutores.jsp" class="nav-item-grupos<%= "tutores".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/coordinador/tutores.png" alt="Tutores">
        <span>Tutores</span>
    </a>
    <a href="<%= ctx %>/AlumnoServlet" class="nav-item-grupos<%= "grupos".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/coordinador/grupos.png" alt="Grupos">
        <span>Grupos</span>
    </a>
    <a href="<%= ctx %>/asignacion" class="nav-item-grupos<%= "asignacion".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/coordinador/asignacion.png" alt="Asignación">
        <span>Asignación</span>
    </a>
    <a href="<%= ctx %>/coordinador/reportes.jsp" class="nav-item-grupos<%= "reportes".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/coordinador/reportes.png" alt="Reportes">
        <span>Reportes</span>
    </a>
    <a href="<%= ctx %>/coordinador/areas-apoyo.jsp" class="nav-item-grupos<%= "areas".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/coordinador/areas.png" alt="Áreas">
        <span>Áreas</span>
    </a>
    <a href="<%= ctx %>/PerfilServlet" class="nav-item-grupos mt-auto<%= "perfil".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/coordinador/perfil.png" alt="Perfil">
        <span>Perfil</span>
    </a>
</aside>
