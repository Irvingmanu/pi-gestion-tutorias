<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="paginaActiva" value="grupal" scope="request" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Registro de Tutoría Grupal</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="titulo-principal banner-grupos h5 mb-4">
            Registro de Tutoría Grupal
        </div>

        <c:choose>
            <c:when test="${empty asignaciones}">
                <div class="alert alert-warning text-center fs-5">
                    No tienes grupos asignados para tutoría.
                </div>
            </c:when>
            <c:otherwise>
        <div class="form-wrap-figma" style="max-width: 900px;">
            <form id="formRegistroGrupal" action="${pageContext.request.contextPath}/tutoria-grupal" method="post">

                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="grupoAsignado" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="grupoAsignado" name="grupoAsignado" class="form-select form-control-figma w-100 fs-6" required>
                            <c:if test="${asignaciones.size() > 1}">
                            <option value="" selected>Seleccione el grupo</option>
                            </c:if>
                            <c:forEach var="asignacion" items="${asignaciones}">
                            <option value="${asignacion.valorOption}">${asignacion.etiqueta}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" required>
                    </div>
                    <div class="col-md-3">
                        <label for="hora" class="form-label fs-6 fw-bold">Hora</label>
                        <input type="time" id="hora" name="hora" class="form-control form-control-figma w-100 fs-6" required>
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                    </div>
                    <div class="col-md-6">
                        <label for="asesoriasGrupales" class="form-label fs-6 fw-bold">Asesorías grupales (Opcional)</label>
                        <input type="text" id="asesoriasGrupales" name="asesorias" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Describe las asesorías grupales">
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-12">
                        <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                        <textarea id="temasTratados" name="temas" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                    </div>
                </div>

                <div id="contenedorAsistencia" class="mb-4" style="display: none;">
                    <p class="fs-5 fw-bold text-center my-3">Lista de Asistencia</p>
                    <div class="table-responsive">
                        <table class="tabla-grupos fs-6 w-100">
                            <thead>
                            <tr>
                                <th>Matrícula</th>
                                <th>Nombre</th>
                                <th class="text-center">Asistencia</th>
                            </tr>
                            </thead>
                            <tbody id="cuerpoTablaAsistencia">
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                </div>

            </form>
        </div>
            </c:otherwise>
        </c:choose>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var selectGrupo = document.getElementById('grupoAsignado');
        var contenedorAsistencia = document.getElementById('contenedorAsistencia');
        var cuerpoTablaAsistencia = document.getElementById('cuerpoTablaAsistencia');

        function mostrarFilaMensaje(texto, claseTexto) {
            cuerpoTablaAsistencia.innerHTML = '';
            var fila = document.createElement('tr');
            var celda = document.createElement('td');
            celda.colSpan = 3;
            celda.className = 'text-center py-3 ' + claseTexto;
            celda.textContent = texto;
            fila.appendChild(celda);
            cuerpoTablaAsistencia.appendChild(fila);
        }

        function pintarAlumnos(alumnos) {
            cuerpoTablaAsistencia.innerHTML = '';

            if (!alumnos.length) {
                mostrarFilaMensaje('No hay alumnos registrados en este grupo.', 'text-muted');
                return;
            }

            alumnos.forEach(function (alumno) {
                var fila = document.createElement('tr');

                var tdMatricula = document.createElement('td');
                tdMatricula.textContent = alumno.matricula;

                var tdNombre = document.createElement('td');
                tdNombre.textContent = alumno.nombres + ' ' + alumno.apellidos;

                var tdAsistencia = document.createElement('td');
                tdAsistencia.className = 'text-center';
                var checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.name = 'asistentes';
                checkbox.value = alumno.matricula;
                tdAsistencia.appendChild(checkbox);

                fila.appendChild(tdMatricula);
                fila.appendChild(tdNombre);
                fila.appendChild(tdAsistencia);
                cuerpoTablaAsistencia.appendChild(fila);
            });
        }

        function cargarAsistencia() {
            if (!selectGrupo) {
                return;
            }

            var valor = selectGrupo.value;

            if (!valor) {
                contenedorAsistencia.style.display = 'none';
                cuerpoTablaAsistencia.innerHTML = '';
                return;
            }

            // El value del select viene armado como "idCarrera|idCuatrimestre|idLetra"
            var partes = valor.split('|');
            var idCarrera = partes[0];
            var idCuatrimestre = partes[1];
            var idLetra = partes[2];

            contenedorAsistencia.style.display = 'block';
            mostrarFilaMensaje('Cargando alumnos...', 'text-muted');

            var url = '${pageContext.request.contextPath}/tutoria-grupal?accion=obtenerAlumnos'
                    + '&idCarrera=' + encodeURIComponent(idCarrera)
                    + '&idCuatrimestre=' + encodeURIComponent(idCuatrimestre)
                    + '&idLetra=' + encodeURIComponent(idLetra);

            fetch(url)
                .then(function (resp) { return resp.json(); })
                .then(pintarAlumnos)
                .catch(function () {
                    mostrarFilaMensaje('Ocurrió un error al cargar la lista de alumnos.', 'text-danger');
                });
        }

        if (selectGrupo) {
            selectGrupo.addEventListener('change', cargarAsistencia);

            // Si el tutor solo tiene un grupo asignado, el select ya carga con ese
            // valor por defecto: disparamos el fetch de una vez sin esperar un "change".
            if (selectGrupo.value) {
                cargarAsistencia();
            }
        }

        var formRegistroGrupal = document.getElementById('formRegistroGrupal');
        formRegistroGrupal.addEventListener('submit', function (e) {
            e.preventDefault();

            if (!formRegistroGrupal.checkValidity()) {
                formRegistroGrupal.reportValidity();
                return;
            }

            mostrarConfirmacion(
                'advertencia',
                '¿Registrar tutoría grupal?',
                'Estás a punto de registrar la sesión y la asistencia del grupo.',
                'Sí, guardar',
                function () {
                    formRegistroGrupal.submit();
                }
            );
        });

        var parametros = new URLSearchParams(window.location.search);
        var exito = parametros.get('exito');
        var errorUrl = parametros.get('error');

        if (exito === 'grupal_guardada') {
            mostrarToast('exito', 'Guardado', 'La tutoría grupal fue registrada correctamente.');
        } else if (errorUrl === 'campos_incompletos') {
            mostrarAlerta('advertencia', 'Faltan datos', 'Completa todos los campos obligatorios.');
        } else if (errorUrl === 'datos_invalidos') {
            mostrarAlerta('advertencia', 'Datos inválidos', 'Revisa el grupo y la fecha capturados.');
        } else if (errorUrl === 'grupo_no_asignado') {
            mostrarAlerta('error', 'Grupo no válido', 'Ese grupo no está asignado a tu cuenta de tutor.');
        } else if (errorUrl === 'tutor_no_encontrado') {
            mostrarAlerta('error', 'Error', 'No se encontró el perfil de tutor asociado a tu cuenta.');
        } else if (errorUrl === 'guardado_fallido') {
            mostrarAlerta('error', 'Error', 'Ocurrió un error al guardar el registro. Intenta de nuevo.');
        }

        if (exito || errorUrl) {
            window.history.replaceState(null, null, window.location.pathname);
        }
    });
</script>
</body>
</html>
