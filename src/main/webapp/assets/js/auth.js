/**
 * Controla el botón de mostrar/ocultar contraseña en el formulario de login,
 * alternando el tipo del input y el ícono del ojo.
 * @author Irvingmanu
 * @date 2026-07-17
 */
document.addEventListener('DOMContentLoaded', function () {
    const toggleBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('toggleIcon');

    if (toggleBtn && passwordInput && toggleIcon) {
        toggleBtn.addEventListener('click', function () {
            const esOculta = passwordInput.type === 'password';
            passwordInput.type = esOculta ? 'text' : 'password';
            toggleIcon.src = esOculta
                ? 'assets/img/login/ojoMostrar.png'
                : 'assets/img/login/ojoOcultar.png';
        });
    }
});
