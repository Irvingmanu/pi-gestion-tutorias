<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Horario" %>
<%
    // Los datos de disponibilidad ya vienen calculados por SolicitudServlet
    // (accion=nueva), que es el único punto de entrada a esta vista.

    @SuppressWarnings("unchecked")
    List<Horario> listaHorarios = (List<Horario>) request.getAttribute("listaHorarios");
    if (listaHorarios == null) {
        listaHorarios = new ArrayList<>();
    }
    Integer idTutorAsignado = (Integer) request.getAttribute("idTutorAsignado");

    boolean puedeEnviar = (idTutorAsignado != null && !listaHorarios.isEmpty());
    String exito = request.getParameter("exito");

    request.setAttribute("paginaActiva", "solicitud");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Solicitud</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-alumno.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Solicitud
        </div>

        <% if ("enviada".equals(exito)) { %>
        <div class="alert alert-success" role="alert">
            Tu solicitud fue enviada correctamente. El tutor la revisará pronto.
        </div>
        <% } else if ("error".equals(exito)) { %>
        <div class="alert alert-danger" role="alert">
            Ocurrió un error al enviar tu solicitud. Intenta de nuevo.
        </div>
        <% } %>

        <div class="form-wrap-figma" style="max-width: 50%;">

            <form method="post" action="<%= request.getContextPath() %>/SolicitudServlet" id="formSolicitud">
                <input type="hidden" name="accion" value="crear">

                <div class="mb-3">
                    <label for="asunto" class="form-label fw-bold">Asunto</label>
                    <input type="text" class="form-control form-control-figma" id="asunto" name="asunto"
                           maxlength="150" required>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label for="fechaPropuesta" class="form-label fw-bold">Día</label>
                        <select id="fechaPropuesta" name="fechaPropuesta" class="form-select form-control-figma" required>
                            <option value="" selected disabled>Seleccione un día</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="duracionPropuesta" class="form-label fw-bold">Duración</label>
                        <select id="duracionPropuesta" name="duracion" class="form-select form-control-figma" required disabled>
                            <option value="" selected disabled>Seleccione un día primero</option>
                            <option value="1">1 hora</option>
                            <option value="2">2 horas</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="horaPropuesta" class="form-label fw-bold">Hora de inicio</label>
                        <select id="horaPropuesta" name="horaPropuesta" class="form-select form-control-figma" required disabled>
                            <option value="" selected disabled>Seleccione la duración primero</option>
                        </select>
                    </div>
                </div>

                <div class="mb-4">
                    <label for="descripcion" class="form-label fw-bold">Descripción</label>
                    <textarea class="form-control form-control-figma" id="descripcion" name="descripcion"
                              rows="4" required></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <a href="<%= request.getContextPath() %>/AgendaServlet"
                       class="btn btn-cancelar-figma">Cancelar</a>
                    <button type="submit" class="btn btn-figma" id="btnEnviarSolicitud">Enviar</button>
                </div>
            </form>

        </div>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var puedeEnviar = <%= puedeEnviar %>;
        var form = document.getElementById('formSolicitud');

        form.addEventListener('submit', function (evento) {
            if (!puedeEnviar) {
                evento.preventDefault();
                mostrarAlerta(
                    'advertencia',
                    'No puedes enviar tu solicitud',
                    'Todavía no tienes un tutor asignado o tu tutor no tiene horarios de atención registrados. Consulta con el coordinador.'
                );
            }
        });
    });
</script>
<script>
    // Disponibilidad real de las próximas 2 semanas, calculada en
    // SolicitudServlet.construirDisponibilidadJson (cruza HORARIO_ATENCION
    // del tutor con las horas ya ocupadas en SOLICITUD_TUTORIA/SESION_INDIVIDUAL).
    const disponibilidad = ${empty disponibilidadJson ? '{}' : disponibilidadJson};

    document.addEventListener('DOMContentLoaded', function () {
        var selectDia = document.getElementById('fechaPropuesta');
        var selectDuracion = document.getElementById('duracionPropuesta');
        var selectHora = document.getElementById('horaPropuesta');

        Object.keys(disponibilidad).forEach(function (fecha) {
            var opcion = document.createElement('option');
            opcion.value = fecha;
            opcion.textContent = fecha;
            selectDia.appendChild(opcion);
        });

        function reiniciarSelect(select, textoPlaceholder) {
            select.innerHTML = '';
            var opcionVacia = document.createElement('option');
            opcionVacia.value = '';
            opcionVacia.textContent = textoPlaceholder;
            opcionVacia.disabled = true;
            opcionVacia.selected = true;
            select.appendChild(opcionVacia);
        }

        function sumarUnaHora(hora) {
            var partes = hora.split(':');
            var horaSiguiente = parseInt(partes[0], 10) + 1;
            return String(horaSiguiente).padStart(2, '0') + ':' + partes[1];
        }

        // Al elegir el Día, se habilita Duración (sus opciones 1/2 horas ya
        // están fijas en el HTML, aquí solo se activa el select)
        selectDia.addEventListener('change', function () {
            selectDuracion.disabled = false;
            selectDuracion.querySelector('option[value=""]').textContent = 'Seleccione duración';
            selectDuracion.value = '';

            reiniciarSelect(selectHora, 'Seleccione la duración primero');
            selectHora.disabled = true;
        });

        // Al elegir la Duración, se calcula y llena la Hora de inicio
        selectDuracion.addEventListener('change', function () {
            reiniciarSelect(selectHora, 'Seleccione hora');

            var fecha = selectDia.value;
            var duracion = parseInt(selectDuracion.value, 10);
            var horasDelDia = disponibilidad[fecha] || [];

            var horasValidas = horasDelDia.filter(function (hora) {
                if (duracion === 1) {
                    return true;
                }
                // Duración de 2 horas: solo si el bloque inmediatamente
                // siguiente también está disponible ese mismo día.
                return horasDelDia.indexOf(sumarUnaHora(hora)) !== -1;
            });

            horasValidas.forEach(function (hora) {
                var opcion = document.createElement('option');
                opcion.value = hora;
                opcion.textContent = hora;
                selectHora.appendChild(opcion);
            });

            selectHora.disabled = horasValidas.length === 0;
        });
    });
</script>
</body>
</html>
