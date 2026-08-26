<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-08-07
  Descripción: Página pública a la que llega el encargado de un área de apoyo
  desde el enlace del correo de canalización, para confirmar por token que
  atendió al alumno. Muestra el resultado de la confirmación.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Confirmación de Canalización</title>
    <link href="assets/css/bootstrap.css" rel="stylesheet">
    <link href="assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">

<div class="min-vh-100 d-flex align-items-center justify-content-center p-3">

    <div class="auth-card text-center">

        <div class="text-center mb-4">
            <img src="assets/img/login/logoUtez.png" alt="Logotipo UTEZ" class="auth-logo">
        </div>

        <c:choose>
            <c:when test="${resultado == 'ok'}">
                <div class="alert alert-success d-flex align-items-center py-3" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    <div class="text-start">
                        Canalización confirmada. Gracias por atender al alumno y avisarnos.
                    </div>
                </div>
            </c:when>
            <c:when test="${resultado == 'ya_confirmada'}">
                <div class="alert alert-info d-flex align-items-center py-3" role="alert">
                    <i class="bi bi-info-circle-fill me-2"></i>
                    <div class="text-start">
                        Esta canalización ya había sido confirmada anteriormente.
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-danger d-flex align-items-center py-3" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <div class="text-start">
                        El link de confirmación no es válido. Verifica que copiaste la dirección completa del correo.
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

</div>

</body>
</html>
