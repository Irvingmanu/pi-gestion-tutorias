<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Recuperar Contraseña</title>
    <link href="assets/css/bootstrap.css" rel="stylesheet">
    <link href="assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">

<div class="min-vh-100 d-flex align-items-center justify-content-center p-3">
    <div class="auth-card">

        <div class="text-center mb-4">
            <img src="assets/img/login/logoUtez.png" alt="Logotipo UTEZ" class="auth-logo">
        </div>

        <div class="text-center mb-4">
            <h1 class="auth-title">Recuperar contraseña</h1>
            <p class="auth-subtitle">
                <c:choose>
                    <c:when test="${step == 'verificar'}">Ingresa el código de 6 caracteres que recibiste por correo</c:when>
                    <c:when test="${step == 'cambiar'}">Ingresa tu nueva contraseña</c:when>
                    <c:otherwise>Ingresa tu correo electrónico o matrícula/nómina</c:otherwise>
                </c:choose>
            </p>
        </div>

        <c:if test="${not empty mensajeError}">
            <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center py-2" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div class="small flex-grow-1">${mensajeError}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty mensajeInfo}">
            <div class="alert alert-info alert-dismissible fade show d-flex align-items-center py-2" role="alert">
                <i class="bi bi-info-circle-fill me-2"></i>
                <div class="small flex-grow-1">${mensajeInfo}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success alert-dismissible fade show d-flex align-items-center py-2" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <div class="small flex-grow-1">${mensajeExito}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>

            <c:when test="${step == 'verificar'}">
                <form action="recuperar" method="POST" class="needs-validation" novalidate>
                    <input type="hidden" name="action" value="verificar">

                    <div class="mb-4">
                        <label for="codigo" class="auth-label">Código</label>
                        <div class="auth-field">
                            <input type="text" class="form-control text-uppercase" id="codigo" name="codigo"
                                   placeholder="Ingresa el código" maxlength="6" required>
                            <div class="invalid-feedback">
                                Ingresa el código de 6 caracteres que recibiste.
                            </div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-center mb-3">
                        <button type="submit" class="btn auth-btn-login px-5">Verificar</button>
                    </div>
                </form>
            </c:when>

            <c:when test="${step == 'cambiar'}">
                <form action="recuperar" method="POST" class="needs-validation" novalidate>
                    <input type="hidden" name="action" value="cambiar">

                    <div class="mb-4">
                        <label for="pass1" class="auth-label">Contraseña nueva</label>
                        <div class="auth-field auth-field--password">
                            <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                            <input type="password" class="form-control auth-input" id="pass1" name="pass1"
                                   placeholder="Contraseña nueva" required minlength="4">
                            <button type="button" class="auth-toggle-password" id="ojoPass1" aria-label="Mostrar contraseña">
                                <img src="assets/img/login/ojoOcultar.png" alt="" class="auth-field-icon" id="iconoPass1">
                            </button>
                            <div class="invalid-feedback">
                                Ingresa una contraseña de al menos 4 caracteres.
                            </div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="pass2" class="auth-label">Confirmar contraseña</label>
                        <div class="auth-field auth-field--password">
                            <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                            <input type="password" class="form-control auth-input" id="pass2" name="pass2"
                                   placeholder="Confirmar contraseña" required minlength="4">
                            <button type="button" class="auth-toggle-password" id="ojoPass2" aria-label="Mostrar contraseña">
                                <img src="assets/img/login/ojoOcultar.png" alt="" class="auth-field-icon" id="iconoPass2">
                            </button>
                            <div class="invalid-feedback" id="pass2Feedback">
                                Las contraseñas no coinciden.
                            </div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-center mb-3">
                        <button type="submit" class="btn auth-btn-login px-5">Cambiar contraseña</button>
                    </div>
                </form>
            </c:when>

            <c:otherwise>
                <form action="recuperar" method="POST" class="needs-validation" novalidate>
                    <input type="hidden" name="action" value="solicitar">

                    <div class="mb-4">
                        <label for="dato" class="auth-label">Correo electrónico o Matrícula/Nómina</label>
                        <div class="auth-field">
                            <img src="assets/img/login/correo.png" alt="" class="auth-icon-left">
                            <input type="text" class="form-control auth-input" id="dato" name="dato"
                                   placeholder="correo@utez.edu.mx o matrícula" required>
                            <div class="invalid-feedback">
                                Ingresa tu correo o matrícula/nómina.
                            </div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-center mb-3">
                        <button type="submit" class="btn auth-btn-login px-5">Siguiente</button>
                    </div>
                </form>
            </c:otherwise>

        </c:choose>

        <div class="d-flex flex-column align-items-center gap-2 small mt-4">
            <div>
                <span class="text-muted">¿Ya te acordaste de tu cuenta?</span>
                <a href="login.jsp" class="text-decoration-none fw-medium" style="color: #008B74;">Inicia sesión aquí</a>
            </div>
        </div>


    </div>
</div>

<script src="assets/js/bootstrap.js"></script>

<script>
    (function () {
        function toggleOjoRecuperar(inputId, iconoId) {
            const input = document.getElementById(inputId);
            const icono = document.getElementById(iconoId);

            if (!input || !icono) return;

            if (input.type === 'password') {
                input.type = 'text';
                icono.src = 'assets/img/login/ojoMostrar.png';
            } else {
                input.type = 'password';
                icono.src = 'assets/img/login/ojoOcultar.png';
            }
        }

        const btnPass1 = document.getElementById('ojoPass1');
        const btnPass2 = document.getElementById('ojoPass2');

        if (btnPass1) {
            btnPass1.addEventListener('click', function () {
                toggleOjoRecuperar('pass1', 'iconoPass1');
            });
        }

        if (btnPass2) {
            btnPass2.addEventListener('click', function () {
                toggleOjoRecuperar('pass2', 'iconoPass2');
            });
        }
    })();
</script>

<script>
    (() => {
        'use strict';
        document.querySelectorAll('.needs-validation').forEach(form => {
            form.addEventListener('submit', event => {
                const pass1 = form.querySelector('#pass1');
                const pass2 = form.querySelector('#pass2');
                if (pass1 && pass2) {
                    if (pass1.value !== pass2.value) {
                        pass2.setCustomValidity('Las contraseñas no coinciden.');
                    } else {
                        pass2.setCustomValidity('');
                    }
                }
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });
    })();
</script>
</body>
</html>