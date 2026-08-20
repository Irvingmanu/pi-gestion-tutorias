// Logica dedicada a formulario-area.jsp: valida en tiempo real todos los
// formularios ".needs-validation" de la vista (modo "Nueva Área" y modo
// edición) y controla el estado del boton Guardar de cada uno.
//
// El marcado visual (borde rojo + mensaje) es "tradicional": un campo vacio u
// invalido NO se marca en rojo apenas carga la pantalla, solo despues de que
// el usuario lo toca (escribe, pega, sale de el) o el navegador lo autocompleta.
// Ver camposTocados mas abajo. El boton Guardar si evalua la validez real del
// formulario completo desde el inicio, independientemente de que se muestre o
// no el marcado en rojo.
//
// Soporta elementos dinamicos: los motivos que motivos.js agrega/quita del
// DOM no disparan 'input'/'focusout' por si solos, por eso se expone
// window.verificarFormularioArea para que motivos.js pueda pedir una
// revalidacion manual despues de mutar el DOM.
//
// Tambien soporta autocompletado del navegador: Chrome/Edge rellenan los
// campos (ej. con contraseñas guardadas o sugerencias) sin disparar 'input'
// de forma confiable, asi que el marcado visual (borde rojo + mensaje) no
// aparecia aunque el valor fuera invalido. Se agrega el evento 'change'
// (mas confiable con autofill) y una deteccion via CSS animation-name
// ('onAutoFillStart', ver el <style> de formulario-area.jsp) para cubrir
// ese caso tambien.

document.addEventListener('DOMContentLoaded', function () {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        // Busca el botón de submit dentro del form, o si está fuera usando
        // el atributo form="id" (caso de "Guardar" en modo edición).
        let btnGuardar = form.querySelector('button[type="submit"]');
        if (!btnGuardar && form.id) {
            btnGuardar = document.querySelector('button[type="submit"][form="' + form.id + '"]');
        }

        function verificarFormulario() {
            if (!btnGuardar) {
                return;
            }

            let esValido = form.checkValidity();

            // "Nueva Área" no tiene un <input required> estatico para motivos
            // (son hidden inputs que agrega motivos.js), asi que se exige aparte.
            if (form.id === 'formNuevaArea') {
                const contenedorMotivos = document.getElementById('motivosContainer');
                const tieneMotivos = !!contenedorMotivos && contenedorMotivos.querySelectorAll('input[name="motivos[]"]').length > 0;
                esValido = esValido && tieneMotivos;
            }

            btnGuardar.disabled = !esValido;
        }

        // Campos que el usuario ya toco (escribio, pego, salio de el, o el navegador
        // autocompleto): el marcado en rojo (borde + mensaje) solo aparece para campos
        // que estan en este set, nunca para uno que el usuario aun no ha tocado. Evita
        // que "Nueva Área" recien abierta se vea todo en rojo antes de que el usuario
        // haga algo.
        const camposTocados = new WeakSet();

        // Mensaje generico de respaldo, solo se usa si un input required NO
        // tiene su propio atributo data-msg-requerido en el HTML.
        const MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';

        // Encuentra el <div class="invalid-feedback"> asociado a un input.
        // Cubre los 3 casos que existen en la vista:
        //  1) Campos principales: el feedback es el hermano siguiente directo
        //     del input (ej. Nombre Área, Correo).
        //  2) Motivos dinamicos que agrega motivos.js: pueden venir envueltos
        //     en un contenedor con clase '.motivo-row'.
        //  3) Fallback: buscar dentro del contenedor padre inmediato.
        function obtenerFeedback(input) {
            if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
                return input.nextElementSibling;
            }
            const motivoRow = input.closest('.motivo-row');
            if (motivoRow) {
                const fb = motivoRow.querySelector('.invalid-feedback');
                if (fb) return fb;
            }
            return input.parentElement?.querySelector('.invalid-feedback') || null;
        }

        // Marca (o desmarca) un input individual como invalido: borde rojo +
        // mensaje de error visible. Reutilizada por 'input', 'focusout',
        // 'change', la deteccion de autofill y el polling, para no repetir
        // la logica.
        //
        // Si el campo es obligatorio y esta vacio (input.validity.valueMissing)
        // se muestra el mensaje de data-msg-requerido del input (ej. "El
        // nombre del área es obligatorio."), o el generico si no tiene ese
        // atributo. Para cualquier otro tipo de invalidez (ej. no cumple el
        // pattern, o el correo no tiene formato valido) se muestra el mensaje
        // original que ya venia escrito en el HTML (el de "Solo se permiten
        // letras...", "El correo debe terminar en...", etc.), guardado la
        // primera vez en el propio elemento para no perderlo al sobreescribir
        // el texto.
        function marcarValidez(input) {
            const feedback = obtenerFeedback(input);

            // Solo se muestra el borde rojo/mensaje si el campo ya fue tocado por el
            // usuario: uno invalido pero aun no tocado se trata visualmente como si
            // estuviera bien.
            if (!camposTocados.has(input) || input.checkValidity()) {
                input.classList.remove('is-invalid');
                if (feedback) feedback.style.display = 'none';
                return;
            }

            input.classList.add('is-invalid');
            if (!feedback) {
                return;
            }

            if (feedback.dataset.msgPatron === undefined) {
                feedback.dataset.msgPatron = feedback.textContent.trim();
            }

            feedback.textContent = input.validity.valueMissing
                ? (input.dataset.msgRequerido || MENSAJE_CAMPO_OBLIGATORIO)
                : feedback.dataset.msgPatron;

            feedback.style.display = 'block';
        }

        // Delegación de eventos a nivel formulario para detectar inputs
        // dinámicos recién agregados.
        form.addEventListener('input', function (e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
                camposTocados.add(e.target);
                marcarValidez(e.target);
                verificarFormulario();
            }
        });

        form.addEventListener('focusout', function (e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
                camposTocados.add(e.target);
                marcarValidez(e.target);
                verificarFormulario();
            }
        });

        // 'change' cubre casos que 'input' no siempre dispara de forma
        // confiable: autocompletado del navegador, pegar texto y salir del
        // campo sin escribir, o cambios via teclado en algunos navegadores.
        form.addEventListener('change', function (e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
                camposTocados.add(e.target);
                marcarValidez(e.target);
                verificarFormulario();
            }
        });

        // Deteccion de autofill en Chrome/Edge: estos navegadores no
        // disparan 'input' al autocompletar, pero si aplican una animacion
        // CSS que se puede "escuchar" (ver @keyframes onAutoFillStart y la
        // regla input:-webkit-autofill en el <style> del JSP). En cuanto el
        // navegador rellena un campo, esto revalida ese campo al instante.
        form.addEventListener('animationstart', function (e) {
            if (e.animationName === 'onAutoFillStart' &&
                (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT')) {
                camposTocados.add(e.target);
                marcarValidez(e.target);
                verificarFormulario();
            }
        });

        if (form.id === 'formNuevaArea') {
            window.verificarFormularioArea = verificarFormulario;
        }

        // Marca como "tocado" (ver camposTocados) cualquier campo que YA traiga un
        // valor: es la señal de que algo lo lleno (modo edicion con datos guardados,
        // autofill del navegador, o un script/extension que asigno el value
        // directamente por JS sin disparar eventos). Un campo vacio nunca se marca
        // aqui, para no adelantarse a que el usuario lo toque.
        function marcarSiTieneValor(input) {
            if (input.value) {
                camposTocados.add(input);
            }
            marcarValidez(input);
        }

        // Revisa todos los campos del form. Se usa en la verificacion inicial y en
        // el polling de abajo.
        function marcarValidezFormularioCompleto() {
            form.querySelectorAll('input, select').forEach(marcarSiTieneValor);
            verificarFormulario();
        }

        // Verificación inicial: si un campo ya viene con un valor invalido desde que
        // carga la pantalla (ej. en edición, un valor guardado en BD que ya no
        // cumple el patron actual) se marca de una vez en rojo. Un campo vacio (ej.
        // "Nueva Área" recien abierta) no se marca todavia, hasta que el coordinador
        // lo toque.
        marcarValidezFormularioCompleto();

        // Revision periodica (polling): cubre el caso de scripts o extensiones
        // (ej. herramientas de "form filler" para pruebas) que asignan el valor
        // de un input directamente por JS (input.value = "..."). Esa asignacion
        // NO dispara 'input', 'change' ni la animacion de autofill, asi que
        // ningun listener se entera. Revisar cada 500ms es barato para un
        // formulario de este tamaño y garantiza que el marcado visual (rojo +
        // mensaje) siempre refleje el valor real, sin marcar como "tocado" un
        // campo que sigue vacio.
        setInterval(marcarValidezFormularioCompleto, 500);
    });

    // Muestra el mensaje de error (si el servidor lo mandó) usando el mismo
    // sistema de alertas que el resto del sitio.
    const mensajeError = document.body.getAttribute('data-mensaje-error');
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});