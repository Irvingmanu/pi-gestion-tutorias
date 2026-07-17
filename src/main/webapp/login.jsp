<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Iniciar Sesión</title>
    <link href="assets/css/bootstrap.css" rel="stylesheet">
    <link href="assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">

<div class="min-vh-100 d-flex align-items-center justify-content-center p-3">

    <div class="auth-card">

        <div class="text-center mb-4">
            <img src="assets/img/login/logoUtez.png" alt="Logotipo UTEZ" class="auth-logo">
        </div>

        <form>

            <div class="mb-4">
                <label for="opcionAsignacion" class="auth-label">Opción de asignación</label>
                <div class="auth-field">
                    <select class="form-select" id="opcionAsignacion" name="opcionAsignacion">
                        <option selected disabled>Seleccione una opción</option>
                        <option value="coordinador">Coordinador</option>
                        <option value="tutor">Tutor</option>
                        <option value="alumno">Alumno</option>
                    </select>
                </div>
            </div>

            <div class="mb-4">
                <label for="usuario" class="auth-label">Nomina/Matricula</label>
                <div class="auth-field">
                    <img src="assets/img/login/usuario.png" alt="" class="auth-icon-left">
                    <input type="text" class="form-control auth-input" id="usuario" name="usuario" placeholder="Usuario">
                </div>
            </div>

            <div class="mb-3">
                <label for="password" class="auth-label">Contraseña</label>
                <div class="auth-field auth-field--password">
                    <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                    <input type="password" class="form-control auth-input" id="password" name="password" placeholder="Contraseña">
                    <button type="button" class="auth-toggle-password" id="togglePassword" aria-label="Mostrar contraseña">
                        <img src="assets/img/login/ojoOcultar.png" alt="" class="auth-field-icon" id="toggleIcon">
                    </button>
                </div>
            </div>

            <div class="text-center mb-4">
                <a href="recuperar-correo.jsp" class="auth-forgot-link">¿Olvidaste tu contraseña?</a>
            </div>

            <div class="d-flex justify-content-center">
                <button type="submit" class="btn auth-btn-login px-5">Iniciar</button>
            </div>

        </form>

    </div>

</div>

<script src="assets/js/bootstrap.js"></script>
<script src="assets/js/auth.js"></script>
</body>
</html>
