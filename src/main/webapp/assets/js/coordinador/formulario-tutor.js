// Logica dedicada a formulario-tutor.jsp: valida el rango de horas del
// horario de atencion y controla el estado (habilitado/deshabilitado)
// del boton Guardar en tiempo real.

function validarLimitesHora(input) {
    const minHora = '07:00';
    const maxHora = '21:00';
    if (input.value) {
        if (input.value < minHora) { input.value = minHora; }
        else if (input.value > maxHora) { input.value = maxHora; }
    }

    const inputDesde = document.getElementById('horarioDesde');
    const inputHasta = document.getElementById('horarioHasta');
    if (inputDesde && inputHasta && inputDesde.value) {
        inputHasta.min = inputDesde.value;
        if (inputHasta.value && inputHasta.value <= inputDesde.value) {
            inputHasta.value = inputDesde.value;
        }
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) return;

    const btnGuardar = document.getElementById('btnGuardar');
    const contenedorHorarios = document.getElementById('contenedorHorarios');
    const inputCorreo = document.getElementById('correo');
    const inputsRequeridos = form.querySelectorAll('input[required], select[required]');

    function tieneHorarios() {
        return contenedorHorarios.querySelectorAll('input[name="horariosDispo"]').length > 0;
    }

    function verificarFormulario() {
        let esValido = true;
        inputsRequeridos.forEach(input => {
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        if (!tieneHorarios()) {
            esValido = false;
        }

        btnGuardar.disabled = !esValido;
        return esValido;
    }

    inputsRequeridos.forEach(input => {
        input.addEventListener('input', function () {
            if (this.checkValidity()) {
                this.classList.remove('is-invalid');
            } else {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });

        input.addEventListener('blur', function () {
            if (!this.checkValidity()) {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });
    });

    form.addEventListener('submit', function (evento) {
        if (inputCorreo && !inputCorreo.checkValidity()) {
            evento.preventDefault();
            inputCorreo.classList.add('is-invalid');
            mostrarAlerta('error', 'Correo inválido', 'El correo debe ser un correo institucional válido terminado en @utez.edu.mx.');
            return;
        }

        if (!tieneHorarios()) {
            evento.preventDefault();
            mostrarAlerta('error', 'Horario requerido', 'Debes agregar al menos un horario de atención antes de guardar.');
            return;
        }

        if (!form.checkValidity()) {
            evento.preventDefault();
            mostrarAlerta('error', 'Formulario incompleto', 'Verifica que todos los campos estén completos y correctos.');
        }
    });

    window.actualizarEstadoGuardar = verificarFormulario;

    verificarFormulario();


});

document.getElementById('btnAgregarHorario').addEventListener('click', function(e) {
    e.preventDefault(); // Evita que se agregue inmediatamente

    // (Opcional) Obtener los valores para personalizar el mensaje
    let dia = document.getElementById('select-dia').value;
    let horaInicio = document.getElementById('hora-inicio').value;
    let horaFin = document.getElementById('hora-fin').value;

    // Lanzar la alerta de confirmación
    Swal.fire({
        title: '¿Confirmar horario?',
        text: `¿Estás seguro de agregar el horario de ${dia} de ${horaInicio} a ${horaFin}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#f39c12', // Color naranja de tu diseño
        cancelButtonColor: '#f1f5f9', // Color del botón cancelar
        confirmButtonText: 'Sí, agregar',
        cancelButtonText: 'Cancelar',
        customClass: {
            cancelButton: 'text-dark'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            // ==========================================
            // AQUÍ VA TU LÓGICA ORIGINAL PARA AGREGARLO
            // (Ej. agregar a la tabla HTML o enviar AJAX)
            // ==========================================

            // Mensaje opcional de éxito
            Swal.fire(
                '¡Agregado!',
                'El horario ha sido añadido a la lista.',
                'success'
            );
        }
    });
});