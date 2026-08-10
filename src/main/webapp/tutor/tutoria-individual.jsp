<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="paginaActiva" value="individual" scope="request" />
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
                    Tutoría Espontánea
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
                    <form id="formTutoriaEspontanea" action="${pageContext.request.contextPath}/tutoria-individual" method="post">

                        <div class="row g-3 mb-4">
                            <div class="col-12">
                                <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                                <input type="text" id="matricula" name="matricula" class="form-control form-control-figma fs-6"
                                       placeholder="Escribe la matrícula del alumno" value="${matriculaEnviada}"
                                       style="text-transform: uppercase;"
                                       maxlength="10" minlength="10" pattern="^[a-zA-Z0-9]{10}$"
                                       title="La matrícula debe tener exactamente 10 caracteres, solo letras y números."
                                       oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()" required>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                                <input type="date" id="fecha" name="fecha" class="form-control form-control-figma fs-6" value="${fechaEnviada}" required>
                            </div>
                            <div class="col-md-6">
                                <label for="hora" class="form-label fs-6 fw-bold">Hora</label>
                                <input type="time" id="hora" name="hora" class="form-control form-control-figma fs-6" value="${horaEnviada}"
                                       min="07:00" max="21:00" required>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                                <textarea id="temasTratados" name="temasTratados" class="form-control form-control-figma fs-6"
                                          rows="3" placeholder="Describe los temas tratados en la sesión" required>${temasEnviados}</textarea>
                            </div>
                            <div class="col-md-6">
                                <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                                <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma fs-6"
                                          rows="3" placeholder="Describe los acuerdos alcanzados" required>${acuerdosEnviados}</textarea>
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
                            <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
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
                    </div>
                    <div class="mb-3">
                        <label for="modalAcuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="modalAcuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
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

<c:if test="${not empty error}">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            mostrarAlerta('error', 'Error', '${error}');
        });
    </script>
</c:if>

<script src="${pageContext.request.contextPath}/assets/js/tutor/tutoria-individual.js"></script>
</body>
</html>
