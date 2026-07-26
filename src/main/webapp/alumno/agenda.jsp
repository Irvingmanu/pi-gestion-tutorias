<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Agenda</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Alumno) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/logo-utez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/AgendaServlet?accion=agenda&matricula=20253DS076&idLetraGrupo=3" class="nav-item-grupos active">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/calendario.png" alt="Agenda">
            <span>Agenda</span>
        </a>
        <a href="<%= request.getContextPath() %>/alumno/solicitud.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/solicitud.png" alt="Solicitud">
            <span>Solicitud</span>
        </a>
        <a href="<%= request.getContextPath() %>/alumno/acuerdos.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/acuerdos.png" alt="Acuerdos">
            <span>Acuerdos</span>
        </a>
        <a href="<%= request.getContextPath() %>/alumno/perfil.jsp" class="nav-item-grupos mt-auto">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </aside>

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Agenda MAYO-AGOSTO 2026
        </div>

        <!-- Lista de eventos de la agenda -->
        <div class="form-wrap-figma" style="max-width: 100%;">

            <c:choose>
                <c:when test="${empty listaEventosAgenda}">
                    <div class="p-4 text-center bg-white rounded shadow-sm border">
                        <p class="fs-5 text-muted mb-0">No hay tutorías programadas en tu agenda.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${listaEventosAgenda}" var="evento">
                        <div class="d-flex align-items-center justify-content-between p-3 mb-3 bg-white rounded shadow-sm border">
                            <div class="d-flex align-items-center gap-3">
                                <div class="d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                                    <c:choose>
                                        <c:when test="${evento.tipo == 'Individual'}">
                                            <i class="bi bi-person fs-1 text-primary"></i>
                                        </c:when>
                                        <c:otherwise>
                                            <i class="bi bi-people fs-1 text-success"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div>
                                    <div class="fw-bold">Tutoría ${evento.tipo}</div>
                                    <div>${evento.descripcion}</div>
                                    <div class="text-muted">${evento.fechaFormateada}</div>
                                </div>
                            </div>
                            <span class="fw-bold ${evento.estado == 'Próximo' ? 'text-secondary' : 'text-success'}">
                                    ${evento.estado}
                            </span>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>

        </div>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
</body>
</html>