<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Interfaz de pedidos de comida" para el CU 03 (carrito y pago).
     Diagrama de secuencia CU03: mensajes 21 (revisa el carrito), 31 (muestra total y saldo de PoliCuenta:
     actividad "Informar valor a pagar") y 32-33 (confirma y paga → confirmar()).
     Examen 1B, HU11 (T11.4) y HU16 (T16.4): el cliente elige entre «Pagar ahora» (compra inmediata del
     Release 1) y «Programar para llevar», con el selector de franjas de hoy y su disponibilidad.
     Atributos nuevos: franjas (List<DisponibilidadFranja>) y fechaHoy (LocalDate). --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Mi pedido" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/cliente/pedido">Menú de hoy</a> / Mi pedido
    </nav>
    <div class="titulo-pagina">
        <h1>Mi pedido</h1>
        <p>Revise los platos elegidos y pague con su PoliCuenta, ahora o con hora de retiro.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty pedido.detalles}">
            <section class="tarjeta vacio">
                <h2>Su pedido está vacío</h2>
                <p>Agregue platos o productos desde el menú de hoy.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido">Ver menú de hoy</a>
            </section>
        </c:when>
        <c:otherwise>
            <div class="rejilla-2 rejilla-2--carrito">
                <section class="tarjeta" aria-labelledby="titulo-detalle">
                    <h2 id="titulo-detalle">Detalle</h2>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <caption class="solo-lector">Platos y productos de su pedido</caption>
                            <thead>
                            <tr>
                                <th scope="col">Ítem</th>
                                <th scope="col" class="numero">Cant.</th>
                                <th scope="col" class="numero">Precio</th>
                                <th scope="col" class="numero">Subtotal</th>
                                <th scope="col" class="acciones"><span class="solo-lector">Acción</span></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="detalle" items="${pedido.detalles}">
                                <tr>
                                    <td data-etiqueta="Ítem"><strong><c:out value="${detalle.item.nombre}"/></strong></td>
                                    <td data-etiqueta="Cantidad" class="numero"><span class="cantidad-fija">${detalle.cantidad}</span></td>
                                    <td data-etiqueta="Precio" class="numero">$<fmt:formatNumber value="${detalle.precioUnitario}" pattern="0.00"/></td>
                                    <td data-etiqueta="Subtotal" class="numero">$<fmt:formatNumber value="${detalle.cantidad * detalle.precioUnitario}" pattern="0.00"/></td>
                                    <td class="acciones">
                                        <form method="post" action="${pageContext.request.contextPath}/cliente/pedido">
                                            <input type="hidden" name="accion" value="quitar">
                                            <input type="hidden" name="idItem" value="${detalle.item.id}">
                                            <button type="submit" class="btn btn-secundario btn-chico">
                                                <svg class="icono" aria-hidden="true"><use href="#i-quitar"/></svg>
                                                Quitar<span class="solo-lector"> <c:out value="${detalle.item.nombre}"/></span>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <div class="acciones-pie acciones-pie--inicio">
                        <a class="btn btn-secundario" href="${pageContext.request.contextPath}/cliente/pedido">
                            <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                            Seguir eligiendo
                        </a>
                    </div>
                </section>

                <%-- Mensaje 31: total del pedido y saldo de PoliCuenta --%>
                <section class="pago" aria-labelledby="titulo-pago">
                    <h2 id="titulo-pago">Pago con PoliCuenta</h2>
                    <p class="pago__cuenta">Cuenta <c:out value="${sessionScope.cliente.policuenta.numero}"/></p>
                    <%-- pages/carrito.md: Total a pagar es la cifra principal; los saldos, cifras secundarias --%>
                    <dl class="cifras cifras--pago">
                        <div class="cifra cifra--principal">
                            <dt>Total a pagar</dt>
                            <dd id="total-pedido">$<fmt:formatNumber value="${total}" pattern="0.00"/></dd>
                        </div>
                        <div class="cifra cifra--secundaria">
                            <dt>Saldo disponible</dt>
                            <dd>$<fmt:formatNumber value="${saldo}" pattern="0.00"/></dd>
                        </div>
                        <div class="cifra cifra--secundaria${saldoSuficiente ? '' : ' cifra--negativa'}">
                            <dt>Saldo después del pago</dt>
                            <dd>${saldoSuficiente ? '' : '−'}$<fmt:formatNumber value="${saldoSuficiente ? saldo - total : total - saldo}" pattern="0.00"/></dd>
                        </div>
                    </dl>
                    <c:if test="${not saldoSuficiente}">
                        <div class="alerta alerta-error alerta--compacta" role="alert">
                            <svg class="icono" aria-hidden="true"><use href="#i-alerta"/></svg>
                            <span>Saldo insuficiente en PoliCuenta. Quite algún ítem o recargue su PoliCuenta.</span>
                        </div>
                    </c:if>
                    <%-- Mensajes 32-33: confirma y paga → confirmar(). Con modo=programar, la misma acción reserva
                         además un lugar en la franja elegida (HU11). El servidor decide: si el saldo no alcanza
                         responde «Saldo insuficiente en PoliCuenta» (HU11 Esc. 2.1), por eso el botón no se bloquea. --%>
                    <form method="post" action="${pageContext.request.contextPath}/cliente/pedido" id="form-pagar" class="pago__form">
                        <input type="hidden" name="accion" value="confirmar">
                        <fieldset class="selector-tipo">
                            <legend>Cuándo retira su pedido</legend>
                            <div class="selector-tipo__opciones">
                                <input type="radio" name="modo" id="modo-ahora" value="ahora" checked>
                                <label for="modo-ahora">Pagar ahora</label>
                                <input type="radio" name="modo" id="modo-programar" value="programar">
                                <label for="modo-programar">Programar para llevar</label>
                            </div>
                        </fieldset>

                        <fieldset class="franjas-retiro" id="franjas-retiro" aria-describedby="franjas-ayuda">
                            <legend>Franja de retiro de hoy</legend>
                            <input type="hidden" name="fecha" value="${fechaHoy}">
                            <c:choose>
                                <c:when test="${empty franjas}">
                                    <p class="franjas-retiro__vacio" id="franjas-ayuda">Ya no quedan franjas de retiro para hoy. Puede pagar ahora.</p>
                                </c:when>
                                <c:otherwise>
                                    <p class="franjas-retiro__ayuda" id="franjas-ayuda">Elija una franja de 15 minutos. Podrá cancelarla o cambiarla hasta 30 minutos antes.</p>
                                    <ul class="franjas-retiro__lista">
                                        <c:forEach var="franja" items="${franjas}">
                                            <li class="franja-fila">
                                                <input type="radio" name="franja" id="franja-${franja.inicio}" value="${franja.inicio}"
                                                       data-etiqueta="${franja.etiqueta}" ${franja.elegible ? '' : 'disabled'}>
                                                <label for="franja-${franja.inicio}">
                                                    <span class="franja-fila__hora"><c:out value="${franja.etiqueta}"/></span>
                                                    <c:choose>
                                                        <c:when test="${franja.elegible}">
                                                            <span class="franja-fila__lugares"><c:out value="${franja.textoLugares}"/></span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="estado ${franja.cerrada ? 'estado-cerrado' : 'estado-agotado'}"><c:out value="${franja.textoLugares}"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </label>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </c:otherwise>
                            </c:choose>
                        </fieldset>

                        <div class="acciones-pie">
                            <button type="submit" class="btn btn-primario btn-bloque" id="boton-pagar"
                                    data-total="<fmt:formatNumber value="${total}" pattern="0.00"/>">
                                <svg class="icono" aria-hidden="true"><use href="#i-billetera"/></svg>
                                <span id="boton-pagar-texto">Confirmar y pagar $<fmt:formatNumber value="${total}" pattern="0.00"/></span>
                            </button>
                        </div>
                    </form>
                </section>
            </div>
            <jsp:include page="/vistas/comun/horaDemostracion.jsp">
                <jsp:param name="volver" value="/cliente/pedido?vista=carrito"/>
            </jsp:include>
        </c:otherwise>
    </c:choose>
</main>

<c:if test="${not empty pedido.detalles}">
<dialog class="dialogo" id="dialogo-pago" aria-labelledby="dialogo-pago-titulo" aria-describedby="dialogo-pago-texto">
    <div class="dialogo__icono dialogo__icono--info"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-billetera"/></svg></div>
    <h2 id="dialogo-pago-titulo">¿Confirmar el pago de $<fmt:formatNumber value="${total}" pattern="0.00"/>?</h2>
    <p id="dialogo-pago-texto">Se descontará de su PoliCuenta <c:out value="${sessionScope.cliente.policuenta.numero}"/> y recibirá su código de retiro.</p>
    <div class="dialogo__acciones">
        <button type="button" class="btn btn-secundario" id="dialogo-pago-cancelar">Cancelar</button>
        <button type="button" class="btn btn-primario" id="dialogo-pago-confirmar">Pagar ahora</button>
    </div>
</dialog>
<script>
    (function () {
        var form = document.getElementById('form-pagar');
        var dialogo = document.getElementById('dialogo-pago');
        if (!form || !dialogo) return;
        var boton = document.getElementById('boton-pagar');
        var textoBoton = document.getElementById('boton-pagar-texto');
        var total = boton.getAttribute('data-total');
        var franjas = document.getElementById('franjas-retiro');
        var modos = form.querySelectorAll('input[name="modo"]');
        var cuenta = '<c:out value="${sessionScope.cliente.policuenta.numero}"/>';

        function modo() { return form.querySelector('input[name="modo"]:checked').value; }
        function franjaElegida() { return form.querySelector('input[name="franja"]:checked'); }

        // La franja solo se muestra al programar; sin JavaScript se ven las dos opciones
        function actualizar() {
            var programar = modo() === 'programar';
            franjas.hidden = !programar;
            textoBoton.textContent = (programar ? 'Programar y pagar $' : 'Confirmar y pagar $') + total;
        }
        for (var i = 0; i < modos.length; i++) modos[i].addEventListener('change', actualizar);
        actualizar();

        if (typeof dialogo.showModal !== 'function') return;
        var confirmar = document.getElementById('dialogo-pago-confirmar');
        var titulo = document.getElementById('dialogo-pago-titulo');
        var texto = document.getElementById('dialogo-pago-texto');
        form.addEventListener('submit', function (evento) {
            // Sin franja elegida no hay nada que confirmar: el servidor pide elegir una (HU11 Esc. 3.4)
            if (modo() === 'programar' && !franjaElegida()) return;
            evento.preventDefault();
            if (modo() === 'programar') {
                titulo.textContent = '¿Programar y pagar $' + total + '?';
                texto.textContent = 'Se descontará de su PoliCuenta ' + cuenta + ' y se reservará su lugar en la franja '
                        + franjaElegida().getAttribute('data-etiqueta') + '. Recibirá su código de retiro.';
                confirmar.textContent = 'Programar y pagar';
            } else {
                titulo.textContent = '¿Confirmar el pago de $' + total + '?';
                texto.textContent = 'Se descontará de su PoliCuenta ' + cuenta + ' y recibirá su código de retiro.';
                confirmar.textContent = 'Pagar ahora';
            }
            dialogo.showModal();
            document.getElementById('dialogo-pago-cancelar').focus();
        });
        document.getElementById('dialogo-pago-cancelar').addEventListener('click', function () { dialogo.close(); });
        confirmar.addEventListener('click', function () {
            confirmar.disabled = true;
            confirmar.textContent = 'Procesando pago…';
            form.submit();
        });
        dialogo.addEventListener('close', function () { if (!confirmar.disabled) boton.focus(); });
        dialogo.addEventListener('click', function (evento) { if (evento.target === dialogo) dialogo.close(); });
    })();
</script>
</c:if>
<jsp:include page="/vistas/comun/pie.jsp"/>
