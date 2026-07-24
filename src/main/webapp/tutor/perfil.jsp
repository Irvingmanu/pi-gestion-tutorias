<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Perfil Tutor</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<jsp:include page="../includes/alertas.jsp" />

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Tutor) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/tutor/registro-individual.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaIndividual.png" alt="Tutoría Individual">
            <span>Tutoría Individual</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/registro-grupal.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaGrupal.png" alt="Tutoría Grupal">
            <span>Tutoría Grupal</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/solicitudes.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/solicitudes.png" alt="Solicitudes">
            <span>Solicitudes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/reportes.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto active">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </aside>

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Perfil Tutor
        </div>

        <div class="bg-white rounded shadow-sm border p-4">

            <div class="d-flex align-items-center gap-4 mb-4">
                <div class="bg-light rounded-circle d-flex justify-content-center align-items-center" style="width: 80px; height: 80px;">
                    <i class="bi bi-person fs-1"></i>
                </div>
                <h4 class="mb-0">${tutor.nombres} ${tutor.apellidos}</h4>
            </div>

            <div class="mb-4">
                <p class="fw-bold mb-3">Información Personal</p>
                <ul class="list-unstyled fs-6">
                    <li class="mb-2">Nombre: ${tutor.apellidos} ${tutor.nombres}</li>
                    <li class="mb-2">Nómina: ${tutor.nomina}</li>
                    <li class="mb-2">Email: ${tutor.correoInstitucional}</li>
                    <li class="mb-2">Teléfono: ${tutor.telefono}</li>
                </ul>
            </div>

            <div class="mb-4 mt-4">
                <p class="fw-bold mb-3">Información Académica</p>
                <ul class="list-unstyled fs-6">
                    <li class="mb-2">Rol: Tutor Académico</li>
                    <li class="mb-2">División: ${tutor.divisionAcademica}</li>
                </ul>
            </div>

            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-cancelar-figma rounded-figma px-4" id="btnCerrarSesion">
                    Cerrar sesión
                </button>
            </div>

        </div>
    </div>
</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script>
    document.getElementById('btnCerrarSesion').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Cerrar sesión?',
            'Tendrás que iniciar sesión de nuevo para continuar.',
            'Cerrar sesión',
            function () {
                window.location.href = '<%= request.getContextPath() %>/logout';
            }
        );
    });
</script>
</body>
</html>