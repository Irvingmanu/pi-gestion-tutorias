/**
 * Controla el formulario de registro de tutoría grupal del tutor: carga la
 * cuadrícula de asistencia del grupo seleccionado, permite marcar el estatus
 * (Presente/Falta/Justificado) de cada alumno por sesión mediante clics,
 * calcula el porcentaje de asistencia y marca en riesgo a quienes caen bajo el
 * umbral, valida fechas y campos del formulario, y decide si el guardado debe
 * registrar una sesión nueva o solo actualizar asistencia histórica.
 * @author 20253ds074-art
 * @date 2026-08-09
 */
document.addEventListener('DOMContentLoaded', function () {
    var CICLO_ESTATUS = ['Presente', 'Falta', 'Justificado'];
    var UMBRAL_RIESGO = 80;
    var COLUMNA_NUEVA = 'nueva';
    var DIAS_ANTIGUEDAD_MAXIMA = 15;
    var MESES_ES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio',
        'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

    var selectGrupo = document.getElementById('grupoAsignado');
    var contenedorAsistencia = document.getElementById('contenedorAsistencia');
    var theadCuadricula = document.getElementById('theadCuadricula');
    var cuerpoTablaAsistencia = document.getElementById('cuerpoTablaAsistencia');
    var tablaCuadricula = document.getElementById('tablaCuadricula');
    var inputFecha = document.getElementById('fecha');
    var inputAcuerdos = document.getElementById('acuerdos');
    var inputTemas = document.getElementById('temasTratados');
    var inputSoloAsistencia = document.getElementById('inputSoloAsistencia');
    var btnGuardarListaAsistencia = document.getElementById('btnGuardarListaAsistencia');

    var hoyStr = formatearFechaISO(new Date());

    var estadoGrid = null;

    var huboEdicionHistorica = false;
    var huboEdicionColumnaNueva = false;

    /**
     * Indica si los campos superiores del formulario (fecha, acuerdos, temas) están todos vacíos.
     * @returns {boolean} true si ningún campo superior tiene valor capturado
     */
    function camposSuperioresVacios() {
        return !(inputFecha && inputFecha.value)
            && !(inputAcuerdos && inputAcuerdos.value.trim())
            && !(inputTemas && inputTemas.value.trim());
    }

    /**
     * Determina si el guardado actual corresponde a una simple actualización de
     * asistencia histórica (sin registrar una sesión nueva).
     * @returns {boolean} true si solo se editó asistencia de sesiones pasadas y los campos superiores están vacíos
     */
    function puedeGuardarSoloAsistencia() {
        return huboEdicionHistorica && !huboEdicionColumnaNueva && camposSuperioresVacios();
    }

    /**
     * Verifica si existe al menos una celda marcada como "Justificado" en el estado actual de la cuadrícula.
     * @returns {boolean} true si hay alguna sesión justificada para algún alumno
     */
    function hayAlgunaSesionJustificada() {
        if (!estadoGrid) {
            return false;
        }
        return estadoGrid.filas.some(function (fila) {
            return estadoGrid.sesionesServidor.some(function (s) {
                return fila.estados[s.idSesionGrupal] === 'Justificado';
            });
        });
    }

    /**
     * Muestra u oculta el botón "Guardar lista de asistencia" según si aplica un guardado de solo asistencia.
     * @returns {void}
     */
    function actualizarBotonListaAsistencia() {
        if (!btnGuardarListaAsistencia) {
            return;
        }
        var mostrar = puedeGuardarSoloAsistencia() && hayAlgunaSesionJustificada();
        btnGuardarListaAsistencia.style.display = mostrar ? '' : 'none';
    }

    /**
     * Formatea una fecha JavaScript como cadena ISO (yyyy-MM-dd).
     * @param {Date} fecha la fecha a formatear
     * @returns {string} la fecha en formato ISO
     */
    function formatearFechaISO(fecha) {
        var yyyy = fecha.getFullYear();
        var mm = String(fecha.getMonth() + 1).padStart(2, '0');
        var dd = String(fecha.getDate()).padStart(2, '0');
        return yyyy + '-' + mm + '-' + dd;
    }

    if (inputFecha) {
        inputFecha.setAttribute('max', hoyStr);

        var haceNDias = new Date();
        haceNDias.setDate(haceNDias.getDate() - DIAS_ANTIGUEDAD_MAXIMA);
        var minPorAntiguedad = formatearFechaISO(haceNDias);

        var minPorPeriodo = inputFecha.getAttribute('min');
        if (!minPorPeriodo || minPorAntiguedad > minPorPeriodo) {
            inputFecha.setAttribute('min', minPorAntiguedad);
        }
    }

    /**
     * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
     * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
     * @returns {string} el texto escapado listo para insertarse en HTML
     */
    function escaparHtml(texto) {
        var div = document.createElement('div');
        div.textContent = texto == null ? '' : String(texto);
        return div.innerHTML;
    }

    /**
     * Formatea una fecha ISO como "dd/mm" para mostrarla en el encabezado de la cuadrícula.
     * @param {string} fechaIso la fecha en formato ISO (yyyy-MM-dd)
     * @returns {string} la fecha corta formateada, o "--/--" si es inválida
     */
    function formatearFechaCorta(fechaIso) {
        if (!fechaIso) {
            return '--/--';
        }
        var partes = fechaIso.split('-'); // yyyy-mm-dd
        if (partes.length !== 3) {
            return '--/--';
        }
        return partes[2] + '/' + partes[1];
    }

    /**
     * Genera la etiqueta "Mes Año" en español correspondiente a una fecha ISO,
     * usada para agrupar visualmente las columnas de la cuadrícula.
     * @param {string} fechaIso la fecha en formato ISO (yyyy-MM-dd)
     * @returns {string} la etiqueta del mes y año, o "Sesión actual" si no hay fecha
     */
    function etiquetaMes(fechaIso) {
        if (!fechaIso) {
            return 'Sesión actual';
        }
        var partes = fechaIso.split('-');
        var nombreMes = MESES_ES[parseInt(partes[1], 10) - 1] || '';
        nombreMes = nombreMes.charAt(0).toUpperCase() + nombreMes.slice(1);
        return nombreMes + ' ' + partes[0];
    }

    /**
     * Reemplaza el contenido de la tabla de asistencia por una única fila con un mensaje centrado.
     * @param {string} texto el mensaje a mostrar
     * @param {string} claseTexto clase CSS adicional para el estilo del texto
     * @returns {void}
     */
    function mostrarFilaMensaje(texto, claseTexto) {
        if (theadCuadricula) {
            theadCuadricula.innerHTML = '';
        }
        cuerpoTablaAsistencia.innerHTML = '';

        var fila = document.createElement('tr');
        var celda = document.createElement('td');
        celda.className = 'text-center py-3 ' + claseTexto;
        celda.textContent = texto;
        fila.appendChild(celda);
        cuerpoTablaAsistencia.appendChild(fila);
    }

    /**
     * Determina la restricción de edición de una columna de la cuadrícula según su fecha
     * respecto al día de hoy ('pasada', 'hoy' o 'libre').
     * @param {string} fechaIso la fecha de la columna en formato ISO, o vacía si es la columna de sesión nueva sin fecha
     * @returns {string} 'pasada', 'hoy' o 'libre'
     */
    function obtenerRestriccionColumna(fechaIso) {
        if (!fechaIso) {
            return 'libre';
        }
        if (fechaIso < hoyStr) {
            return 'pasada';
        }
        if (fechaIso === hoyStr) {
            return 'hoy';
        }
        return 'libre';
    }

    /**
     * Devuelve el siguiente estatus dentro de un ciclo dado, después del estatus actual.
     * @param {string} actual el estatus actual
     * @param {Array<string>} ciclo la secuencia de estatus permitidos
     * @returns {string} el siguiente estatus en el ciclo
     */
    function siguienteEstatusEnCiclo(actual, ciclo) {
        var idx = ciclo.indexOf(actual);
        return ciclo[(idx + 1) % ciclo.length];
    }

    /**
     * Calcula el siguiente estatus permitido para una celda al hacer clic, respetando
     * la restricción de edición de su columna (sesión pasada, de hoy o libre).
     * @param {string} actual el estatus actual de la celda
     * @param {string} restriccion la restricción de la columna ('pasada', 'hoy' o 'libre')
     * @returns {string} el siguiente estatus permitido
     */
    function siguienteEstatusPermitido(actual, restriccion) {
        if (restriccion === 'pasada') {
            return actual === 'Presente' ? 'Presente' : 'Justificado';
        }
        if (restriccion === 'hoy') {
            return siguienteEstatusEnCiclo(actual, ['Presente', 'Falta']);
        }
        return siguienteEstatusEnCiclo(actual, CICLO_ESTATUS);
    }

    /**
     * Construye la celda de encabezado (th) de una columna de fecha de la cuadrícula,
     * incluyendo el botón para marcar toda la columna a la vez.
     * @param {string} fechaTexto el texto corto de la fecha a mostrar
     * @param {string|number} idSesion el id de la sesión de esa columna (o el marcador de columna nueva)
     * @param {string} restriccion la restricción de edición de la columna
     * @returns {HTMLTableCellElement} el elemento th construido
     */
    function crearThFecha(fechaTexto, idSesion, restriccion) {
        var th = document.createElement('th');
        th.className = 'col-fecha-asist';

        var span = document.createElement('span');
        span.textContent = fechaTexto;
        th.appendChild(span);
        th.appendChild(document.createElement('br'));

        var boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'btn-select-all';
        boton.title = restriccion === 'pasada' ? 'Marcar todos como Justificado' : 'Marcar todos como Presente';
        boton.setAttribute('data-id-sesion', idSesion);
        boton.setAttribute('data-restriccion', restriccion);
        boton.textContent = '✓';
        th.appendChild(boton);

        return th;
    }

    /**
     * Construye la celda (td) con el botón de estatus y el input oculto de una
     * asistencia individual (alumno + sesión).
     * @param {string|number} idSesion el id de la sesión de la columna
     * @param {string} matricula la matrícula del alumno de la fila
     * @param {string} estatus el estatus inicial de la celda
     * @param {string} restriccion la restricción de edición de la columna
     * @returns {HTMLTableCellElement} el elemento td construido
     */
    function crearCeldaAsistencia(idSesion, matricula, estatus, restriccion) {
        var td = document.createElement('td');
        td.className = 'col-fecha-asist';

        var boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'celda-asist';
        boton.setAttribute('data-estatus', estatus);
        boton.setAttribute('data-id-sesion', idSesion);
        boton.setAttribute('data-matricula', matricula);
        boton.setAttribute('data-restriccion', restriccion);
        td.appendChild(boton);

        var inputOculto = document.createElement('input');
        inputOculto.type = 'hidden';
        inputOculto.name = 'celda';
        inputOculto.value = idSesion + '|' + matricula + '|' + estatus;
        td.appendChild(inputOculto);

        return td;
    }

    /**
     * Construye la fila completa (tr) de un alumno en la cuadrícula de asistencia,
     * incluyendo matrícula, número, nombre y una celda de asistencia por cada columna.
     * @param {Object} fila los datos del alumno y su estado de asistencia por sesión
     * @param {number} numero el número consecutivo del alumno en la lista
     * @param {Array<Object>} columnas las columnas (sesiones) a renderizar para esta fila
     * @returns {HTMLTableRowElement} el elemento tr construido
     */
    function crearFilaAlumno(fila, numero, columnas) {
        var tr = document.createElement('tr');

        var tdMatricula = document.createElement('td');
        tdMatricula.className = 'col-matricula-asist';
        tdMatricula.textContent = fila.matricula;
        tr.appendChild(tdMatricula);

        var tdNo = document.createElement('td');
        tdNo.className = 'col-no-asist';
        tdNo.textContent = numero;
        tr.appendChild(tdNo);

        var tdNombre = document.createElement('td');
        tdNombre.className = 'col-nombre-asist';
        tdNombre.textContent = fila.nombreCompleto;
        tr.appendChild(tdNombre);

        columnas.forEach(function (col) {
            var estatus = fila.estados[col.id] || 'Falta';
            tr.appendChild(crearCeldaAsistencia(col.id, fila.matricula, estatus, obtenerRestriccionColumna(col.fechaIso)));
        });

        var tdTotal = document.createElement('td');
        tdTotal.className = 'col-total-asist total-sesiones';
        tr.appendChild(tdTotal);

        var tdPorc = document.createElement('td');
        tdPorc.className = 'col-porc-asist porcentaje-asistencia';
        tr.appendChild(tdPorc);

        return tr;
    }

    /**
     * Construye la lista ordenada de columnas de la cuadrícula (sesiones ya
     * registradas más la columna de sesión nueva), ordenadas por fecha.
     * @returns {Array<Object>} las columnas ordenadas, cada una con id, fechaIso y esNueva
     */
    function obtenerColumnasOrdenadas() {
        var columnas = estadoGrid.sesionesServidor.map(function (s) {
            return { id: s.idSesionGrupal, fechaIso: s.fechaIso, esNueva: false };
        });

        columnas.push({
            id: COLUMNA_NUEVA,
            fechaIso: inputFecha ? inputFecha.value : '',
            esNueva: true
        });

        columnas.sort(function (a, b) {
            var fa = a.fechaIso || '9999-99-99';
            var fb = b.fechaIso || '9999-99-99';
            if (fa === fb) {
                return (a.esNueva ? 1 : 0) - (b.esNueva ? 1 : 0);
            }
            return fa < fb ? -1 : 1;
        });

        return columnas;
    }

    /**
     * Corrige automáticamente el estatus de la columna de sesión nueva cuando su
     * fecha deja de ser libre (pasada u hoy), forzando Justificado o revirtiendo Falta según corresponda.
     * @param {Array<Object>} columnas las columnas actuales de la cuadrícula
     * @returns {void}
     */
    function corregirEstatusColumnaNueva(columnas) {
        var columnaNueva = columnas.find(function (c) { return c.esNueva; });
        if (!columnaNueva) {
            return;
        }
        var restriccion = obtenerRestriccionColumna(columnaNueva.fechaIso);
        if (restriccion === 'libre') {
            return;
        }

        estadoGrid.filas.forEach(function (fila) {
            var actual = fila.estados[COLUMNA_NUEVA];
            if (restriccion === 'pasada' && actual !== 'Presente' && actual !== 'Justificado') {
                fila.estados[COLUMNA_NUEVA] = 'Justificado';
            } else if (restriccion === 'hoy' && actual === 'Justificado') {
                fila.estados[COLUMNA_NUEVA] = 'Falta';
            }
        });
    }

    /**
     * Agrupa las columnas consecutivas que caen en el mismo mes, para construir
     * la fila de encabezado superior de la cuadrícula (con colspan por mes).
     * @param {Array<Object>} columnas las columnas ordenadas de la cuadrícula
     * @returns {Array<Object>} los grupos por mes, cada uno con label y colspan
     */
    function agruparPorMes(columnas) {
        var grupos = [];
        columnas.forEach(function (col) {
            var etiqueta = etiquetaMes(col.fechaIso);
            var ultimo = grupos[grupos.length - 1];
            if (ultimo && ultimo.label === etiqueta) {
                ultimo.colspan++;
            } else {
                grupos.push({ label: etiqueta, colspan: 1 });
            }
        });
        return grupos;
    }

    /**
     * Reconstruye por completo el encabezado y el cuerpo de la tabla de asistencia
     * a partir del estado actual (`estadoGrid`).
     * @returns {void}
     */
    function renderizarDesdeEstado() {
        if (!estadoGrid) {
            return;
        }

        theadCuadricula.innerHTML = '';
        cuerpoTablaAsistencia.innerHTML = '';

        if (!estadoGrid.filas.length) {
            mostrarFilaMensaje('No hay alumnos registrados en este grupo.', 'text-muted');
            return;
        }

        var columnas = obtenerColumnasOrdenadas();
        corregirEstatusColumnaNueva(columnas);
        var meses = agruparPorMes(columnas);

        var fila1 = document.createElement('tr');
        fila1.innerHTML =
            '<th class="col-matricula-asist" rowspan="2">Matrícula</th>' +
            '<th class="col-no-asist" rowspan="2">No.</th>' +
            '<th class="col-nombre-asist" rowspan="2">Nombre del Estudiante</th>';

        meses.forEach(function (mes) {
            fila1.innerHTML += '<th colspan="' + mes.colspan + '">' + escaparHtml(mes.label) + '</th>';
        });

        fila1.innerHTML +=
            '<th class="col-total-asist" rowspan="2">Total<br>Sesiones</th>' +
            '<th class="col-porc-asist" rowspan="2">%<br>Asistencia</th>';
        theadCuadricula.appendChild(fila1);

        var fila2 = document.createElement('tr');
        columnas.forEach(function (col) {
            fila2.appendChild(crearThFecha(formatearFechaCorta(col.fechaIso), col.id, obtenerRestriccionColumna(col.fechaIso)));
        });
        theadCuadricula.appendChild(fila2);

        estadoGrid.filas.forEach(function (fila, indice) {
            cuerpoTablaAsistencia.appendChild(crearFilaAlumno(fila, indice + 1, columnas));
        });

        recalcularTodasLasFilas();
        verificarFormularioGrupal();
    }

    /**
     * Inicializa el estado interno de la cuadrícula (`estadoGrid`) a partir de la
     * respuesta del servidor con las sesiones y alumnos del grupo, y dispara el renderizado.
     * @param {Object} data la respuesta del servidor con `sesiones` y `filas`
     * @returns {void}
     */
    function iniciarEstadoGrid(data) {
        var sesionesServidor = (data && data.sesiones) || [];

        var filas = ((data && data.filas) || []).map(function (f) {
            var estados = {};
            sesionesServidor.forEach(function (s) {
                estados[s.idSesionGrupal] = (f.estatusPorSesion && f.estatusPorSesion[s.idSesionGrupal]) || 'Falta';
            });
            estados[COLUMNA_NUEVA] = 'Falta';
            return { matricula: f.matricula, nombreCompleto: f.nombreCompleto, estados: estados };
        });

        estadoGrid = { sesionesServidor: sesionesServidor, filas: filas };
        huboEdicionHistorica = false;
        huboEdicionColumnaNueva = false;

        renderizarDesdeEstado();
        actualizarBotonListaAsistencia();
    }

    /**
     * Aplica visualmente un nuevo estatus a un botón de celda y sincroniza el
     * input oculto correspondiente que se enviará en el formulario.
     * @param {HTMLElement} boton el botón de la celda de asistencia
     * @param {string} estatus el nuevo estatus a aplicar
     * @returns {void}
     */
    function aplicarEstatus(boton, estatus) {
        boton.setAttribute('data-estatus', estatus);

        var celda = boton.closest('td');
        var inputOculto = celda ? celda.querySelector('input[name="celda"]') : null;
        if (inputOculto) {
            var partes = inputOculto.value.split('|');
            inputOculto.value = partes[0] + '|' + partes[1] + '|' + estatus;
        }
    }

    /**
     * Actualiza el estatus de una celda en el estado interno (`estadoGrid`) del alumno y sesión indicados.
     * @param {string|number} idSesion el id de la sesión (o columna nueva) a actualizar
     * @param {string} matricula la matrícula del alumno
     * @param {string} estatus el nuevo estatus a guardar en el estado
     * @returns {void}
     */
    function actualizarEstadoCelda(idSesion, matricula, estatus) {
        if (!estadoGrid) {
            return;
        }
        var fila = estadoGrid.filas.find(function (f) { return f.matricula === matricula; });
        if (fila) {
            fila.estados[idSesion] = estatus;
        }
    }

    /**
     * Recalcula y actualiza en el DOM el total de sesiones, el porcentaje de
     * asistencia y la clase de riesgo de una fila de alumno.
     * @param {HTMLTableRowElement} fila el elemento tr de la fila del alumno
     * @returns {void}
     */
    function recalcularFila(fila) {
        if (!fila) {
            return;
        }
        var botones = fila.querySelectorAll('.celda-asist');
        if (!botones.length) {
            return;
        }

        var presentes = 0;
        var justificadas = 0;
        botones.forEach(function (boton) {
            var estatus = boton.getAttribute('data-estatus');
            if (estatus === 'Presente') {
                presentes++;
            } else if (estatus === 'Justificado') {
                justificadas++;
            }
        });

        var total = botones.length;
        var denominador = total - justificadas;
        var porcentaje = denominador <= 0 ? 100 : Math.round((presentes * 100) / denominador);

        var tdTotal = fila.querySelector('.total-sesiones');
        if (tdTotal) {
            tdTotal.textContent = total;
        }
        var tdPorcentaje = fila.querySelector('.porcentaje-asistencia');
        if (tdPorcentaje) {
            tdPorcentaje.textContent = porcentaje + '%';
        }

        fila.classList.toggle('fila-riesgo', porcentaje < UMBRAL_RIESGO);
    }

    /**
     * Recalcula el total y porcentaje de asistencia de todas las filas de la tabla.
     * @returns {void}
     */
    function recalcularTodasLasFilas() {
        cuerpoTablaAsistencia.querySelectorAll('tr').forEach(recalcularFila);
    }

    /**
     * Marca que hubo una edición en la columna nueva o en una columna histórica,
     * y refresca la visibilidad del botón de guardado y la validez del formulario.
     * @param {string|number} idSesion el id de la sesión editada (o el marcador de columna nueva)
     * @returns {void}
     */
    function registrarEdicion(idSesion) {
        if (idSesion === COLUMNA_NUEVA) {
            huboEdicionColumnaNueva = true;
        } else {
            huboEdicionHistorica = true;
        }
        actualizarBotonListaAsistencia();
        verificarFormularioGrupal();
    }

    if (tablaCuadricula) {
        tablaCuadricula.addEventListener('click', function (evento) {
            var boton = evento.target.closest('.celda-asist');
            if (boton) {
                var idSesionBoton = boton.getAttribute('data-id-sesion');
                var matriculaBoton = boton.getAttribute('data-matricula');
                var restriccionBoton = boton.getAttribute('data-restriccion');
                var estatusActual = boton.getAttribute('data-estatus');
                var nuevoEstatus = siguienteEstatusPermitido(estatusActual, restriccionBoton);
                if (nuevoEstatus === estatusActual) {
                    return;
                }
                aplicarEstatus(boton, nuevoEstatus);
                actualizarEstadoCelda(idSesionBoton, matriculaBoton, nuevoEstatus);
                recalcularFila(boton.closest('tr'));
                registrarEdicion(idSesionBoton);
                return;
            }

            var botonTodos = evento.target.closest('.btn-select-all');
            if (botonTodos) {
                var idSesion = botonTodos.getAttribute('data-id-sesion');
                var restriccionColumna = botonTodos.getAttribute('data-restriccion');
                tablaCuadricula.querySelectorAll('.celda-asist[data-id-sesion="' + idSesion + '"]').forEach(function (b) {
                    var actual = b.getAttribute('data-estatus');
                    var destino = restriccionColumna === 'pasada'
                        ? (actual === 'Presente' ? 'Presente' : 'Justificado')
                        : 'Presente';
                    if (destino === actual) {
                        return;
                    }
                    aplicarEstatus(b, destino);
                    actualizarEstadoCelda(idSesion, b.getAttribute('data-matricula'), destino);
                    recalcularFila(b.closest('tr'));
                });
                registrarEdicion(idSesion);
            }
        });
    }

    if (inputFecha) {
        inputFecha.addEventListener('input', renderizarDesdeEstado);
        inputFecha.addEventListener('change', renderizarDesdeEstado);
    }

    /**
     * Carga vía fetch la cuadrícula de asistencia (alumnos y sesiones) del grupo
     * seleccionado en el select de grupo, o limpia la vista si no hay grupo seleccionado.
     * @returns {void}
     */
    function cargarAsistencia() {
        if (!selectGrupo) {
            return;
        }

        var valor = selectGrupo.value;

        if (!valor) {
            estadoGrid = null;
            contenedorAsistencia.style.display = 'none';
            theadCuadricula.innerHTML = '';
            cuerpoTablaAsistencia.innerHTML = '';
            actualizarBotonListaAsistencia();
            return;
        }

        contenedorAsistencia.style.display = 'block';
        mostrarFilaMensaje('Cargando alumnos...', 'text-muted');

        var url = APP_CONTEXT + '/tutoria-grupal?accion=obtenerCuadricula'
            + '&idGrupo=' + encodeURIComponent(valor);

        fetch(url)
            .then(function (resp) { return resp.json(); })
            .then(iniciarEstadoGrid)
            .catch(function () {
                mostrarFilaMensaje('Ocurrió un error al cargar la lista de alumnos.', 'text-danger');
            });
    }

    if (selectGrupo) {
        selectGrupo.addEventListener('change', cargarAsistencia);

        if (selectGrupo.value) {
            cargarAsistencia();
        }
    }

    var formRegistroGrupal = document.getElementById('formRegistroGrupal');

    var inputsRequeridos;
    var btnGuardarGrupal;
    /**
     * Revalida los campos obligatorios del formulario de tutoría grupal y habilita
     * el botón de guardar si son válidos o si aplica un guardado de solo asistencia.
     * Se reasigna con la implementación real solo cuando el formulario existe en la página.
     * @returns {void}
     */
    var verificarFormularioGrupal = function () {};

    if (formRegistroGrupal) {
        btnGuardarGrupal = document.getElementById('btnGuardarGrupal');
        inputsRequeridos = formRegistroGrupal.querySelectorAll('input[required], select[required], textarea[required]');

        verificarFormularioGrupal = function () {
            var esValido = true;
            inputsRequeridos.forEach(function (input) {
                if (!input.checkValidity()) {
                    esValido = false;
                }
            });
            if (btnGuardarGrupal) {
                btnGuardarGrupal.disabled = !(esValido || puedeGuardarSoloAsistencia());
            }
        };

        inputsRequeridos.forEach(function (input) {
            input.addEventListener('input', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormularioGrupal();
            });

            input.addEventListener('change', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormularioGrupal();
            });

            input.addEventListener('blur', function () {
                if (!this.checkValidity()) {
                    this.classList.add('is-invalid');
                }
                verificarFormularioGrupal();
            });
        });

        verificarFormularioGrupal();
    }

    if (formRegistroGrupal) {
        formRegistroGrupal.addEventListener('submit', function (e) {
            e.preventDefault();

            if (puedeGuardarSoloAsistencia()) {
                if (inputSoloAsistencia) {
                    inputSoloAsistencia.value = 'true';
                }

                mostrarConfirmacion(
                    'advertencia',
                    '¿Guardar cambios de asistencia?',
                    'Se actualizará la asistencia marcada en la cuadrícula (no se registrará una sesión nueva).',
                    'Sí, guardar',
                    function () {
                        formRegistroGrupal.submit();
                    }
                );
                return;
            }

            if (inputSoloAsistencia) {
                inputSoloAsistencia.value = 'false';
            }

            if (!formRegistroGrupal.checkValidity()) {
                inputsRequeridos.forEach(function (input) {
                    if (!input.checkValidity()) {
                        input.classList.add('is-invalid');
                    }
                });
                verificarFormularioGrupal();
                return;
            }

            if (inputFecha && inputFecha.value) {
                var fechaSeleccionada = new Date(inputFecha.value + 'T00:00:00');
                var fechaHoy = new Date();
                fechaHoy.setHours(0, 0, 0, 0);

                if (fechaSeleccionada.getTime() > fechaHoy.getTime()) {
                    mostrarAlerta('advertencia', 'Fecha inválida', 'No se pueden registrar tutorías con fecha futura.');
                    return;
                }

                var limiteAntiguedad = new Date(fechaHoy);
                limiteAntiguedad.setDate(limiteAntiguedad.getDate() - DIAS_ANTIGUEDAD_MAXIMA);
                if (fechaSeleccionada.getTime() < limiteAntiguedad.getTime()) {
                    mostrarAlerta('advertencia', 'Fecha inválida', 'Solo se permite registrar sesiones de tutoría con un máximo de 15 días de antigüedad.');
                    return;
                }
            }

            mostrarConfirmacion(
                'advertencia',
                '¿Registrar tutoría grupal?',
                'Estás a punto de registrar la sesión y guardar la asistencia completa del grupo.',
                'Sí, guardar',
                function () {
                    formRegistroGrupal.submit();
                }
            );
        });
    }

    var parametros = new URLSearchParams(window.location.search);
    var exito = parametros.get('exito');
    var errorUrl = parametros.get('error');

    if (exito === 'grupal_guardada') {
        mostrarToast('exito', 'Guardado', 'La tutoría grupal fue registrada correctamente.');
    } else if (exito === 'asistencia_actualizada') {
        mostrarToast('exito', 'Guardado', 'La asistencia se actualizó correctamente.');
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
    } else if (errorUrl === 'fecha_futura') {
        mostrarAlerta('advertencia', 'Fecha inválida', 'No se pueden registrar tutorías con fecha futura.');
    } else if (errorUrl === 'fecha_fuera_rango_15_dias') {
        mostrarAlerta('advertencia', 'Fecha inválida', 'Solo se permite registrar sesiones de tutoría con un máximo de 15 días de antigüedad.');
    }

    if (exito || errorUrl) {
        parametros.delete('exito');
        parametros.delete('error');
        var query = parametros.toString();
        window.history.replaceState(null, null, window.location.pathname + (query ? '?' + query : ''));
    }
});
