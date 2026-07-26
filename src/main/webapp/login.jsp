<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Iniciar Sesión</title>
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

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center py-2" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div class="small flex-grow-1">${error}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty mensaje}">
            <div class="alert alert-success alert-dismissible fade show d-flex align-items-center py-2" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <div class="small flex-grow-1">${mensaje}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <form action="login" method="POST" id="loginForm" novalidate>

            <div class="mb-4">
                <label for="opcionAsignacion" class="auth-label">Opción de asignación</label>
                <div class="auth-field">
                    <select class="form-select" id="opcionAsignacion" name="opcionAsignacion" required>
                        <option value="" disabled ${empty param.opcionAsignacion ? 'selected' : ''}>Seleccione una opción</option>
                        <option value="coordinador" ${param.opcionAsignacion == 'coordinador' ? 'selected' : ''}>Coordinador</option>
                        <option value="tutor" ${param.opcionAsignacion == 'tutor' ? 'selected' : ''}>Tutor</option>
                        <option value="alumno" ${param.opcionAsignacion == 'alumno' ? 'selected' : ''}>Alumno</option>
                    </select>
                    <div class="invalid-feedback">
                        Selecciona una opción de asignación.
                    </div>
                </div>
            </div>

            <div class="mb-4">
                <label for="usuario" class="auth-label">Nomina/Matricula</label>
                <div class="auth-field">
                    <img src="assets/img/login/usuario.png" alt="" class="auth-icon-left">
                    <input type="text" class="form-control auth-input" id="usuario" name="usuario"
                           value="${param.usuario}" placeholder="Usuario" required>
                    <div class="invalid-feedback">
                        Ingresa tu matrícula o nómina.
                    </div>
                </div>
            </div>

            <div class="mb-3">
                <label for="password" class="auth-label">Contraseña</label>
                <div class="auth-field auth-field--password">
                    <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                    <input type="password" class="form-control auth-input" id="password" name="password"
                           placeholder="Contraseña" required>
                    <button type="button" class="auth-toggle-password" id="togglePassword" aria-label="Mostrar contraseña">
                        <img src="assets/img/login/ojoOcultar.png" alt="" class="auth-field-icon" id="toggleIcon">
                    </button>
                    <div class="invalid-feedback">
                        Ingresa tu contraseña.
                    </div>
                </div>
            </div>

            <div class="text-center mb-4">
                <a href="recuperar-contra.jsp" class="auth-forgot-link">¿Olvidaste tu contraseña?</a>
            </div>

            <div class="d-flex justify-content-center">
                <button type="submit" class="btn auth-btn-login px-5">Iniciar</button>
            </div>

        </form>
    </div>

</div>

<script src="assets/js/bootstrap.js"></script>
<script src="assets/js/auth.js"></script>
<script>
    (() => {
        'use strict';
        const form = document.getElementById('loginForm');
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    })();
</script>
</body>
</html>