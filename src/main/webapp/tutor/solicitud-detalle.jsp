<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Solicitud" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%
    Solicitud solicitud = (Solicitud) request.getAttribute("solicitud");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "MX"));

    request.setAttribute("paginaActiva", "solicitudes");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Detalle de Solicitud</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
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

        <% if (solicitud == null) { %>
        <div class="alert alert-danger" role="alert">
            No se encontró la solicitud solicitada.
        </div>
        <% } else { %>

        <div class="form-wrap-figma" style="max-width: 100%;">
            <div class="bg-white rounded-figma shadow-sm border p-4">

                <div class="d-flex align-items-center gap-3 mb-4">
                    <div class="bg-light rounded-circle d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                        <i class="bi bi-person fs-1"></i>
                    </div>
                    <div>
                        <div class="fw-bold fs-5">
                            <%= solicitud.getNombreAlumno() %> <%= solicitud.getApellidosAlumno() %>
                        </div>
                        <div class="text-muted"><%= solicitud.getMatricula() %></div>
                    </div>
                    <div class="ms-auto">
                        <%
                            String badge;
                            switch (solicitud.getEstatus()) {
                                case "Confirmada": badge = "success"; break;
                                case "Rechazada": badge = "danger"; break;
                                case "Reprogramada": badge = "info"; break;
                                default: badge = "warning";
                            }
                        %>
                        <span class="badge text-bg-<%= badge %> fs-6"><%= solicitud.getEstatus() %></span>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Asunto</label>
                    <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                        <%= solicitud.getAsunto() %>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Fecha Propuesta</label>
                    <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                        <% if (solicitud.getFechaPropuesta() != null) { %>
                        <%= formatoFecha.format(solicitud.getFechaPropuesta()) %><% if (solicitud.getHoraPropuesta() != null) { %> - <%= solicitud.getHoraPropuesta() %><% } %><% if (solicitud.getDuracion() != null) { %> (<%= solicitud.getDuracion() %> <%= solicitud.getDuracion() == 1 ? "hora" : "horas" %>)<% } %>
                        <% } else { %>
                        No especificada
                        <% } %>
                    </div>
                </div>

                <% if ("Reprogramada".equals(solicitud.getEstatus()) && solicitud.getNuevaFecha() != null) { %>
                <div class="mb-3">
                    <label class="form-label fw-bold">Nueva Fecha Propuesta</label>
                    <div class="form-control form-control-figma" style="background-color:#f8f9fa;">
                        <%= formatoFecha.format(solicitud.getNuevaFecha()) %><% if (solicitud.getNuevaHora() != null) { %> - <%= solicitud.getNuevaHora() %><% } %>
                    </div>
                </div>
                <% } %>

                <div class="mb-4">
                    <label class="form-label fw-bold">Descripción</label>
                    <div class="form-control form-control-figma" style="background-color:#f8f9fa; min-height: 100px;">
                        <%= solicitud.getDescripcion() %>
                    </div>
                </div>

                <% if ("Pendiente".equals(solicitud.getEstatus())) { %>
                <div class="d-flex justify-content-end gap-2">
                    <button type="button" class="btn-cancelar-figma fw-medium px-4 py-2" id="btnNegar">Negar</button>
                    <button type="button" class="btn-figma fw-medium px-4 py-2" id="btnReprogramar">Reprogramar</button>
                    <button type="button" class="btn-figma fw-medium px-4 py-2" id="btnAceptar">Aceptar</button>
                </div>

                <% if ("fecha_invalida".equals(request.getParameter("error"))) { %>
                <div class="alert alert-danger" role="alert">
                    La fecha u hora propuesta ya no está disponible. Debes reprogramar con al menos 2 días de anticipación y elegir un horario libre del tutor.
                </div>
                <% } %>

                <div class="d-none mt-4" id="panelReprogramar">
                    <form id="formReprogramar" method="post" action="<%= request.getContextPath() %>/SolicitudServlet">
                        <input type="hidden" name="accion" value="reprogramar">
                        <input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>">

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
                <% } else { %>
                <div class="d-flex justify-content-end">
                    <a href="<%= request.getContextPath() %>/SolicitudServlet" class="btn-figma fw-medium px-4 py-2">Volver</a>
                </div>
                <% } %>

            </div>
        </div>

        <!-- Formularios ocultos: el servlet ya maneja accion=aceptar / accion=rechazar -->
        <form id="formAceptar" method="post" action="<%= request.getContextPath() %>/SolicitudServlet" class="d-none">
            <input type="hidden" name="accion" value="aceptar">
            <input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>">
        </form>

        <form id="formRechazar" method="post" action="<%= request.getContextPath() %>/SolicitudServlet" class="d-none">
            <input type="hidden" name="accion" value="rechazar">
            <input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>">
        </form>

        <% } %>

    </div>

</div>

<!-- ==================== MODALES DE ALERTA (incluye tal cual del proyecto) ==================== -->
<jsp:include page="/includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>

<% if (solicitud != null && "Pendiente".equals(solicitud.getEstatus())) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var modalEl = document.getElementById('modalConfirmacion');
        var modalConfirmacion = new bootstrap.Modal(modalEl);

        var tituloEl = document.getElementById('confirmacionTitulo');
        var mensajeEl = document.getElementById('confirmacionMensaje');
        var iconoEl = document.getElementById('confirmacionIcono');
        var circuloEl = document.getElementById('confirmacionIconoCirculo');
        var btnAceptarModal = document.getElementById('btnConfirmacionAceptar');

        var accionPendiente = null; // 'aceptar' | 'rechazar'

        function abrirConfirmacion(accion) {
            accionPendiente = accion;

            circuloEl.classList.remove('confirmacion-icono--exito', 'confirmacion-icono--critica', 'confirmacion-icono--advertencia');

            if (accion === 'aceptar') {
                tituloEl.textContent = '¿Aceptar solicitud?';
                mensajeEl.textContent = 'El alumno será notificado de que su tutoría fue confirmada.';
                iconoEl.src = iconoEl.getAttribute('data-base-path') + 'exito.png';
                circuloEl.classList.add('confirmacion-icono--exito');
            } else {
                tituloEl.textContent = '¿Negar solicitud?';
                mensajeEl.textContent = 'El alumno será notificado de que su solicitud fue rechazada.';
                iconoEl.src = iconoEl.getAttribute('data-base-path') + 'advertencia.png';
                circuloEl.classList.add('confirmacion-icono--advertencia');
            }

            modalConfirmacion.show();
        }

        document.getElementById('btnAceptar').addEventListener('click', function () {
            abrirConfirmacion('aceptar');
        });

        document.getElementById('btnNegar').addEventListener('click', function () {
            abrirConfirmacion('rechazar');
        });

        btnAceptarModal.addEventListener('click', function () {
            if (accionPendiente === 'aceptar') {
                document.getElementById('formAceptar').submit();
            } else if (accionPendiente === 'rechazar') {
                document.getElementById('formRechazar').submit();
            }
        });

        // ---- Reprogramar: revela el panel con la disponibilidad real del tutor ----
        var btnReprogramar = document.getElementById('btnReprogramar');
        var panelReprogramar = document.getElementById('panelReprogramar');

        btnReprogramar.addEventListener('click', function () {
            panelReprogramar.classList.toggle('d-none');
        });
    });
</script>
<script>
    // Misma disponibilidad real (día+hora) que ve el alumno al crear la
    // solicitud, calculada en SolicitudServlet.construirDisponibilidadJson
    // a partir del ID_TUTOR de esta solicitud.
    const disponibilidadReprogramar = ${empty disponibilidadJson ? '{}' : disponibilidadJson};
    const duracionSolicitud = ${empty duracionSolicitud ? 1 : duracionSolicitud};

    document.addEventListener('DOMContentLoaded', function () {
        var selectDia = document.getElementById('nuevaFecha');
        var selectHora = document.getElementById('nuevaHora');

        if (!selectDia || !selectHora) {
            return;
        }

        Object.keys(disponibilidadReprogramar).forEach(function (fecha) {
            var opcion = document.createElement('option');
            opcion.value = fecha;
            opcion.textContent = fecha;
            selectDia.appendChild(opcion);
        });

        function sumarUnaHora(hora) {
            var partes = hora.split(':');
            var horaSiguiente = parseInt(partes[0], 10) + 1;
            return String(horaSiguiente).padStart(2, '0') + ':' + partes[1];
        }

        selectDia.addEventListener('change', function () {
            selectHora.innerHTML = '';
            var opcionVacia = document.createElement('option');
            opcionVacia.value = '';
            opcionVacia.textContent = 'Seleccione hora';
            opcionVacia.disabled = true;
            opcionVacia.selected = true;
            selectHora.appendChild(opcionVacia);

            var horasDelDia = disponibilidadReprogramar[selectDia.value] || [];

            // La duración de la reprogramación es la misma que ya tenía la
            // solicitud original: si es de 2 horas, solo se ofrecen horas
            // cuyo bloque siguiente también esté libre ese mismo día.
            var horasValidas = horasDelDia.filter(function (hora) {
                if (duracionSolicitud !== 2) {
                    return true;
                }
                return horasDelDia.indexOf(sumarUnaHora(hora)) !== -1;
            });

            horasValidas.forEach(function (hora) {
                var opcion = document.createElement('option');
                opcion.value = hora;
                opcion.textContent = hora;
                selectHora.appendChild(opcion);
            });

            selectHora.disabled = horasValidas.length === 0;
        });
    });
</script>
<% } %>

</body>
</html>
