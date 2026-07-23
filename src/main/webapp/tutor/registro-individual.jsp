<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Registro de Tutoría Individual</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Tutor) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/tutor/registro-individual.jsp" class="nav-item-grupos active">
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
        <a href="<%= request.getContextPath() %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </aside>

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Registro de Tutoría Individual
        </div>

        <div class="form-wrap-figma">
            <form action="<%= request.getContextPath() %>/TutoriaServlet" method="post">

                <input type="hidden" name="accion" value="registrarIndividual">

                <!-- Fila 1: Matricula / Fecha -->
                <div class="row g-3 mb-5">
                    <div class="col-md-6">
                        <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                        <input type="text" id="matricula" name="matricula" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Escribe la matrícula del alumno"
                               pattern="^[a-zA-Z0-9]+$" title="La matrícula solo debe contener letras y números, sin espacios ni símbolos."
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '')" required>
                    </div>
                    <div class="col-md-6">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" required>
                    </div>
                </div>

                <!-- Fila 2: Temas Tratados / Acuerdos -->
                <div class="row g-3 mb-5">
                    <div class="col-md-6">
                        <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                        <textarea id="temasTratados" name="temasTratados" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                    </div>
                    <div class="col-md-6">
                        <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                    </div>
                </div>

                <!-- Vínculo Directo -->
                <p class="fs-5 fw-bold text-center my-4">Vínculo Directo</p>

                <div class="row g-3 mb-5">
                    <div class="col-md-6">
                        <label for="selectBecas" class="form-label fs-6 fw-bold">Becas</label>
                        <select id="selectBecas" name="selectBecas" class="form-select form-control-figma select-canalizacion w-100 fs-6">
                            <option value="">Seleccione la beca</option>
                            <option value="1">Canalizar a Becas</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="selectAsesorias" class="form-label fs-6 fw-bold">Asesorías</label>
                        <select id="selectAsesorias" name="selectAsesorias" class="form-select form-control-figma select-canalizacion w-100 fs-6">
                            <option value="">Seleccione la Asesoria</option>
                            <option value="2">Canalizar a Asesorías</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="selectServicioMedico" class="form-label fs-6 fw-bold">Servicio Médico</label>
                        <select id="selectServicioMedico" name="selectServicioMedico" class="form-select form-control-figma select-canalizacion w-100 fs-6">
                            <option value="">Seleccione el servicio</option>
                            <option value="3">Canalizar a Servicio Médico</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="selectPsicopedagogia" class="form-label fs-6 fw-bold">Psicopedagogía</label>
                        <select id="selectPsicopedagogia" name="selectPsicopedagogia" class="form-select form-control-figma select-canalizacion w-100 fs-6">
                            <option value="">Seleccione el motivo</option>
                            <option value="4">Canalizar a Psicopedagogía</option>
                        </select>
                    </div>
                </div>

                <input type="hidden" id="idAreaCanalizacion" name="idAreaCanalizacion" value="">
                <input type="hidden" id="observacionesCanalizacion" name="observacionesCanalizacion" value="">

                <div class="d-flex justify-content-end mt-4">
                    <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                </div>

            </form>
        </div>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var selectsCanalizacion = document.querySelectorAll('.select-canalizacion');
        var inputIdArea = document.getElementById('idAreaCanalizacion');
        var inputObservaciones = document.getElementById('observacionesCanalizacion');

        selectsCanalizacion.forEach(function (select) {
            select.addEventListener('change', function () {
                if (select.value !== '') {
                    selectsCanalizacion.forEach(function (otroSelect) {
                        if (otroSelect !== select) {
                            otroSelect.selectedIndex = 0;
                        }
                    });
                    inputIdArea.value = select.value;
                    inputObservaciones.value = select.options[select.selectedIndex].text;
                } else {
                    inputIdArea.value = '';
                    inputObservaciones.value = '';
                }
            });
        });
    });
</script>
</body>
</html>
