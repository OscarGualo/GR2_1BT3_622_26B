<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Examen 1B, HU12 (T12.3): ventana de confirmación de la cancelación, compartida por «Mis pedidos» y
     «Mis pedidos programados». Cada formulario .form-cancelar abre esta ventana al enviarse (sin JavaScript
     se envía directo y el servidor aplica las mismas reglas). Atributos del formulario: data-codigo, data-total.
     «Volver» deja el pedido, el saldo y las existencias como estaban (Esc. 1.3). El foco inicial queda en la
     opción segura; al confirmar se deshabilita el botón para que un doble clic no envíe dos solicitudes (Esc. 3.1). --%>
<dialog class="dialogo" id="dialogo-cancelar" aria-labelledby="dialogo-cancelar-titulo" aria-describedby="dialogo-cancelar-texto">
    <div class="dialogo__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-retirar"/></svg></div>
    <h2 id="dialogo-cancelar-titulo">¿Cancelar el pedido?</h2>
    <p id="dialogo-cancelar-texto">Se devolverá el total a su PoliCuenta y el código de retiro dejará de valer.</p>
    <div class="dialogo__acciones">
        <button type="button" class="btn btn-secundario" id="dialogo-cancelar-volver">Volver</button>
        <button type="button" class="btn btn-peligro" id="dialogo-cancelar-confirmar">Confirmar cancelación</button>
    </div>
</dialog>
<script>
    (function () {
        var dialogo = document.getElementById('dialogo-cancelar');
        var formularios = document.querySelectorAll('.form-cancelar');
        if (!dialogo || typeof dialogo.showModal !== 'function' || !formularios.length) return;
        var titulo = document.getElementById('dialogo-cancelar-titulo');
        var texto = document.getElementById('dialogo-cancelar-texto');
        var volver = document.getElementById('dialogo-cancelar-volver');
        var confirmar = document.getElementById('dialogo-cancelar-confirmar');
        var pendiente = null;
        var disparador = null;

        for (var i = 0; i < formularios.length; i++) {
            formularios[i].addEventListener('submit', function (evento) {
                evento.preventDefault();
                pendiente = evento.currentTarget;
                disparador = evento.submitter || pendiente.querySelector('button');
                var codigo = pendiente.getAttribute('data-codigo');
                var total = pendiente.getAttribute('data-total');
                titulo.textContent = '¿Cancelar el pedido ' + codigo + '?';
                texto.textContent = 'Se devolverán $' + total + ' a su PoliCuenta, se repondrán las existencias y el código '
                        + codigo + ' dejará de valer.';
                dialogo.showModal();
                volver.focus();
            });
        }
        volver.addEventListener('click', function () { dialogo.close(); });
        confirmar.addEventListener('click', function () {
            confirmar.disabled = true;
            confirmar.textContent = 'Cancelando…';
            pendiente.submit();
        });
        dialogo.addEventListener('close', function () { if (!confirmar.disabled && disparador) disparador.focus(); });
        dialogo.addEventListener('click', function (evento) { if (evento.target === dialogo) dialogo.close(); });
    })();
</script>
