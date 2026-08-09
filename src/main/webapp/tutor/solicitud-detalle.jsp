<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Detalle de Solicitud</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Detalle de Solicitud
        </div>

        <c:choose>
            <c:when test="${empty solicitud}">
                <div class="alert alert-danger" role="alert">
                    No se encontró la solicitud solicitada.
                </div>
            </c:when>
            <c:otherwise>

                <div class="form-wrap-figma" style="max-width: 100%;">
                    <div class="bg-white rounded-figma shadow-sm border p-4">

                        <div class="d-flex align-items-center gap-3 mb-4">
                            <div class="bg-light rounded-circle d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                                <i class="bi bi-person fs-1"></i>
                            </div>
                            <div>
                                <div class="fw-bold fs-5">
                                        ${solicitud.nombreAlumno} ${solicitud.apellidosAlumno}
                                </div>
                                <div class="text-muted">${solicitud.matricula}</div>
                            </div>
                            <div class="ms-auto">
                                <span class="badge text-bg-${badgeColor} fs-6">${solicitud.estatus}</span>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Asunto</label>
                            <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                                    ${solicitud.asunto}
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Fecha Propuesta</label>
                            <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                                <c:choose>
                                    <c:when test="${not empty fechaPropuestaFormateada}">
                                        ${fechaPropuestaFormateada}
                                        <c:if test="${not empty solicitud.horaPropuesta}"> - ${solicitud.horaPropuesta}</c:if>
                                        <c:if test="${not empty solicitud.duracion}">
                                            (${solicitud.duracion} ${solicitud.duracion == 1 ? 'hora' : 'horas'})
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        No especificada
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <c:if test="${solicitud.estatus == 'Reprogramada' and not empty nuevaFechaFormateada}">
                            <div class="mb-3">
                                <label class="form-label fw-bold">Nueva Fecha Propuesta</label>
                                <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                                        ${nuevaFechaFormateada}
                                    <c:if test="${not empty solicitud.nuevaHora}"> - ${solicitud.nuevaHora}</c:if>
                                </div>
                            </div>
                        </c:if>

                        <div class="mb-4">
                            <label class="form-label fw-bold">Descripción</label>
                            <div class="form-control form-control-figma" style="background-color:#f8f9fa; min-height: 100px;">
                                    ${solicitud.descripcion}
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${solicitud.estatus == 'Pendiente'}">
                                <div class="d-flex justify-content-end gap-2">
                                    <button type="button" class="btn-cancelar-figma fw-medium px-4 py-2" id="btnNegar">Negar</button>
                                    <button type="button" class="btn-figma fw-medium px-4 py-2" id="btnReprogramar">Reprogramar</button>
                                    <button type="button" class="btn-figma fw-medium px-4 py-2" id="btnAceptar">Aceptar</button>
                                </div>

                                <c:if test="${param.error == 'fecha_invalida'}">
                                    <div class="alert alert-danger mt-3" role="alert">
                                        La fecha u hora propuesta ya no está disponible. Debes reprogramar con al menos 2 días de anticipación y elegir un horario libre del tutor.
                                    </div>
                                </c:if>

                                <div class="d-none mt-4" id="panelReprogramar">
                                    <form id="formReprogramar" method="post" action="${pageContext.request.contextPath}/solicitudes">
                                        <input type="hidden" name="accion" value="reprogramar">
                                        <input type="hidden" name="idSolicitud" value="${solicitud.idSolicitud}">

                                        <div class="row g-3 mb-3">
                                            <div class="col-md-6">
                                                <label for="nuevaFecha" class="form-label fw-bold">Nuevo día</label>
                                                <select id="nuevaFecha" name="nuevaFecha" class="form-select form-control-figma" required>
                                                    <option value="" selected disabled>Seleccione un día</option>
                                                </select>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="nuevaHora" class="form-label fw-bold">Nueva hora</label>
                                                <select id="nuevaHora" name="nuevaHora" class="form-select form-control-figma" required disabled>
                                                    <option value="" selected disabled>Seleccione un día primero</option>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="d-flex justify-content-end">
                                            <button type="submit" class="btn-figma fw-medium px-4 py-2">Enviar nueva fecha</button>
                                        </div>
                                    </form>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="d-flex justify-content-end">
                                    <a href="${pageContext.request.contextPath}/solicitudes" class="btn-figma fw-medium px-4 py-2">Volver</a>
                                </div>
                            </c:otherwise>
                        </c:choose>

                    </div>
                </div>

                <!-- Formularios ocultos -->
                <form id="formAceptar" method="post" action="${pageContext.request.contextPath}/solicitudes" class="d-none">
                    <input type="hidden" name="accion" value="aceptar">
                    <input type="hidden" name="idSolicitud" value="${solicitud.idSolicitud}">
                </form>

                <form id="formRechazar" method="post" action="${pageContext.request.contextPath}/solicitudes" class="d-none">
                    <input type="hidden" name="accion" value="rechazar">
                    <input type="hidden" name="idSolicitud" value="${solicitud.idSolicitud}">
                </form>

            </c:otherwise>
        </c:choose>

    </div>

</div>

<!-- ==================== MODALES DE ALERTA ==================== -->
<jsp:include page="/includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>

<c:if test="${not empty solicitud and solicitud.estatus == 'Pendiente'}">
    <script>
        window.DISPONIBILIDAD_REPROGRAMAR = ${empty disponibilidadJson ? '{}' : disponibilidadJson};
        window.DURACION_SOLICITUD = ${empty duracionSolicitud ? 1 : duracionSolicitud};
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/tutor/solicitud-detalle.js"></script>

</c:if>

</body>
</html>