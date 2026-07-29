<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String paginaActiva = (String) request.getAttribute("paginaActiva");
    if (paginaActiva == null) {
        paginaActiva = "";
    }

    String ctx = request.getContextPath();
%>
<!-- ==================== BARRA LATERAL (Alumno) ==================== -->
<aside class="sidebar-grupos">
    <div class="sidebar-logo">
        <img src="<%= ctx %>/assets/img/alumno/logo-utez.png" alt="UTEZ">
    </div>

    <a href="<%= ctx %>/AgendaServlet" class="nav-item-grupos<%= "agenda".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/alumno/calendario.png" alt="Agenda">
        <span>Agenda</span>
    </a>
    <a href="<%= ctx %>/SolicitudServlet?accion=nueva" class="nav-item-grupos<%= "solicitud".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/alumno/solicitud.png" alt="Solicitud">
        <span>Solicitud</span>
    </a>
    <a href="<%= ctx %>/SolicitudServlet?accion=historial" class="nav-item-grupos<%= "misSolicitudes".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/alumno/mis-solicitudes.png" alt="Mis Solicitudes">
        <span>Mis Solicitudes</span>
    </a>
    <a href="<%= ctx %>/alumno/acuerdos.jsp" class="nav-item-grupos<%= "acuerdos".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/alumno/acuerdos.png" alt="Acuerdos">
        <span>Acuerdos</span>
    </a>
    <a href="<%= ctx %>/alumno/perfil.jsp" class="nav-item-grupos mt-auto<%= "perfil".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/alumno/perfil.png" alt="Perfil">
        <span>Perfil</span>
    </a>
</aside>
