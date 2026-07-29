<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String paginaActiva = (String) request.getAttribute("paginaActiva");
    if (paginaActiva == null) {
        paginaActiva = "";
    }

    String ctx = request.getContextPath();
%>
<!-- ==================== BARRA LATERAL (Tutor) ==================== -->
<aside class="sidebar-grupos">
    <div class="sidebar-logo">
        <img src="<%= ctx %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
    </div>

    <a href="<%= ctx %>/tutor/registro-individual.jsp" class="nav-item-grupos<%= "individual".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/tutor/tutoriaIndividual.png" alt="Tutoría Individual">
        <span>Tutoría Individual</span>
    </a>
    <a href="<%= ctx %>/tutor/registro-grupal.jsp" class="nav-item-grupos<%= "grupal".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/tutor/tutoriaGrupal.png" alt="Tutoría Grupal">
        <span>Tutoría Grupal</span>
    </a>
    <a href="<%= ctx %>/SolicitudServlet" class="nav-item-grupos<%= "solicitudes".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/tutor/solicitudes.png" alt="Solicitudes">
        <span>Solicitudes</span>
    </a>
    <a href="<%= ctx %>/tutor/reportes.jsp" class="nav-item-grupos<%= "reportes".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/tutor/reportes.png" alt="Reportes">
        <span>Reportes</span>
    </a>
    <a href="<%= ctx %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto<%= "perfil".equals(paginaActiva) ? " active" : "" %>">
        <img src="<%= ctx %>/assets/img/tutor/perfil.png" alt="Perfil">
        <span>Perfil</span>
    </a>
</aside>
