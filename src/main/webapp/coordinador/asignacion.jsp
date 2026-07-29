<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("paginaActiva", "asignacion");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Asignación de Tutores</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/asignacion.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Asignación de Tutores
        </div>

        <form action="${pageContext.request.contextPath}/asignacion" method="POST" class="asignacion-form-wrap mt-3">

            <div class="mb-4">
                <label class="campo-label fs-6" for="tutor">Tutor</label>
                <select id="tutor" name="id_tutor" class="form-select form-control-figma w-100 fs-6" required>
                    <option value="" selected disabled>Seleccione el tutor</option>
                    <c:forEach var="tutor" items="${listaTutores}">
                        <option value="${tutor.idTutor}">${tutor.nombres} ${tutor.apellidos}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-4">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" name="id_carrera" class="form-select form-control-figma w-100 fs-6" required>
                    <option value="" selected disabled>Seleccione la carrera</option>
                    <c:forEach var="carrera" items="${carreras}">
                        <option value="${carrera.idCarrera}">${carrera.nombre}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-4">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" name="id_cuatrimestre" class="form-select form-control-figma w-100 fs-6" required>
                    <option value="" selected disabled>Seleccione el cuatrimestre</option>
                    <c:forEach var="cuatrimestre" items="${listaCuatrimestres}">
                        <option value="${cuatrimestre.idCuatrimestre}">${cuatrimestre.numero}°</option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-4">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" name="id_letra_grupo" class="form-select form-control-figma w-100 fs-6" required>
                    <option value="" selected disabled>Seleccione el Grupo</option>
                    <c:forEach var="letraGrupo" items="${listaLetras}">
                        <option value="${letraGrupo.idLetra}">${letraGrupo.letra}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="text-center mt-4">
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Agregar</button>
            </div>

        </form>

        <div class="banner-grupos h5 mb-3 mt-5">
            Asignaciones Actuales
        </div>

        <div class="table-responsive mb-auto">
            <c:if test="${empty listaAsignaciones}">
                <div class="alert alert-info text-center">
                    No hay asignaciones registradas todavía.
                </div>
            </c:if>
            <c:if test="${not empty listaAsignaciones}">
                <table class="tabla-grupos fs-6">
                    <thead>
                    <tr>
                        <th>Tutor</th>
                        <th>Cuatrimestre</th>
                        <th>Grupo</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="asignacion" items="${listaAsignaciones}">
                        <tr>
                            <td>${asignacion.nombresTutor} ${asignacion.apellidosTutor}</td>
                            <td>${asignacion.numeroCuatrimestre}&deg;</td>
                            <td>${asignacion.letraGrupo}</td>
                            <td>
                                <div class="d-flex justify-content-center gap-2">
                                    <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacionAsignacion('${asignacion.idAsignacion}')">
                                        <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>

    </div>

</div>

<form id="formEliminarAsignacion" action="${pageContext.request.contextPath}/asignacion" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id_asignacion" id="inputEliminarAsignacion">
</form>

<!-- 1. PRIMERO cargamos el script base de Bootstrap para que inicialice objetos como Toast y Modal -->
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>

<!-- 2. DESPUÉS incluimos el HTML de los modales y toasts -->
<jsp:include page="../includes/alertas.jsp" />

<!-- 3. Script de alertas compartido: expone mostrarConfirmacion() para el borrado -->
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>

<!-- 3. SCRIPT DE CONTROL DE ALERTAS -->
<script>
    document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        const exito = urlParams.get('exito');
        const error = urlParams.get('error');

        if (exito === 'true' || exito === 'eliminado' || error) {
            const modalEl = document.getElementById('modalAlerta');
            const imgIcono = document.getElementById('alertaIcono');
            const circulo = document.getElementById('alertaIconoCirculo');
            const btnAceptar = document.getElementById('alertaBtnAceptar');

            if (modalEl) {
                if (circulo) {
                    circulo.classList.remove('alerta-icono--exito', 'alerta-icono--error', 'alerta-icono--advertencia');
                }

                if (exito === 'true') {
                    document.getElementById('alertaTitulo').innerText = "¡Asignación Exitosa!";
                    document.getElementById('alertaMensaje').innerText = "El tutor ha sido asignado correctamente al grupo.";
                    if (imgIcono) {
                        imgIcono.src = "${pageContext.request.contextPath}/assets/img/alertas/exito.png";
                    }
                    if (circulo) {
                        circulo.classList.add('alerta-icono--exito');
                    }
                    if (btnAceptar) {
                        btnAceptar.classList.remove('alerta-btn-error');
                        btnAceptar.classList.add('alerta-btn-exito');
                    }
                } else if (exito === 'eliminado') {
                    document.getElementById('alertaTitulo').innerText = "¡Asignación Eliminada!";
                    document.getElementById('alertaMensaje').innerText = "El tutor ya no está asignado a ese grupo y cuatrimestre.";
                    if (imgIcono) {
                        imgIcono.src = "${pageContext.request.contextPath}/assets/img/alertas/exito.png";
                    }
                    if (circulo) {
                        circulo.classList.add('alerta-icono--exito');
                    }
                    if (btnAceptar) {
                        btnAceptar.classList.remove('alerta-btn-error');
                        btnAceptar.classList.add('alerta-btn-exito');
                    }
                } else if (error === 'grupo_asignado') {
                    document.getElementById('alertaTitulo').innerText = "Grupo ya asignado";
                    document.getElementById('alertaMensaje').innerText = "Este grupo ya tiene un tutor asignado en ese cuatrimestre.";
                    if (imgIcono) {
                        imgIcono.src = "${pageContext.request.contextPath}/assets/img/alertas/error.png";
                    }
                    if (circulo) {
                        circulo.classList.add('alerta-icono--error');
                    }
                    if (btnAceptar) {
                        btnAceptar.classList.remove('alerta-btn-exito');
                        btnAceptar.classList.add('alerta-btn-error');
                    }
                } else if (error === 'true') {
                    document.getElementById('alertaTitulo').innerText = "Error en la Asignación";
                    document.getElementById('alertaMensaje').innerText = "Esta asignación ya existe en la base de datos.";
                    if (imgIcono) {
                        imgIcono.src = "${pageContext.request.contextPath}/assets/img/alertas/error.png";
                    }
                    if (circulo) {
                        circulo.classList.add('alerta-icono--error');
                    }
                    if (btnAceptar) {
                        btnAceptar.classList.remove('alerta-btn-exito');
                        btnAceptar.classList.add('alerta-btn-error');
                    }
                }

                if (imgIcono) {
                    imgIcono.style.display = "block";
                    imgIcono.style.width = "70px";
                    imgIcono.style.height = "70px";
                    imgIcono.style.margin = "0 auto";
                    imgIcono.style.objectFit = "contain";
                }

                const modal = new bootstrap.Modal(modalEl);
                modal.show();

                window.history.replaceState(null, null, window.location.pathname);
            }
        }
    });

    function prepararEliminacionAsignacion(idAsignacion) {
        mostrarConfirmacion(
            'critica',
            '¿Eliminar asignación?',
            'El tutor dejará de estar asignado a este grupo y cuatrimestre.',
            'Eliminar',
            function () {
                document.getElementById('inputEliminarAsignacion').value = idAsignacion;
                document.getElementById('formEliminarAsignacion').submit();
            }
        );
    }
</script>
</body>
</html>