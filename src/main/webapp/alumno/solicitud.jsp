<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="paginaActiva" value="solicitud" scope="request" />
<%-- Los datos de disponibilidad ya vienen calculados por SolicitudServlet (accion=nueva),
     que es el unico punto de entrada a esta vista. "empty" cubre tanto listaHorarios == null
     como una lista vacia, igual que la validacion original en Java. --%>
<c:set var="puedeEnviar" value="${not empty idTutorAsignado and not empty listaHorarios}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Solicitud</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-alumno.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Solicitud
        </div>

        <c:if test="${param.exito == 'enviada'}">
            <div class="alert alert-success" role="alert">
                Tu solicitud fue enviada correctamente. El tutor la revisará pronto.
            </div>
        </c:if>
        <c:if test="${param.exito == 'error'}">
            <div class="alert alert-danger" role="alert">
                Ocurrió un error al enviar tu solicitud. Intenta de nuevo.
            </div>
        </c:if>

        <div class="form-wrap-figma" style="max-width: 50%;">

            <form method="post" action="${pageContext.request.contextPath}/solicitudes" id="formSolicitud">
                <input type="hidden" name="accion" value="crear">

                <div class="mb-3">
                    <label for="asunto" class="form-label fw-bold">Asunto</label>
                    <input type="text" class="form-control form-control-figma" id="asunto" name="asunto"
                           maxlength="150" required>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label for="fechaPropuesta" class="form-label fw-bold">Día</label>
                        <select id="fechaPropuesta" name="fechaPropuesta" class="form-select form-control-figma" required>
                            <option value="" selected disabled>Seleccione un día</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="duracionPropuesta" class="form-label fw-bold">Duración</label>
                        <select id="duracionPropuesta" name="duracion" class="form-select form-control-figma" required disabled>
                            <option value="" selected disabled>Seleccione un día primero</option>
                            <option value="1">1 hora</option>
                            <option value="2">2 horas</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="horaPropuesta" class="form-label fw-bold">Hora de inicio</label>
                        <select id="horaPropuesta" name="horaPropuesta" class="form-select form-control-figma" required disabled>
                            <option value="" selected disabled>Seleccione la duración primero</option>
                        </select>
                    </div>
                </div>

                <div class="mb-4">
                    <label for="descripcion" class="form-label fw-bold">Descripción</label>
                    <textarea class="form-control form-control-figma" id="descripcion" name="descripcion"
                              rows="4" required></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/agenda"
                       class="btn btn-cancelar-figma">Cancelar</a>
                    <button type="submit" class="btn btn-figma" id="btnEnviarSolicitud">Enviar</button>
                </div>
            </form>

        </div>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>

<!-- Puente entre EL y el JS externo: valores calculados por el servidor -->
<script>
    window.PUEDE_ENVIAR = ${puedeEnviar};
    // Disponibilidad real de las próximas 2 semanas, calculada en
    // SolicitudServlet.construirDisponibilidadJson (cruza HORARIO_ATENCION
    // del tutor con las horas ya ocupadas en SOLICITUD_TUTORIA/SESION_INDIVIDUAL).
    window.DISPONIBILIDAD = ${empty disponibilidadJson ? '{}' : disponibilidadJson};
</script>
<script src="${pageContext.request.contextPath}/assets/js/alumno/solicitud.js"></script>
</body>
</html>