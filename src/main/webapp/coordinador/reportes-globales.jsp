<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes Globales</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <style>
        #filtroDesde, #filtroHasta {
            cursor: pointer;
        }
        .tarjeta-kpi-clickeable {
            cursor: pointer;
            transition: box-shadow .15s ease, transform .15s ease;
        }
        .tarjeta-kpi-clickeable:hover, .tarjeta-kpi-clickeable:focus-visible {
            box-shadow: 0 .5rem 1rem rgba(0,0,0,.1) !important;
            transform: translateY(-2px);
        }
        .badge-al-dia {
            background-color: #E1F3E9;
            color: #008B74;
        }
        .badge-en-riesgo {
            background-color: #FBE2E1;
            color: #DC2626;
        }
        .badge-sin-objetivo {
            background-color: #E5E7EB;
            color: #374151;
        }
        .btn-alertar-tutor {
            border: none;
            background: transparent;
            color: #6B7280;
            padding: .25rem .4rem;
            border-radius: 8px;
        }
        .btn-alertar-tutor:hover {
            background-color: #ECF3FA;
        }
        .btn-alertar-tutor.riesgo {
            color: #DC2626;
        }
        .btn-alertar-tutor.riesgo:hover {
            background-color: #FBE2E1;
        }
        /* Reutilizadas por todos los modales de "Ver detalles" (Tutorías Grupales,
           Alumnos Atendidos, ...) para partir la ficha en secciones y evitar aglomeración. */
        .seccion-detalle-titulo {
            font-weight: 600;
            color: #0B1C30;
            font-size: .95rem;
            margin-bottom: .75rem;
        }
        .seccion-detalle-card {
            background-color: #F8FAFC;
            border-radius: var(--radius-figma, 12px);
            padding: 1rem 1.25rem;
        }
        .seccion-detalle-card + .seccion-detalle-card {
            margin-top: 1rem;
        }
        .campo-detalle-label {
            color: #6B7280;
            font-size: .8rem;
            margin-bottom: .15rem;
        }
        .campo-detalle-valor {
            margin-bottom: .85rem;
            white-space: pre-wrap;
        }
        /* Boton "Volver" de los modales de detalle: verde institucional en vez del azul
           por defecto de .btn-link de Bootstrap. */
        .btn-volver-figma {
            color: var(--utez-green) !important;
            text-decoration: none;
        }
        .btn-volver-figma:hover, .btn-volver-figma:focus {
            color: var(--utez-green) !important;
            text-decoration: underline;
        }
        .buscador-alumno-wrap {
            position: relative;
        }
        #resultadosBuscadorAlumno {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            z-index: 1050;
            max-height: 280px;
            overflow-y: auto;
            box-shadow: 0 .5rem 1rem rgba(0,0,0,.15);
            border-radius: var(--radius-figma, 12px);
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Reportes Globales
        </div>

        <div class="row g-3 mb-2">
            <div class="col-md-3">
                <label for="filtroCarrera" class="form-label fw-bold">Carrera</label>
                <select id="filtroCarrera" class="form-select form-control-figma">
                    <option value="">Seleccione la carrera</option>
                    <c:forEach var="c" items="${listaCarreras}">
                        <option value="${c.idCarrera}">${c.nombre}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="filtroCuatrimestre" class="form-label fw-bold">Cuatrimestre</label>
                <select id="filtroCuatrimestre" class="form-select form-control-figma">
                    <option value="">Seleccione el cuatrimestre</option>
                    <c:forEach var="numero" begin="1" end="10">
                        <option value="${numero}">${numero}°</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="filtroGrupo" class="form-label fw-bold">Grupo</label>
                <select id="filtroGrupo" class="form-select form-control-figma">
                    <option value="">Seleccione el grupo</option>
                    <option value="A">A</option>
                    <option value="B">B</option>
                    <option value="C">C</option>
                    <option value="D">D</option>
                    <option value="E">E</option>
                    <option value="F">F</option>
                </select>
            </div>
            <div class="col-md-3">
                <label for="filtroTutor" class="form-label fw-bold">Tutor</label>
                <select id="filtroTutor" class="form-select form-control-figma">
                    <option value="">Todos los tutores</option>
                    <c:forEach var="t" items="${listaTutores}">
                        <option value="${t.numeroEmpleado}">${t.nombres} ${t.apellidos}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-4 align-items-end">
            <div class="col-md-4">
                <label for="filtroDesde" class="form-label fw-bold">Desde</label>
                <input type="date" id="filtroDesde" class="form-control form-control-figma">
            </div>
            <div class="col-md-4">
                <label for="filtroHasta" class="form-label fw-bold">Hasta</label>
                <input type="date" id="filtroHasta" class="form-control form-control-figma">
            </div>
            <div class="col-md-4">
                <button type="button" id="btnBuscar" class="btn btn-figma w-100">Buscar</button>
            </div>
        </div>

        <div id="avisoReporte" class="aviso-inline aviso-inline-advertencia d-none" role="alert"></div>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label for="buscadorAlumno" class="form-label fw-bold">Buscar alumno (nombre o matrícula)</label>
                <div class="buscador-alumno-wrap">
                    <input type="text" id="buscadorAlumno" class="form-control form-control-figma"
                           placeholder="Ej. Juan Pérez o EN2312345" autocomplete="off">
                    <div id="resultadosBuscadorAlumno" class="list-group d-none"></div>
                </div>
                <input type="hidden" id="filtroMatricula" value="">
                <div id="filtroAlumnoActivo" class="alert alert-info d-none d-flex justify-content-between align-items-center py-2 px-3 mt-2 mb-0">
                    <span>Mostrando el reporte de: <strong id="nombreAlumnoFiltro"></strong> (<span id="matriculaAlumnoFiltro"></span>) — historial completo de todos los años en los que ha estado inscrito.</span>
                    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnQuitarFiltroAlumno">Quitar filtro</button>
                </div>
            </div>
        </div>

        <!-- ==================== TRAYECTORIA ACADEMICA DEL ALUMNO ====================
             Solo visible cuando hay un alumno filtrado (buscador o acceso directo desde
             gestion-grupos.jsp). Se llena via accion=trayectoriaAlumno sobre
             ALUMNO_GRUPO_HISTORICO (ver cargarTrayectoriaAlumno() mas abajo). -->
        <div class="row g-3 mb-4 d-none" id="seccionTrayectoriaAlumno">
            <div class="col-12">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="seccion-detalle-titulo mb-2">Trayectoria académica</div>
                    <div class="table-responsive">
                        <table class="table table-sm align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Carrera</th>
                                <th>Nivel</th>
                                <th>Cuatrimestre</th>
                                <th>Grupo</th>
                                <th>Generación</th>
                                <th>Desde</th>
                                <th>Hasta</th>
                            </tr>
                            </thead>
                            <tbody id="tablaTrayectoriaBody"></tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardAlumnosAtendidos" role="button" tabindex="0">
                    <i class="bi bi-person-check fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Alumnos Atendidos</div>
                        <div class="fw-bold fs-4" id="kpiAtendidos">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardTutoriasGrupales" role="button" tabindex="0">
                    <i class="bi bi-people fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Tutorías Grupales</div>
                        <div class="fw-bold fs-4" id="kpiTutoriasGrupales">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardCanalizados" role="button" tabindex="0">
                    <i class="bi bi-signpost-split fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Canalizados</div>
                        <div class="fw-bold fs-4" id="kpiCanalizados">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardPendientes" role="button" tabindex="0">
                    <i class="bi bi-hourglass-split fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Pendientes</div>
                        <div class="fw-bold fs-4" id="kpiPendientes">--</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3">
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Distribución de Alumnos Canalizados</div>
                    <div style="height: 320px;">
                        <canvas id="graficaPastel"></canvas>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Estado de Solicitudes de Asesoría (General)</div>
                    <div style="height: 320px;">
                        <canvas id="graficaBarras"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-end mt-4 gap-2">
            <button type="button" id="btnExportarExcel" class="btn btn-figma">
                <i class="bi bi-file-earmark-excel"></i> Exportar Excel
            </button>
            <button type="button" id="btnExportarPdf" class="btn btn-figma">
                <i class="bi bi-file-earmark-pdf"></i> Exportar PDF
            </button>
        </div>

    </div>

</div>

<div class="modal fade" id="modalTutoriasGrupales" tabindex="-1" aria-labelledby="tituloModalTutoriasGrupales" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalTutoriasGrupales">Seguimiento de Tutorías Grupales</h5>
                    <div class="text-muted small" id="subtituloModalTutoriasGrupales">Avance de cada tutor frente al objetivo del periodo vigente.</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinPeriodoGrupal" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay un periodo escolar vigente, por lo que no es posible calcular el avance de tutorías grupales.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Nombre del Tutor</th>
                            <th>Grupo Asignado</th>
                            <th>Tutorías Impartidas</th>
                            <th>Estatus</th>
                            <th class="text-center">Acciones</th>
                        </tr>
                        </thead>
                        <tbody id="tablaTutoriasGrupalesBody">
                        <tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleSesionGrupal" tabindex="-1" aria-labelledby="tituloModalDetalleSesionGrupal" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaTutorias">
                        <i class="bi bi-arrow-left"></i> Volver al seguimiento
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleSesionGrupal">Detalle de sesiones</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">

                <!-- Seccion 2: resumen del tutor/grupo/avance (misma info que la tarjeta de seguimiento) -->
                <div class="seccion-detalle-card mb-4">
                    <div class="seccion-detalle-titulo">Tutor y avance</div>
                    <div class="row g-3">
                        <div class="col-md-4">
                            <div class="campo-detalle-label">Nombre del Tutor</div>
                            <div class="campo-detalle-valor fw-semibold" id="detalleNombreTutor">--</div>
                        </div>
                        <div class="col-md-4">
                            <div class="campo-detalle-label">Grupo Asignado</div>
                            <div class="campo-detalle-valor fw-semibold" id="detalleGrupoAsignado">--</div>
                        </div>
                        <div class="col-md-2">
                            <div class="campo-detalle-label">Tutorías Impartidas</div>
                            <div class="campo-detalle-valor fw-semibold" id="detalleAvance">--</div>
                        </div>
                        <div class="col-md-2">
                            <div class="campo-detalle-label">Estatus</div>
                            <div class="campo-detalle-valor" id="detalleEstatus">--</div>
                        </div>
                    </div>
                </div>

                <!-- Seccion 1: detalle de cada sesion registrada, con la misma distribucion de
                     tarjetas blancas ("Detalles de la Sesión" / "Asesorías Grupales") que usa
                     el modal de detalle de Alumnos Atendidos, una tarjeta por sesion. -->
                <div id="avisoSinSesiones" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    Este tutor todavía no ha registrado sesiones grupales para este grupo en el periodo vigente.
                </div>
                <div id="listaSesionesGrupales"></div>

            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalAlumnosAtendidos" tabindex="-1" aria-labelledby="tituloModalAlumnosAtendidos" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalAlumnosAtendidos">Alumnos Atendidos</h5>
                    <div class="text-muted small">Tutorías Individuales y Espontáneas completadas (no incluye tutorías grupales).</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinAtenciones" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay tutorías individuales o espontáneas registradas con los filtros seleccionados.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Tipo</th>
                            <th>Fecha y Hora</th>
                            <th>Grupo</th>
                            <th>Alumno</th>
                            <th>Estado</th>
                            <th class="text-center">Acción</th>
                        </tr>
                        </thead>
                        <tbody id="tablaAlumnosAtendidosBody">
                        <tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleAtencion" tabindex="-1" aria-labelledby="tituloModalDetalleAtencion" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaAtenciones">
                        <i class="bi bi-arrow-left"></i> Volver al listado
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleAtencion">Detalle de la tutoría</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Detalles de la Sesión</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Grupo</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleGrupo">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Alumno</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleAlumno">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Fecha</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleFecha">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Hora</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleHora">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Temas Tratados</div>
                            <div class="campo-detalle-valor" id="atencionDetalleTemas">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Acuerdos</div>
                            <div class="campo-detalle-valor mb-0" id="atencionDetalleAcuerdos">--</div>
                        </div>
                    </div>
                </div>
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Vínculo Directo</div>
                    <div class="campo-detalle-valor mb-0" id="atencionDetalleVinculo">--</div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalCanalizados" tabindex="-1" aria-labelledby="tituloModalCanalizados" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalCanalizados">Alumnos Canalizados</h5>
                    <div class="text-muted small">Alumnos derivados a algún área de apoyo (Psicología, Servicio Médico, Becas, etc.).</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinCanalizaciones" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay canalizaciones registradas con los filtros seleccionados.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Alumno</th>
                            <th>Grupo</th>
                            <th>Fecha de Canalización</th>
                            <th>Tutor</th>
                            <th>Área de Canalización</th>
                            <th>Estatus</th>
                            <th class="text-center">Acción</th>
                        </tr>
                        </thead>
                        <tbody id="tablaCanalizadosBody">
                        <tr><td colspan="7" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleCanalizacion" tabindex="-1" aria-labelledby="tituloModalDetalleCanalizacion" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaCanalizados">
                        <i class="bi bi-arrow-left"></i> Volver al listado
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleCanalizacion">Detalle de la canalización</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Datos del Alumno</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Alumno</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleAlumno">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Grupo</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleGrupo">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Fecha de Canalización</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleFecha">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Tutor</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleTutor">--</div>
                        </div>
                    </div>
                </div>
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Datos de la Canalización</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Área de Canalización</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleArea">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Estatus</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleEstatus">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Motivo</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleMotivo">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Observaciones</div>
                            <div class="campo-detalle-valor mb-0" id="canalizacionDetalleObservaciones">--</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalSolicitudesPendientes" tabindex="-1" aria-labelledby="tituloModalSolicitudesPendientes" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalSolicitudesPendientes">Solicitudes Pendientes</h5>
                    <div class="text-muted small">Solicitudes de tutoría creadas por los alumnos que aún no han sido procesadas.</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinPendientes" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay solicitudes pendientes con los filtros seleccionados.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Alumno</th>
                            <th>Asunto</th>
                            <th>Fecha Propuesta</th>
                            <th>Estatus</th>
                            <th class="text-center">Acción</th>
                        </tr>
                        </thead>
                        <tbody id="tablaPendientesBody">
                        <tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleSolicitudPendiente" tabindex="-1" aria-labelledby="tituloModalDetalleSolicitudPendiente" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaPendientes">
                        <i class="bi bi-arrow-left"></i> Volver al listado
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleSolicitudPendiente">Detalle de la solicitud</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Datos de la Solicitud</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Nombre del Alumno</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleAlumno">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Matrícula</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleMatricula">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Asunto</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleAsunto">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Fecha Propuesta</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleFecha">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Descripción</div>
                            <div class="campo-detalle-valor mb-0" id="pendienteDetalleDescripcion">--</div>
                        </div>
                    </div>
                </div>
                <div class="seccion-detalle-card d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <div>
                        <div class="seccion-detalle-titulo mb-1">Tutor Asignado</div>
                        <div class="campo-detalle-valor mb-0 fw-semibold" id="pendienteDetalleTutor">--</div>
                    </div>
                    <button type="button" class="btn btn-figma" id="btnRecordarTutor">
                        <i class="bi bi-envelope"></i> Enviar recordatorio por correo
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalAlerta" tabindex="-1" aria-labelledby="alertaTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">
                <div class="alerta-icono-wrap">
                    <div class="alerta-icono" id="alertaIconoCirculo">
                        <img id="alertaIcono" src="" alt="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/">
                    </div>
                </div>
                <div class="alerta-texto">
                    <h2 class="alerta-titulo" id="alertaTitulo"></h2>
                    <p class="alerta-mensaje" id="alertaMensaje"></p>
                </div>
                <div class="alerta-botones" id="alertaBotones">
                    <button type="button" class="alerta-btn alerta-btn-secundario" id="alertaBtnCancelar" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="alerta-btn alerta-btn-exito" id="alertaBtnAceptar" data-bs-dismiss="modal">Aceptar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalConfirmacion" tabindex="-1" aria-labelledby="confirmacionTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">
                <div class="confirmacion-icono-wrap">
                    <div class="confirmacion-icono" id="confirmacionIconoCirculo">
                        <img id="confirmacionIcono" src="" alt="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/">
                    </div>
                </div>
                <h2 class="confirmacion-titulo" id="confirmacionTitulo"></h2>
                <p class="confirmacion-mensaje" id="confirmacionMensaje"></p>
                <div class="confirmacion-botones">
                    <button type="button" class="btn-confirmar-cancelar" id="btnConfirmacionCancelar" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn-confirmar" id="btnConfirmacionAceptar">Aceptar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="toastNotificacion" class="toast toast-alerta" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-alerta-cuerpo">
            <div class="toast-alerta-icono" id="toastIconoCirculo">
                <img id="toastIcono" src="" alt="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/">
            </div>
            <div class="toast-alerta-contenido">
                <p class="toast-alerta-titulo" id="toastTitulo"></p>
                <p class="toast-alerta-mensaje" id="toastMensaje"></p>
            </div>
            <button type="button" class="btn-close toast-alerta-cerrar" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>
        <div class="toast-progress-bar" id="toastBarra"></div>
    </div>
</div>

<jsp:include page="../includes/cargando.jsp" />
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/reportes.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/tutorias-grupales-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/alumnos-atendidos-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/canalizados-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/pendientes-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/buscador-alumno.js"></script>
<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";

    // Accesos directos desde gestion-grupos.jsp ("Ver historial de tutorías del grupo" /
    // "Ver historial del alumno"): estos valores los expone ReportesGlobalesServlet.doGet
    // como atributos prefiltro* cuando la URL trae idCarrera/cuatrimestre/letra/matricula.
    // Cadena vacia si no vinieron (nunca null, para poder comparar con !== '' sin problema).
    const PREFILTRO_ID_CARRERA = "${prefiltroIdCarrera}";
    const PREFILTRO_CUATRIMESTRE = "${prefiltroCuatrimestre}";
    const PREFILTRO_LETRA = "${prefiltroLetra}";
    const PREFILTRO_MATRICULA = "${prefiltroMatricula}";
    const PREFILTRO_NOMBRE_ALUMNO = "${empty prefiltroNombreAlumno ? '' : fn:replace(prefiltroNombreAlumno, '\"', '\\\"')}";

    let graficaBarrasCoordinador = null;

    function pintarBarrasCoordinador(data) {
        const ctx = document.getElementById('graficaBarras');

        if (graficaBarrasCoordinador) graficaBarrasCoordinador.destroy();
        graficaBarrasCoordinador = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Pendientes', 'Atendidas', 'Canalizadas'],
                datasets: [{
                    data: [data.totalPendientes, data.totalAtendidos, data.totalCanalizados],
                    backgroundColor: ['#8FD9C4', '#7FA8C9', '#0B2544']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    function buscarReporteCoordinador() {
        cargarReporte({
            contextPath: CONTEXT_PATH,
            filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroTutor', 'filtroDesde', 'filtroHasta', 'filtroMatricula'],
            onDatos: function (data) {
                pintarBarrasCoordinador(data);
                pintarKpi('kpiTutoriasGrupales', data.totalGruposAtendidos);
            }
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarReporteCoordinador);

    // Trayectoria academica del alumno filtrado (Historial a largo plazo): trae TODOS los
    // renglones de ALUMNO_GRUPO_HISTORICO para esa matricula, sin importar carrera/
    // cuatrimestre/grupo actual, y los pinta en orden cronologico.
    function cargarTrayectoriaAlumno(matricula) {
        const seccion = document.getElementById('seccionTrayectoriaAlumno');
        const tbody = document.getElementById('tablaTrayectoriaBody');
        if (!seccion || !tbody || !matricula) return;

        fetch(CONTEXT_PATH + '/reportes-globales?accion=trayectoriaAlumno&matricula=' + encodeURIComponent(matricula))
            .then(function (resp) { return resp.json(); })
            .then(function (lista) {
                if (!lista.length) {
                    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Sin movimientos de grupo registrados.</td></tr>';
                } else {
                    tbody.innerHTML = lista.map(function (t) {
                        return '<tr>' +
                            '<td>' + escaparHtml(t.nombreCarrera) + '</td>' +
                            '<td>' + escaparHtml(t.nivel) + '</td>' +
                            '<td>' + escaparHtml(t.cuatrimestre) + '°</td>' +
                            '<td>' + escaparHtml(t.letra) + '</td>' +
                            '<td>' + escaparHtml(t.generacion) + '</td>' +
                            '<td>' + escaparHtml(t.fechaInicio) + '</td>' +
                            '<td>' + (t.fechaFin ? escaparHtml(t.fechaFin) : '<span class="badge-al-dia px-2 py-1 rounded-figma">Actual</span>') + '</td>' +
                            '</tr>';
                    }).join('');
                }
                seccion.classList.remove('d-none');
            })
            .catch(function (err) { console.error('Error al cargar la trayectoria del alumno:', err); });
    }

    function ocultarTrayectoriaAlumno() {
        const seccion = document.getElementById('seccionTrayectoriaAlumno');
        if (seccion) seccion.classList.add('d-none');
    }

    // Aplica los prefiltros de un acceso directo (Parte B) antes de la primera busqueda:
    // preselecciona Carrera/Cuatrimestre/Grupo, o fija el alumno + su trayectoria cuando
    // el acceso es por matricula. A proposito NO se tocan filtroDesde/filtroHasta: quedan
    // vacios (rango por defecto = historico completo) para que, al entrar por un alumno,
    // sus tutorias individuales/canalizaciones/pendientes de TODOS los años inscritos
    // aparezcan de una vez, no solo las del periodo vigente.
    function aplicarPrefiltrosYBuscar() {
        if (PREFILTRO_ID_CARRERA) document.getElementById('filtroCarrera').value = PREFILTRO_ID_CARRERA;
        if (PREFILTRO_CUATRIMESTRE) document.getElementById('filtroCuatrimestre').value = PREFILTRO_CUATRIMESTRE;
        if (PREFILTRO_LETRA) document.getElementById('filtroGrupo').value = PREFILTRO_LETRA;

        if (PREFILTRO_MATRICULA) {
            document.getElementById('filtroMatricula').value = PREFILTRO_MATRICULA;
            document.getElementById('nombreAlumnoFiltro').textContent = PREFILTRO_NOMBRE_ALUMNO || PREFILTRO_MATRICULA;
            document.getElementById('matriculaAlumnoFiltro').textContent = PREFILTRO_MATRICULA;
            document.getElementById('filtroAlumnoActivo').classList.remove('d-none');
            cargarTrayectoriaAlumno(PREFILTRO_MATRICULA);
        }

        buscarReporteCoordinador();
    }

    document.addEventListener('DOMContentLoaded', aplicarPrefiltrosYBuscar);

    document.getElementById('cardTutoriasGrupales').addEventListener('click', abrirModalTutoriasGrupales);
    document.getElementById('cardTutoriasGrupales').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalTutoriasGrupales();
        }
    });

    function filtrosReporteActuales() {
        return {
            idCarrera: document.getElementById('filtroCarrera').value,
            cuatrimestre: document.getElementById('filtroCuatrimestre').value,
            letra: document.getElementById('filtroGrupo').value,
            idTutor: document.getElementById('filtroTutor').value,
            desde: document.getElementById('filtroDesde').value,
            hasta: document.getElementById('filtroHasta').value,
            matricula: document.getElementById('filtroMatricula').value
        };
    }

    function abrirModalAlumnosAtendidosConFiltros() {
        abrirModalAlumnosAtendidos(filtrosReporteActuales());
    }

    document.getElementById('cardAlumnosAtendidos').addEventListener('click', abrirModalAlumnosAtendidosConFiltros);
    document.getElementById('cardAlumnosAtendidos').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalAlumnosAtendidosConFiltros();
        }
    });

    function abrirModalCanalizadosConFiltros() {
        abrirModalCanalizados(filtrosReporteActuales());
    }

    document.getElementById('cardCanalizados').addEventListener('click', abrirModalCanalizadosConFiltros);
    document.getElementById('cardCanalizados').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalCanalizadosConFiltros();
        }
    });

    function abrirModalPendientesConFiltros() {
        abrirModalPendientes(filtrosReporteActuales());
    }

    document.getElementById('cardPendientes').addEventListener('click', abrirModalPendientesConFiltros);
    document.getElementById('cardPendientes').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalPendientesConFiltros();
        }
    });

    function habilitarClickCompletoFecha(id) {
        const input = document.getElementById(id);
        input.addEventListener('click', function () {
            if (typeof input.showPicker === 'function') {
                input.showPicker();
            }
        });
    }
    habilitarClickCompletoFecha('filtroDesde');
    habilitarClickCompletoFecha('filtroHasta');

    function construirParamsExport(formato) {
        const params = new URLSearchParams();

        const selectCarrera = document.getElementById('filtroCarrera');
        const selectCuatrimestre = document.getElementById('filtroCuatrimestre');
        const selectGrupo = document.getElementById('filtroGrupo');
        const selectTutor = document.getElementById('filtroTutor');
        const desde = document.getElementById('filtroDesde').value;
        const hasta = document.getElementById('filtroHasta').value;

        if (selectCarrera.value) {
            params.append('idCarrera', selectCarrera.value);
            params.append('nombreCarrera', selectCarrera.options[selectCarrera.selectedIndex].text);
        }
        if (selectCuatrimestre.value) {
            params.append('cuatrimestre', selectCuatrimestre.value);
            params.append('nombreCuatrimestre', selectCuatrimestre.options[selectCuatrimestre.selectedIndex].text);
        }
        if (selectGrupo.value) {
            params.append('letra', selectGrupo.value);
            params.append('nombreGrupo', selectGrupo.options[selectGrupo.selectedIndex].text);
        }
        if (selectTutor.value) {
            params.append('idTutor', selectTutor.value);
            params.append('nombreTutor', selectTutor.options[selectTutor.selectedIndex].text);
        }
        if (desde) params.append('desde', desde);
        if (hasta) params.append('hasta', hasta);

        const matriculaFiltro = document.getElementById('filtroMatricula').value;
        if (matriculaFiltro) {
            params.append('matricula', matriculaFiltro);
            params.append('nombreAlumno', document.getElementById('nombreAlumnoFiltro').textContent);
        }

        // Las graficas solo existen como <canvas> en el navegador: se capturan como PNG
        // (misma imagen que ve el coordinador) para que el Excel/PDF las incluya tal cual.
        if (graficaPastelReporte) params.append('imagenPastel', graficaPastelReporte.toBase64Image());
        if (graficaBarrasCoordinador) params.append('imagenBarras', graficaBarrasCoordinador.toBase64Image());

        const accion = formato === 'excel' ? 'exportarExcel' : 'exportarPdf';
        params.append('accion', accion);

        return params;
    }

    async function descargarReporte(formato) {
        const params = construirParamsExport(formato);
        try {
            const resp = await fetch(CONTEXT_PATH + '/reportes-globales', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            });
            if (!resp.ok) throw new Error('Respuesta no válida del servidor');

            const blob = await resp.blob();
            const disposition = resp.headers.get('Content-Disposition') || '';
            const match = disposition.match(/filename="?([^"]+)"?/);
            const nombreArchivo = match ? match[1] : 'reporte_tutorias.' + formato;

            const linkUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = linkUrl;
            a.download = nombreArchivo;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(linkUrl);

            mostrarToast('exito', 'Éxito', 'El reporte se descargó correctamente');
        } catch (err) {
            console.error(err);
            mostrarToast('error', 'Error', 'Ocurrió un error al exportar el reporte');
        }
    }

    document.getElementById('btnExportarExcel').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato Excel.',
            'Sí, descargar',
            () => {
                descargarReporte('excel');
            }
        );
    });

    document.getElementById('btnExportarPdf').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato PDF.',
            'Sí, descargar',
            () => {
                descargarReporte('pdf');
            }
        );
    });

</script>
</body>
</html>