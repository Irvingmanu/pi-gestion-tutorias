<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-07-01
  Descripción: Vista de inicio de sesión del sistema. Contiene el formulario de
  autenticación (correo y contraseña) enviado a LoginServlet, y el enlace de
  recuperación de contraseña.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
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

        <c:if test="${param.motivo == 'sesion_duplicada'}">
            <div class="alert alert-warning alert-dismissible fade show d-flex align-items-center py-2" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div class="small flex-grow-1">Tu sesión se cerró porque se inició sesión con tu cuenta en otro dispositivo o navegador.</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <form action="login" method="POST" id="loginForm" autocomplete="off" novalidate>

            <div class="mb-4">
                <label for="correo" class="auth-label">Correo institucional</label>
                <div class="auth-field">
                    <img src="assets/img/login/usuario.png" alt="" class="auth-icon-left">
                    <input type="email" class="form-control auth-input" id="correo" name="correo"
                           value="${param.correo}" placeholder="correo@utez.edu.mx"
                           autocomplete="off" readonly required>
                    <div class="invalid-feedback">
                        Ingresa tu correo institucional.
                    </div>
                </div>
            </div>

            <div class="mb-3">
                <label for="password" class="auth-label">Contraseña</label>
                <div class="auth-field auth-field--password">
                    <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                    <input type="password" class="form-control auth-input" id="password" name="password"
                           placeholder="Contraseña" autocomplete="new-password" readonly required>
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

<jsp:include page="includes/cargando.jsp" />
<script src="assets/js/bootstrap.js"></script>
<script src="assets/js/cargando.js"></script>
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

    // Si la pagina se restauro desde el bfcache (boton "Atras" del navegador), la
    // recargamos desde el servidor para que el formulario siempre aparezca limpio.
    window.addEventListener('pageshow', function (evento) {
        if (evento.persisted) {
            window.location.reload();
        }
    });

    // Truco anti-autofill: Chrome (y la mayoria de navegadores) NO autocompletan
    // campos marcados como "readonly" al cargar la pagina. En cuanto el usuario
    // hace foco/clic en el campo, le quitamos el readonly para que pueda escribir
    // normal. Esto es mas confiable que autocomplete="off", que Chrome ignora
    // a proposito para contraseñas guardadas.
    ['correo', 'password'].forEach(function (id) {
        var campo = document.getElementById(id);
        campo.value = '';
        campo.addEventListener('focus', function quitarReadonly() {
            campo.removeAttribute('readonly');
        });
        campo.addEventListener('mousedown', function () {
            campo.removeAttribute('readonly');
        });
    });
</script>
</body>
</html>