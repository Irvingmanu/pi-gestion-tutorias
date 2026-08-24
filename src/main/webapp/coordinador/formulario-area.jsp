<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="paginaActiva" value="areas" scope="request"/>
<c:set var="esEdicion" value="${not empty areaEdit}" scope="page"/>
<c:set var="tituloBanner" value="${esEdicion ? 'Editar área de apoyo' : 'Nueva área de Apoyo'}" scope="page"/>

<c:choose>
    <c:when test="${error == 'formato_invalido'}">
        <c:set var="mensajeError" value="Verifica los datos. El formato de uno o más campos es incorrecto." scope="page"/>
    </c:when>
    <c:when test="${error == 'nombre_duplicado'}">
        <c:set var="mensajeError" value="Ya existe un área de apoyo registrada con ese nombre." scope="page"/>
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
            background-color: #7ab899 !important;
            color: #ffffff;
            cursor: not-allowed;
            opacity: 0.6;
            box-shadow: none;
            border: none;
        }

        /* Fondo gris para campos bloqueados por tener alumnos canalizados */
        .campo-bloqueado, .campo-bloqueado:focus {
            background-color: #e9ecef !important;
            cursor: not-allowed;
            opacity: 1;
        }

        /* Truco para detectar el autocompletado del navegador (Chrome/Edge)
           desde JS: al autocompletar un input, el navegador dispara esta
           animacion (vacia, no se ve nada) y formulario-area.js la escucha
           via el evento 'animationstart' para revalidar el campo al instante. */
        @keyframes onAutoFillStart {
            from {}
            to {}
        }
        input:-webkit-autofill {
            animation-name: onAutoFillStart;
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

        <c:choose>
            <c:when test="${not esEdicion}">
                <!-- ==================== MODO NUEVA AREA: un solo form ==================== -->
                <form id="formNuevaArea" class="form-wrap-figma mt-3 needs-validation" style="max-width: 900px;" action="${pageContext.request.contextPath}/areas-apoyo"
                      method="post" onsubmit="return confirmarGuardarArea(event)" novalidate>

                    <input type="hidden" name="accion" value="guardarArea">

                    <div class="row g-4">

                        <!-- Columna izquierda: datos del area -->
                        <div class="col-md-6">

                            <div class="mb-4">
                                <label for="nombreArea" class="form-label fs-6 fw-bold">Nombre Área</label>
                                <input type="text" id="nombreArea" name="nombreArea" class="form-control form-control-figma w-100 fs-6"
                                       value="${area.nombre}" placeholder="Escribe nombre"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="nombresEncargado" class="form-label fs-6 fw-bold">Nombres del encargado</label>
                                <input type="text" id="nombresEncargado" name="nombresEncargado" class="form-control form-control-figma w-100 fs-6"
                                       value="${area.nombresEncargado}" placeholder="Escribe los nombres del encargado"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="apellidoPaternoEncargado" class="form-label fs-6 fw-bold">Apellido paterno del encargado</label>
                                <input type="text" id="apellidoPaternoEncargado" name="apellidoPaternoEncargado" class="form-control form-control-figma w-100 fs-6"
                                       value="${area.apellidoPaternoEncargado}" placeholder="Escribe el apellido paterno del encargado"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="apellidoMaternoEncargado" class="form-label fs-6 fw-bold">Apellido materno del encargado</label>
                                <input type="text" id="apellidoMaternoEncargado" name="apellidoMaternoEncargado" class="form-control form-control-figma w-100 fs-6"
                                       value="${area.apellidoMaternoEncargado}" placeholder="Escribe el apellido materno del encargado"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]*$" title="Solo se permiten letras, números, espacios y . , ( ) / -">
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                                <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                                       value="${area.correoContacto}" placeholder="Escribe el correo del encargado"
                                       pattern="^[a-zA-Z0-9._\-]+@utez\.edu\.mx$" title="El correo debe terminar en @utez.edu.mx" required>
                                <div class="invalid-feedback">El correo debe tener un formato válido y terminar en @utez.edu.mx.</div>
                            </div>

                        </div>

                        <!-- Columna derecha: motivos dinamicos, se insertan junto con el area -->
                        <div class="col-md-6">

                            <label class="form-label fs-6 fw-bold">Motivos de Canalización</label>

                            <!-- Input fijo para agregar: siempre visible arriba, no se recorre con la lista -->
                            <div class="d-flex gap-2 mb-2 align-items-start">
                                <div class="w-100">
                                    <input type="text" id="nuevoMotivoInput" class="form-control form-control-figma w-100 fs-6"
                                           placeholder="Escribe el motivo de atención" maxlength="150">
                                    <div id="feedbackNuevoMotivo" class="invalid-feedback" style="display:none;">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                                </div>
                                <button type="button" id="btnAgregarMotivoNuevo" class="btn-figma btn-figma-sm flex-shrink-0"
                                        onclick="agregarMotivo()" title="Agregar motivo">+</button>
                            </div>

                            <!-- Lista de motivos ya agregados: contenedor con scroll fijo, igual que horarios -->
                            <div id="motivosContainer" class="d-flex flex-column gap-2 p-2 rounded-figma border bg-white shadow-sm"
                                 style="height: 180px !important; max-height: 180px !important; overflow-y: auto !important; overflow-x: hidden;">
                            </div>
                            <div id="feedbackMotivoRequerido" class="invalid-feedback" style="display:none;">Es necesario agregar al menos un motivo de canalización.</div>

                        </div>

                    </div>

                    <div class="d-flex justify-content-center gap-3 mt-4">
                        <button type="button" id="btnCancelarFormularioArea" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                                data-url-cancelar="${pageContext.request.contextPath}/areas-apoyo"
                                onclick="confirmarCancelacionArea()">Cancelar</button>
                        <button type="submit" id="btnGuardarArea" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
                    </div>

                </form>

            </c:when>
            <c:otherwise>
                <!-- ==================== MODO EDICION: maestro-detalle en dos columnas ==================== -->

                <c:set var="areaBloqueada" value="${areaEdit.alumnosCanalizados > 0}" scope="page"/>

                <c:if test="${areaBloqueada}">
                    <div class="alert alert-danger mx-auto mt-3" role="alert" style="max-width: 900px; color:#dc3545; border-color:#dc3545; background-color:#f8d7da;">
                        El nombre del área y sus motivos están bloqueados porque ya cuenta con alumnos canalizados.
                    </div>
                </c:if>

                <div class="row g-4 mt-3 mx-auto" style="max-width: 900px;">

                    <!-- Columna izquierda: form del AREA (maestro), solo actualiza sus datos -->
                    <div class="col-md-6">
                        <form id="formEditarArea" class="form-wrap-figma needs-validation" action="${pageContext.request.contextPath}/areas-apoyo"
                              method="post" onsubmit="return confirmarGuardarArea(event)" novalidate>

                            <input type="hidden" name="accion" value="guardarArea">
                            <input type="hidden" name="idArea" value="${areaEdit.idArea}">

                            <div class="mb-4">
                                <label for="nombreArea" class="form-label fs-6 fw-bold">Nombre Área</label>
                                <input type="text" id="nombreArea" name="nombreArea"
                                       class="form-control form-control-figma w-100 fs-6 ${areaBloqueada ? 'campo-bloqueado' : ''}"
                                       value="${areaEdit.nombre}" placeholder="Escribe nombre"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -"
                                    ${areaBloqueada ? 'readonly' : ''} required>
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="nombresEncargado" class="form-label fs-6 fw-bold">Nombres del encargado</label>
                                <input type="text" id="nombresEncargado" name="nombresEncargado" class="form-control form-control-figma w-100 fs-6"
                                       value="${areaEdit.nombresEncargado}" placeholder="Escribe los nombres del encargado"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="apellidoPaternoEncargado" class="form-label fs-6 fw-bold">Apellido paterno del encargado</label>
                                <input type="text" id="apellidoPaternoEncargado" name="apellidoPaternoEncargado" class="form-control form-control-figma w-100 fs-6"
                                       value="${areaEdit.apellidoPaternoEncargado}" placeholder="Escribe el apellido paterno del encargado"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="apellidoMaternoEncargado" class="form-label fs-6 fw-bold">Apellido materno del encargado</label>
                                <input type="text" id="apellidoMaternoEncargado" name="apellidoMaternoEncargado" class="form-control form-control-figma w-100 fs-6"
                                       value="${areaEdit.apellidoMaternoEncargado}" placeholder="Escribe el apellido materno del encargado"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]*$" title="Solo se permiten letras, números, espacios y . , ( ) / -">
                                <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>

                            <div class="mb-4">
                                <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                                <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                                       value="${areaEdit.correoContacto}" placeholder="Escribe el correo del encargado"
                                       pattern="^[a-zA-Z0-9._\-]+@utez\.edu\.mx$" title="El correo debe terminar en @utez.edu.mx" required>
                                <div class="invalid-feedback">El correo debe tener un formato válido y terminar en @utez.edu.mx.</div>
                            </div>

                        </form>
                    </div>

                    <!-- Columna derecha: detalle de MOTIVOS, alta y baja individual (sin JS/AJAX) -->
                    <div class="col-md-6">

                        <label class="form-label fs-6 fw-bold">Motivos de Canalización</label>

                        <!-- Alta: form independiente que agrega un solo motivo a esta area.
                             Siempre visible arriba, fuera del contenedor con scroll de abajo. -->
                        <form id="formAgregarMotivo" class="d-flex gap-2 mb-2 needs-validation" action="${pageContext.request.contextPath}/areas-apoyo" method="post" novalidate onsubmit="return validarAgregarMotivo(event)">
                            <input type="hidden" name="accion" value="agregarMotivo">
                            <input type="hidden" name="idArea" value="${areaEdit.idArea}">
                            <div class="w-100 position-relative">
                                <input type="text" name="nuevoMotivo" id="inputAgregarMotivo"
                                       class="form-control form-control-figma w-100 fs-6 ${areaBloqueada ? 'campo-bloqueado' : ''}"
                                       placeholder="Escribe el motivo de atención"
                                       pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$" title="Solo se permiten letras, números, espacios y . , ( ) / -"
                                    ${areaBloqueada ? 'disabled' : ''}>
                                <div id="feedbackAgregarMotivo" class="invalid-feedback" style="display:none;">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                            </div>
                            <button type="submit" id="btnAgregarMotivo" class="btn-figma btn-figma-sm flex-shrink-0 ${areaBloqueada ? 'd-none' : ''}" ${areaBloqueada ? 'disabled' : ''}>+</button>
                        </form>

                        <!-- Baja/edicion: contenedor con scroll fijo, igual que horarios, para que
                             la pantalla no crezca conforme se agregan mas motivos. -->
                        <div id="listaMotivosArea" class="d-flex flex-column gap-2 p-2 rounded-figma border bg-white shadow-sm"
                             style="height: 180px !important; max-height: 180px !important; overflow-y: auto !important; overflow-x: hidden;">
                            <c:forEach items="${areaEdit.motivos}" var="motivo">
                                <div class="d-flex align-items-center gap-2 motivo-item">
                                    <span id="lbl-motivo-${motivo.idMotivo}" class="flex-grow-1"><c:out value="${motivo.nombreMotivo}"/></span>

                                    <form id="form-edit-${motivo.idMotivo}" class="d-none d-flex gap-2 flex-grow-1 needs-validation"
                                          action="${pageContext.request.contextPath}/areas-apoyo" method="post" novalidate>
                                        <input type="hidden" name="accion" value="editarMotivo">
                                        <input type="hidden" name="idArea" value="${areaEdit.idArea}">
                                        <input type="hidden" name="idMotivo" value="${motivo.idMotivo}">
                                        <div class="w-100 position-relative">
                                            <input type="text" name="nombreMotivo" value="${motivo.nombreMotivo}"
                                                   class="form-control form-control-figma w-100 fs-6"
                                                   pattern="^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s.,\(\)\/\-]+$"
                                                   title="Solo se permiten letras, números, espacios y . , ( ) / -" required>
                                            <div class="invalid-feedback">Solo se permiten letras, números, espacios y . , ( ) / -</div>
                                        </div>
                                        <button type="submit" class="btn-figma btn-figma-sm flex-shrink-0" title="Guardar motivo" disabled>
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/check.png" width="16" alt="Guardar">
                                        </button>
                                    </form>

                                    <div class="d-flex gap-2 flex-shrink-0 ${areaBloqueada ? 'd-none' : ''}">
                                        <button type="button" class="btn-figma btn-figma-sm" title="Editar motivo"
                                                onclick="toggleEditarMotivo(${motivo.idMotivo})">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/editar.png" width="16" alt="Editar">
                                        </button>
                                        <button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm" title="Eliminar motivo"
                                                onclick="prepararEliminacionMotivo(${motivo.idMotivo}, ${areaEdit.idArea})">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                        </button>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty areaEdit.motivos}">
                                <div class="text-muted text-center py-2">Sin motivos registrados.</div>
                            </c:if>
                        </div>

                    </div>

                </div>

                <!-- Botones del AREA: fuera del contenedor de 900px, centrados en toda la pantalla -->
                <div class="d-flex justify-content-center gap-3 w-100 mt-5">
                    <button type="button" id="btnCancelarFormularioArea" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                            data-url-cancelar="${pageContext.request.contextPath}/areas-apoyo"
                            onclick="confirmarCancelacionArea()">Cancelar</button>
                    <button type="submit" form="formEditarArea" id="btnGuardarEdicion" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
                </div>

                <!-- Form oculto compartido: elimina un motivo tras confirmar en prepararEliminacionMotivo() -->
                <form id="formEliminarMotivo" action="${pageContext.request.contextPath}/areas-apoyo" method="post" style="display:none;">
                    <input type="hidden" name="accion" value="eliminarMotivo">
                    <input type="hidden" name="idArea" id="inputEliminarMotivoIdArea">
                    <input type="hidden" name="idMotivo" id="inputEliminarMotivoId">
                </form>
            </c:otherwise>
        </c:choose>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/motivos.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/areas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/formulario-area.js"></script>

</body>
</html>