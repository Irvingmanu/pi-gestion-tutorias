<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="paginaActiva" value="tutores" scope="request"/>
<!-- tutorFormulario, esEdicion, tituloBanner y mensajeError ya vienen calculados desde
TutoresServlet (forwardAFormulario/resolverMensajeError): esta vista solo los consume. -->
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
        /* Ajuste para iconos de error en inputs personalizados */
        .form-control-figma.is-invalid, .form-select.is-invalid {
            border-color: #dc3545 !important;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(.375em + .1875rem) center;
            background-size: calc(.75em + .375rem) calc(.75em + .375rem);
        }

        /* Estilo para degradar el botón cuando está deshabilitado */
        .btn-figma:disabled {
            background-color: #7ab899 !important; /* Un verde más pálido/opaco */
            color: #ffffff;
            cursor: not-allowed;
            opacity: 0.6;
            box-shadow: none;
            border: none;
        }
    </style>
</head>
<body data-mensaje-error="${mensajeError}">

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            ${tituloBanner}
        </div>

        <!-- Atributo novalidate para quitar los mensajes por defecto del navegador -->
        <form id="formGuardar" class="form-wrap-figma mt-3 needs-validation" style="max-width: 1100px;" action="${pageContext.request.contextPath}/gestion-tutores" method="post" novalidate>

            <input type="hidden" name="accion" value="${esEdicion ? 'editar' : 'nuevo'}">

            <div class="row">

                <!-- ==================== SECCIÓN 1: INFORMACIÓN PERSONAL ==================== -->
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
                        <label for="apellidoPaterno" class="form-label fs-6 fw-bold">Apellido paterno</label>
                        <input type="text" id="apellidoPaterno" name="apellidoPaterno" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.apellidoPaterno}"
                               placeholder="Escribe el apellido paterno" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

                    <div class="mb-4">
                        <label for="apellidoMaterno" class="form-label fs-6 fw-bold">Apellido materno</label>
                        <input type="text" id="apellidoMaterno" name="apellidoMaterno" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.apellidoMaterno}"
                               placeholder="Escribe el apellido materno" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]*$">
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

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario.correoInstitucional}"
                               placeholder="Escribe el correo" pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9.\-_@]/g, '')" required>
                        <div class="invalid-feedback">Debe ser un correo válido terminado en @utez.edu.mx.</div>
                    </div>
                </div>

                <!-- ==================== SECCIÓN 2: INFORMACIÓN LABORAL ==================== -->
                <div class="col-lg-4 col-md-6 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Laboral</h5>

                    <div class="mb-4">
                        <label for="nomina" class="form-label fs-6 fw-bold">Nómina</label>
                        <!-- La nomina ya no la captura el coordinador: se asigna automaticamente
                             a partir de 1000 (ver TutorDao#obtenerSiguienteNomina), tanto para
                             tutores nuevos como para los ya existentes en modo edicion. -->
                        <input type="text" id="nomina" name="nomina" class="form-control form-control-figma w-100 fs-6"
                               value="${tutorFormulario != null && tutorFormulario.numeroEmpleado > 0 ? tutorFormulario.numeroEmpleado : ''}"
                               placeholder="Asignada automáticamente" maxlength="4" minlength="4" pattern="^[0-9]{4}$"
                               readonly required>
                        <div class="invalid-feedback">La nómina debe tener exactamente 4 dígitos.</div>
                    </div>

                    <div class="mb-4">
                        <label for="academiaTutor" class="form-label fs-6 fw-bold">Academia</label>
                        <select id="academiaTutor" name="idAcademia" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty tutorFormulario ? 'selected' : ''}>Seleccione la academia</option>
                            <c:forEach var="academia" items="${listaAcademias}">
                                <option value="${academia.idAcademia}"
                                    ${tutorFormulario != null && tutorFormulario.idAcademia == academia.idAcademia ? 'selected' : ''}>
                                        ${academia.nombre}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor selecciona una academia.</div>
                    </div>
                </div>

                <!-- ==================== SECCIÓN 3: HORARIO DE ATENCIÓN ==================== -->
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
                    <div id="feedbackHorarioRequerido" class="invalid-feedback" style="display:none;">Es necesario agregar al menos un horario de atención.</div>
                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4 border-top pt-4">
                <button type="button" id="btnCancelarFormulario" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="${pageContext.request.contextPath}/gestion-tutores" onclick="confirmarCancelacion()">Cancelar</button>
                <!-- Botón de Guardar con ID para manipularlo con JS -->
                <button type="submit" id="btnGuardar" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="../includes/cargando.jsp" />
<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/tutor.js"></script>
</body>
</html>
