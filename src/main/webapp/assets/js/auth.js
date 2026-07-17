// Alterna la visibilidad del campo de contraseña en el login
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
