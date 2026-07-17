<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Recuperar Contraseña</title>
    <link href="assets/css/bootstrap.css" rel="stylesheet">
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
        </div>

        <form>

            <div class="mb-4">
                <label for="passwordNueva" class="auth-label">Contraseña nueva</label>
                <div class="auth-field auth-field--password">
                    <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                    <input type="password" class="form-control auth-input" id="passwordNueva" name="passwordNueva" placeholder="Contraseña nueva">
                </div>
            </div>

            <div class="mb-4">
                <label for="passwordConfirmar" class="auth-label">Confirmar contraseña</label>
                <div class="auth-field auth-field--password">
                    <img src="assets/img/login/candado.png" alt="" class="auth-icon-left">
                    <input type="password" class="form-control auth-input" id="passwordConfirmar" name="passwordConfirmar" placeholder="Confirmar contraseña">
                </div>
            </div>

            <div class="d-flex justify-content-center mb-3">
                <button type="submit" class="btn auth-btn-login px-5">Siguiente</button>
            </div>

        </form>

    </div>

</div>

<script src="assets/js/bootstrap.js"></script>
</body>
</html>
