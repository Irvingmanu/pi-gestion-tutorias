<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="paginaActiva" value="individual" scope="request" />
<%
    String codigoError = (String) request.getAttribute("error");
    String mensajeError = null;
    if ("campos_incompletos".equals(codigoError)) {
        mensajeError = "Completa todos los campos obligatorios.";
    } else if ("asistencia_no_indicada".equals(codigoError)) {
        mensajeError = "Indica si el alumno asistió o faltó a la sesión.";
    } else if ("fecha_invalida".equals(codigoError)) {
        mensajeError = "La fecha capturada no es válida.";
    } else if ("fecha_futura".equals(codigoError)) {
        mensajeError = "No se pueden registrar tutorías con fecha futura.";
    } else if ("fecha_fuera_periodo".equals(codigoError)) {
        mensajeError = "La fecha debe estar dentro del periodo escolar vigente.";
    } else if ("hora_invalida".equals(codigoError)) {
        mensajeError = "La hora capturada no es válida.";
    } else if ("horario_no_permitido".equals(codigoError)) {
        mensajeError = "Las tutorías solo pueden agendarse entre las 7:00 AM y las 9:00 PM.";
    } else if ("matricula_invalida".equals(codigoError)) {
        mensajeError = "La matrícula debe tener exactamente 10 caracteres.";
    } else if ("matricula_no_existe".equals(codigoError)) {
        mensajeError = "El alumno no está registrado en el sistema. Verifica la matrícula.";
    } else if ("alumno_no_asignado".equals(codigoError)) {
        mensajeError = "El alumno con esta matrícula existe, pero no está asignado a tus grupos.";
    } else if ("guardado_fallido".equals(codigoError)) {
        mensajeError = "Ocurrió un error al guardar el registro. Intenta de nuevo.";
    } else if ("tutor_no_encontrado".equals(codigoError)) {
        mensajeError = "No se encontró el perfil de tutor asociado a tu cuenta.";
    } else if ("aun_no_es_el_dia".equals(codigoError)) {
        mensajeError = "Aún no puedes completar esta sesión: solo se puede el día programado o después.";
    } else if ("sesion_no_encontrada".equals(codigoError)) {
        mensajeError = "No se encontró la sesión que intentas completar.";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Tutoría Individual</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <!-- Select2 (buscador del alumno en Tutoría Espontánea) -->
    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/select2-bootstrap-5-theme@1.3.0/dist/select2-bootstrap-5-theme.min.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Tutoría Individual
        </div>

        <ul class="nav nav-tabs mb-4" id="tutoriaTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link ${tabActiva == 'espontanea' ? '' : 'active'}" id="tab-programadas-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-programadas" type="button" role="tab" aria-selected="${tabActiva == 'espontanea' ? 'false' : 'true'}">
                    Sesiones Programadas
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link ${tabActiva == 'espontanea' ? 'active' : ''}" id="tab-espontanea-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-espontanea" type="button" role="tab" aria-selected="${tabActiva == 'espontanea' ? 'true' : 'false'}">
                    Tutoría Individual
                </button>
            </li>
        </ul>

        <div class="tab-content" id="tutoriaTabsContent">

            <!-- ==================== TAB 1: SESIONES PROGRAMADAS ==================== -->
            <div class="tab-pane fade ${tabActiva == 'espontanea' ? '' : 'show active'}" id="tab-programadas" role="tabpanel">

                <div class="table-responsive">
                    <c:choose>
                        <c:when test="${empty sesionesProgramadas}">
                            <div class="alert alert-info text-center">
                                No tienes sesiones programadas pendientes de completar.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <table class="tabla-grupos fs-6">
                                <thead>
                                <tr>
                                    <th>Matrícula</th>
                                    <th>Alumno</th>
                                    <th>Fecha</th>
                                    <th>Hora</th>
                                    <th>Acciones</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="sesion" items="${sesionesProgramadas}">
                                    <c:set var="nombreAlumno" value="${not empty nombresAlumnos[sesion.matricula] ? nombresAlumnos[sesion.matricula] : sesion.matricula}" />
                                    <tr>
                                        <td>${sesion.matricula}</td>
                                        <td>${nombreAlumno}</td>
                                        <td>${sesion.fecha}</td>
                                        <td>${sesion.hora}</td>
                                        <td>
                                            <button type="button" class="btn-figma btn-completar-sesion"
                                                    data-id-sesion="${sesion.idSesionIndividual}"
                                                    data-matricula="${sesion.matricula}"
                                                    data-alumno="${nombreAlumno}"
                                                    data-fecha="${sesion.fecha}"
                                                    data-hora="${sesion.hora}">
                                                Completar
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>

            <!-- ==================== TAB 2: TUTORÍA ESPONTÁNEA ==================== -->
            <div class="tab-pane fade ${tabActiva == 'espontanea' ? 'show active' : ''}" id="tab-espontanea" role="tabpanel">

                <div class="form-wrap-figma" style="max-width: 920px;">
                    <!-- novalidate: quitamos los mensajes nativos del navegador, igual que en coordinador/grupal -->
                    <form id="formTutoriaEspontanea" class="needs-validation" action="${pageContext.request.contextPath}/tutoria-individual" method="post" novalidate>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="grupoSelector" class="form-label fs-6 fw-bold">Grupo</label>
                                <select id="grupoSelector" class="form-select form-control-figma fs-6" required>
                                    <option value="">Selecciona un grupo</option>
                                    <c:forEach var="grupo" items="${gruposAsignados}">
                                        <option value="${grupo.idGrupo}">${grupo.nombreGrupo}</option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">Selecciona un grupo.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="alumnoBuscador" class="form-label fs-6 fw-bold">Alumno</label>
                                <!-- Select2 sobre un <select required name="matricula">: al elegir una opcion,
                                     el propio <select> manda la matricula en el POST, sin input hidden aparte. -->
                                <select id="alumnoBuscador" name="matricula" class="form-select" disabled required>
                                    <option value="">Selecciona un grupo primero</option>
                                </select>
                                <!-- El <select> real queda oculto (display:none) tras el widget de Select2,
                                     pero sigue siendo hermano en el DOM de este div, asi que la regla nativa
                                     de Bootstrap ".is-invalid ~ .invalid-feedback" lo detecta igual; ademas
                                     el submit handler lo fuerza a mano por si acaso (ver tutoria-individual.js). -->
                                <div class="invalid-feedback" id="alumnoBuscadorInvalido">Por favor, selecciona un alumno de la lista.</div>
                                <div class="form-text" id="alumnoEstado"></div>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                                <input type="date" id="fecha" name="fecha" class="form-control form-control-figma fs-6" value="${fechaEnviada}" required>
                                <div class="invalid-feedback">Selecciona una fecha válida.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="hora" class="form-label fs-6 fw-bold">Hora</label>
                                <input type="time" id="hora" name="hora" class="form-control form-control-figma fs-6" value="${horaEnviada}"
                                       min="07:00" max="21:00" required>
                                <div class="invalid-feedback">El horario debe ser de 7:00 am a 9:00 pm.</div>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                                <!-- Bloqueado hasta elegir Grupo y Alumno (ver actualizarCamposPorGrupoAlumno en
                                     tutoria-individual.js): evita que el tutor redacte temas/acuerdos de una
                                     sesion que todavia no tiene a quien pertenece. -->
                                <textarea id="temasTratados" name="temasTratados" class="form-control form-control-figma fs-6"
                                          rows="3" placeholder="Selecciona primero el grupo y el alumno" required disabled>${temasEnviados}</textarea>
                                <div class="invalid-feedback">Este campo es obligatorio.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                                <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma fs-6"
                                          rows="3" placeholder="Selecciona primero el grupo y el alumno" required disabled>${acuerdosEnviados}</textarea>
                                <div class="invalid-feedback">Este campo es obligatorio.</div>
                            </div>
                        </div>

                        <p class="fs-5 fw-bold text-center my-4">Vínculo Directo</p>

                        <div class="row g-3 mb-4">
                            <c:forEach var="area" items="${areasConMotivos}">
                                <div class="col-md-6">
                                    <label for="motivo-espontanea-${area.idArea}" class="form-label fs-6 fw-bold">${area.nombre}</label>
                                    <select id="motivo-espontanea-${area.idArea}" name="idMotivo" class="form-select form-control-figma fs-6">
                                        <option value="">Ninguno</option>
                                        <c:forEach var="motivo" items="${area.motivos}">
                                            <option value="${motivo.idMotivo}">${motivo.nombreMotivo}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="d-flex justify-content-end mt-4">
                            <!-- Botón deshabilitado hasta que el formulario sea válido, igual que en coordinador/grupal -->
                            <button type="submit" id="btnGuardarEspontanea" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
                        </div>

                    </form>
                </div>

            </div>

        </div>

    </div>

</div>

<!-- ==================== MODAL: COMPLETAR SESIÓN PROGRAMADA ==================== -->
<div class="modal fade" id="modalCompletarSesion" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <form id="formCompletarSesion" action="${pageContext.request.contextPath}/tutoria-individual" method="post">
                <input type="hidden" name="idSesion" id="modalIdSesion">

                <div class="modal-header">
                    <h5 class="modal-title">Completar sesión — <span id="modalAlumnoInfo"></span></h5>
                    <button type="button" class="btn-close" id="btnCerrarModalCompletar" aria-label="Cerrar"></button>
                </div>

                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold d-block">Asistencia</label>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="estatusAsistencia" id="modalAsistenciaPresente" value="Presente" required>
                            <label class="form-check-label" for="modalAsistenciaPresente">Asistió</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="estatusAsistencia" id="modalAsistenciaFalta" value="Falta" required>
                            <label class="form-check-label" for="modalAsistenciaFalta">Faltó</label>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="modalTemasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                        <textarea id="modalTemasTratados" name="temasTratados" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                        <div class="invalid-feedback">Este campo es obligatorio.</div>
                    </div>
                    <div class="mb-3">
                        <label for="modalAcuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="modalAcuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                        <div class="invalid-feedback">Este campo es obligatorio.</div>
                    </div>

                    <p class="fs-6 fw-bold text-center my-3">Vínculo Directo</p>

                    <div class="row g-3">
                        <c:forEach var="area" items="${areasConMotivos}">
                            <div class="col-md-6">
                                <label for="motivo-modal-${area.idArea}" class="form-label fs-6 fw-bold">${area.nombre}</label>
                                <select id="motivo-modal-${area.idArea}" name="idMotivo" class="form-select form-control-figma w-100 fs-6">
                                    <option value="">Ninguno</option>
                                    <c:forEach var="motivo" items="${area.motivos}">
                                        <option value="${motivo.idMotivo}">${motivo.nombreMotivo}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" id="btnCancelarModalCompletar" class="btn-cancelar-figma fw-medium px-4 py-2">Cancelar</button>
                    <button type="submit" class="btn-figma fw-medium px-4 py-2">Guardar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>

<% if (mensajeError != null) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        mostrarAlerta('error', 'Error', '<%= mensajeError %>');
    });
</script>
<% } %>

<!-- Select2 (buscador del alumno en Tutoría Espontánea): jQuery debe cargar antes que Select2,
     y ambos antes que tutoria-individual.js, que los usa. -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/tutoria-individual.js"></script>
</body>
</html>