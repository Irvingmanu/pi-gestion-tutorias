<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Alumno" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Horario" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AsignacionTutorDao" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.HorarioDao" %>
<%
    // TODO: mover esto a un doGet del SolicitudServlet cuando se quiera
    // seguir el patrón MVC completo; por ahora se hace aquí porque el
    // enlace del sidebar entra directo a este JSP.

    Integer idUsuarioSesion = (Integer) session.getAttribute("idUsuario");
    if (idUsuarioSesion == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Alumno alumnoSesion = new AlumnoDAO().getByIdUsuario(idUsuarioSesion);
    List<Horario> listaHorarios = new java.util.ArrayList<>();
    Integer idTutorAsignado = null;

    if (alumnoSesion != null) {
        idTutorAsignado = new AsignacionTutorDao().findIdTutorByGrupoYCuatrimestre(
                alumnoSesion.getIdLetraGrupo(), alumnoSesion.getIdCuatrimestre());

        if (idTutorAsignado != null) {
            listaHorarios = new HorarioDao().findDisponiblesByTutor(idTutorAsignado);
        }
    }

    boolean puedeEnviar = (idTutorAsignado != null && !listaHorarios.isEmpty());
    String exito = request.getParameter("exito");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Solicitud</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Alumno) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/logo-utez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/alumno/agenda.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/alumno/calendario.png" alt="Agenda">
            <span>Agenda</span>
        </a>
        <a href="<%= request.getContextPath() %>/alumno/solicitud.jsp" class="nav-item-grupos active">
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
            Solicitud
        </div>

        <% if ("enviada".equals(exito)) { %>
        <div class="alert alert-success" role="alert">
            Tu solicitud fue enviada correctamente. El tutor la revisará pronto.
        </div>
        <% } else if ("error".equals(exito)) { %>
        <div class="alert alert-danger" role="alert">
            Ocurrió un error al enviar tu solicitud. Intenta de nuevo.
        </div>
        <% } %>

        <div class="form-wrap-figma" style="max-width: 50%;">

            <form method="post" action="<%= request.getContextPath() %>/SolicitudServlet" id="formSolicitud">
                <input type="hidden" name="accion" value="crear">

                <div class="mb-3">
                    <label for="asunto" class="form-label fw-bold">Asunto</label>
                    <input type="text" class="form-control form-control-figma" id="asunto" name="asunto"
                           maxlength="150" required>
                </div>

                <div class="mb-3">
                    <label for="idHorario" class="form-label fw-bold">Programar Tutoría individual</label>
                    <% if (idTutorAsignado == null) { %>
                    <div class="alert alert-warning mb-0" role="alert">
                        Todavía no tienes un tutor asignado, así que no puedes elegir horario.
                        Consulta con el coordinador.
                    </div>
                    <% } else if (listaHorarios.isEmpty()) { %>
                    <div class="alert alert-warning mb-0" role="alert">
                        Tu tutor todavía no tiene horarios de atención registrados.
                    </div>
                    <% } else { %>
                    <select class="form-select form-control-figma" id="idHorario" name="idHorario" required>
                        <option value="" selected disabled>Seleccione horario disponible</option>
                        <% for (Horario h : listaHorarios) { %>
                        <option value="<%= h.getIdHorario() %>"><%= h.getEtiqueta() %></option>
                        <% } %>
                    </select>
                    <% } %>
                </div>

                <div class="mb-4">
                    <label for="descripcion" class="form-label fw-bold">Descripción</label>
                    <textarea class="form-control form-control-figma" id="descripcion" name="descripcion"
                              rows="4" required></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <a href="<%= request.getContextPath() %>/alumno/agenda.jsp"
                       class="btn btn-cancelar-figma">Cancelar</a>
                    <button type="submit" class="btn btn-figma" id="btnEnviarSolicitud">Enviar</button>
                </div>
            </form>

        </div>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var puedeEnviar = <%= puedeEnviar %>;
        var form = document.getElementById('formSolicitud');

        form.addEventListener('submit', function (evento) {
            if (!puedeEnviar) {
                evento.preventDefault();
                mostrarAlerta(
                    'advertencia',
                    'No puedes enviar tu solicitud',
                    'Todavía no tienes un tutor asignado o tu tutor no tiene horarios de atención registrados. Consulta con el coordinador.'
                );
            }
        });
    });
</script>
</body>
</html>
