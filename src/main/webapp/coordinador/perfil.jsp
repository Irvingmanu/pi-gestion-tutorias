<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="paginaActiva" value="perfil" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Perfil Coordinador</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<jsp:include page="../includes/alertas.jsp" />

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Perfil Coordinador
        </div>

        <div class="bg-white p-4 rounded-figma shadow-sm border">

            <div class="d-flex align-items-center gap-3 mb-4">
                <div class="rounded-circle bg-light d-flex justify-content-center align-items-center" style="width: 60px; height: 60px;">
                    <i class="bi bi-person fs-1"></i>
                </div>
                <h4 class="fs-4 mb-0">${coordinador.nombres} ${coordinador.apellidos}</h4>
            </div>

            <p class="fw-bold fs-5 mt-4 mb-3">Información Personal</p>
            <ul class="ms-3">
                <li class="mb-2"><strong>Nombre:</strong> ${coordinador.nombres} ${coordinador.apellidos}</li>
                <li class="mb-2"><strong>Email:</strong> ${coordinador.correoInstitucional}</li>
            </ul>

            <div class="d-flex justify-content-end mt-4 gap-2">
                <button type="button" class="btn-figma rounded-figma px-4"
                        data-bs-toggle="modal" data-bs-target="#modalCambiarPassword">
                    Cambiar contraseña
                </button>
                <button type="button" class="btn btn-cancelar-figma rounded-figma px-4"
                        id="btnCerrarSesion"
                        data-context-path="${pageContext.request.contextPath}">
                    Cerrar sesión
                </button>
            </div>

        </div>

    </div>

</div>

<!-- ==================== MODAL CAMBIAR CONTRASEÑA ==================== -->
<div class="modal fade" id="modalCambiarPassword" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-figma">
            <div class="modal-header">
                <h5 class="modal-title" id="tituloModalCambiarPassword">Cambiar contraseña</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">

                <!-- Paso 1: verificar contraseña actual -->
                <div id="pasoVerificarPassword">
                    <p class="mb-3 text-muted">Por seguridad, ingresa tu contraseña actual para continuar.</p>
                    <label for="passwordVerificar" class="form-label fs-6 fw-bold">Contraseña actual</label>
                    <input type="password" id="passwordVerificar" class="form-control form-control-figma w-100 fs-6" placeholder="Contraseña actual">
                    <div class="invalid-feedback" id="errorPasswordVerificar">Contraseña incorrecta.</div>
                </div>

                <!-- Paso 2: nueva contraseña -->
                <div id="pasoNuevaPassword" class="d-none">
                    <div class="mb-3">
                        <label for="passwordNueva" class="form-label fs-6 fw-bold">Nueva contraseña</label>
                        <input type="password" id="passwordNueva" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Nueva contraseña" minlength="8">
                        <div class="invalid-feedback" id="errorPasswordNueva">
                            Debe tener al menos 8 caracteres y ser distinta a la actual.
                        </div>
                    </div>
                    <div class="mb-2">
                        <label for="passwordConfirmar" class="form-label fs-6 fw-bold">Confirmar nueva contraseña</label>
                        <input type="password" id="passwordConfirmar" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Confirma la nueva contraseña">
                        <div class="invalid-feedback" id="errorPasswordConfirmar">Las contraseñas no coinciden.</div>
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" id="btnCancelarCambioPassword" class="btn-cancelar-figma rounded-figma px-4" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" id="btnVerificarPassword" class="btn-figma rounded-figma px-4">Continuar</button>
                <button type="button" id="btnGuardarNuevaPassword" class="btn-figma rounded-figma px-4 d-none" disabled>Guardar</button>
            </div>
        </div>
    </div>
</div>

<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
    const ENDPOINT_CAMBIAR_PASSWORD = "${pageContext.request.contextPath}/perfil";
</script>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/logout.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cambiar-password-modal.js"></script>
</body>
</html>