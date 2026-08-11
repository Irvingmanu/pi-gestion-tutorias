<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    request.setAttribute("paginaActiva", "tutores");
%>
<c:set var="tutorFormulario" value="${not empty tutorEdit ? tutorEdit : tutor}" scope="page"/>
<c:set var="esEdicion" value="${not empty tutorEdit or param.accion == 'editar' or param.accion == 'prepararEdicion'}" scope="page"/>
<c:set var="tituloBanner" value="${esEdicion ? 'Editar Tutor' : 'Nuevo Tutor'}" scope="page"/>

<c:choose>
    <c:when test="${error == 'nomina_duplicada'}">
        <c:set var="mensajeError" value="Esta nómina ya está registrada en el sistema."/>
    </c:when>
    <c:when test="${error == 'correo_duplicado'}">
        <c:set var="mensajeError" value="Este correo ya está registrado en el sistema."/>
    </c:when>
    <c:when test="${error == 'telefono_duplicado'}">
        <c:set var="mensajeError" value="Este número de teléfono ya está registrado en el sistema."/>
    </c:when>
    <c:when test="${error == 'formato_invalido'}">
        <c:set var="mensajeError" value="Verifica los datos. El formato de uno o más campos es incorrecto."/>
    </c:when>
    <c:when test="${error == 'correo_invalido'}">
        <c:set var="mensajeError" value="El correo debe ser un correo institucional válido terminado en @utez.edu.mx."/>
    </c:when>
    <c:when test="${error == 'horario_requerido'}">
        <c:set var="mensajeError" value="Debes agregar al menos un horario de atención antes de guardar."/>
    </c:when>
    <c:when test="${error == 'registro_fallido'}">
        <c:set var="mensajeError" value="No se pudo guardar el tutor. Intenta de nuevo."/>
    </c:when>
</c:choose>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - ${tituloBanner}</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-tutores.css" rel="stylesheet">

    <style>
        .form-control-figma.is-invalid, .form-select.is-invalid {
            border-color: #dc3545 !important;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(.375em + .1875rem) center;
            background-size: calc(.75em + .375rem) calc(.75em + .375rem);
        }

        .btn-figma:disabled {
            background-color: #7ab899 !important;
            color: #ffffff;
            cursor: not-allowed;
            opacity: 0.6;
            box-shadow: none;
            border: none;
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            ${tituloBanner}
        </div>

        <form id="formGuardar" class="form-wrap-figma mt-3 needs-validation" style="max-width: 1100px;" action="${pageContext.request.contextPath}/gestion-tutores" method="post" novalidate>

            <input type="hidden" name="accion" value="${esEdicion ? 'editar' : 'nuevo'}">
            <input type="hidden" name="idTutor" value="${not empty tutorFormulario ? tutorFormulario.idTutor : 0}">
            <input type="hidden" name="idUsuario" value="${not empty tutorFormulario ? tutorFormulario.idUsuario : 0}">

            <div class="row">

                <div class="col-lg-4 col-md-6 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Personal</h5>

                    <div class="mb-4">
                        <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                        <input type="text" id="nombres" name="nombres" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.nombres}"
                               placeholder="Escribe los nombres" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

                    <div class="mb-4">
                        <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                        <input type="text" id="apellidos" name="apellidos" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.apellidos}"
                               placeholder="Escribe los apellidos" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

                    <div class="mb-4">
                        <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                        <input type="text" id="telefono" name="telefono" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.telefono}"
                               placeholder="+52 ..." pattern="^\d{10}$" maxlength="10" minlength="10"
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')" required>
                        <div class="invalid-feedback">Debe contener exactamente 10 dígitos numéricos.</div>
                    </div>

                    <!-- ============ CORREO: forzado a @utez.edu.mx, con feedback en vivo ============ -->
                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.correoInstitucional}"
                               placeholder="usuario@utez.edu.mx"
                               pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$"
                               title="El correo debe terminar en @utez.edu.mx"
                               maxlength="100"
                               oninput="this.value = this.value.toLowerCase().replace(/[^a-z0-9.\-_@]/g, '')"
                               required>
                        <div class="invalid-feedback">Debe ser un correo válido terminado en @utez.edu.mx.</div>
                    </div>
                </div>

                <div class="col-lg-4 col-md-6 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Laboral</h5>

                    <div class="mb-4">
                        <label for="nomina" class="form-label fs-6 fw-bold">Nómina</label>
                        <input type="text" id="nomina" name="nomina" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.nomina > 0 ? tutorFormulario.nomina : ''}"
                               placeholder="Escribe la nómina" maxlength="4" minlength="4" pattern="^[0-9]{4}$"
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                        ${esEdicion ? 'readonly' : ''} required>
                        <div class="invalid-feedback">La nómina debe tener exactamente 4 dígitos.</div>
                    </div>

                    <div class="mb-4">
                        <label for="academia" class="form-label fs-6 fw-bold">Academia</label>
                        <select id="academia" name="idAcademia" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty tutorFormulario ? 'selected' : ''}>Seleccione la academia</option>
                            <c:forEach var="academia" items="${listaAcademias}">
                                <option value="${academia.idAcademia}"
                                    ${not empty tutorFormulario && tutorFormulario.idAcademia == academia.idAcademia ? 'selected' : ''}>
                                        ${academia.nombre}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor selecciona una academia.</div>
                    </div>
                </div>

                <div class="col-lg-4 col-md-12 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Horario de Atención</h5>

                    <div class="mb-4">
                        <label for="selectDia" class="form-label fs-6 fw-bold">Días</label>
                        <select id="selectDia" class="form-select form-control-figma w-100 fs-6">
                            <option value="" selected>Día</option>
                            <option value="Lunes">Lunes</option>
                            <option value="Martes">Martes</option>
                            <option value="Miércoles">Miércoles</option>
                            <option value="Jueves">Jueves</option>
                            <option value="Viernes">Viernes</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold">Horas</label>
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <div class="row g-2 flex-grow-1 align-items-center">
                                <div class="col-6">
                                    <input type="time" id="horarioDesde" class="form-control form-control-figma fs-6"
                                           value="07:00" min="07:00" max="21:00" style="cursor: pointer;"
                                           onclick="this.showPicker()" onchange="validarLimitesHora(this)">
                                </div>
                                <div class="col-6">
                                    <input type="time" id="horarioHasta" class="form-control form-control-figma fs-6"
                                           value="09:00" min="07:00" max="21:00" style="cursor: pointer;"
                                           onclick="this.showPicker()" onchange="validarLimitesHora(this)">
                                </div>
                            </div>
                            <button type="button" id="btnAgregarHorario" class="btn-figma btn-figma-sm flex-shrink-0" title="Agregar Horario">+</button>
                        </div>
                    </div>

                    <div id="contenedorHorarios" class="d-flex flex-column gap-2 mt-2 mb-4 p-2 rounded-figma border bg-white shadow-sm"
                         style="height: 180px !important; max-height: 180px !important; overflow-y: auto !important; overflow-x: hidden;">
                        <c:forEach var="horario" items="${tutorFormulario.horariosDispo}">
                            <div class="d-flex align-items-center gap-2 mb-2 horario-item">
                                <input type="text" class="form-control form-control-figma fs-6" value="${horario}" readonly>
                                <input type="hidden" name="horariosDispo" value="${horario}">
                                <button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0" onclick="eliminarHorario(this)" title="Eliminar Horario">-</button>
                            </div>
                        </c:forEach>
                    </div>
                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4 border-top pt-4">
                <button type="button" id="btnCancelarFormulario" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="${pageContext.request.contextPath}/gestion-tutores" onclick="confirmarCancelacion()">Cancelar</button>
                <button type="submit" id="btnGuardar" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/tutor.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/formulario-tutor.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/validar-correo.js"></script>

<c:if test="${not empty mensajeError}">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            mostrarAlerta('error', 'Error', '${mensajeError}');
        });
    </script>
</c:if>
</body>
</html>