/**
 * Fuerza la recarga de la página cuando se restaura desde la caché de retroceso
 * (bfcache) tras cerrar sesión, y controla el diálogo de confirmación de cierre
 * de sesión del botón "Cerrar sesión".
 * @author J4IROXD
 * @date 2026-08-10
 */
window.addEventListener('pageshow', function (event) {
    if (event.persisted) {
        window.location.reload();
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const btnCerrarSesion = document.getElementById('btnCerrarSesion');

    if (btnCerrarSesion) {
        btnCerrarSesion.addEventListener('click', function () {
            const contextPath = this.getAttribute('data-context-path');
            mostrarConfirmacion(
                'advertencia',
                '¿Cerrar sesión?',
                'Tendrás que iniciar sesión de nuevo para continuar.',
                'Cerrar sesión',
                function () {
                    window.location.href = contextPath + '/logout';
                }
            );
        });
    }
});