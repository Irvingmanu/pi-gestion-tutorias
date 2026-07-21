document.querySelectorAll('.toggle-password-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const targetId = btn.getAttribute('data-target');
        const input = document.getElementById(targetId);
        const icon = btn.querySelector('.toggle-icon');

        if (input.type === 'password') {
            input.type = 'text';
            icon.src = 'assets/img/login/ojoMostrar.png';
        } else {
            input.type = 'password';
            icon.src = 'assets/img/login/ojoOcultar.png';
        }
    });
});