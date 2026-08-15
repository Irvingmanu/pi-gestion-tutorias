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