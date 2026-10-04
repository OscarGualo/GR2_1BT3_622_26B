<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Interfaz de pedidos de comida" para el CU 03 (carrito y pago).
     Diagrama de secuencia CU03: mensajes 21 (revisa el carrito), 31 (muestra total y saldo de PoliCuenta:
     actividad "Informar valor a pagar") y 32-33 (confirma y paga → confirmar()). --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Mi pedido" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/cliente/pedido">Menú de hoy</a> / Mi pedido
    </nav>
    <div class="titulo-pagina">
        <h1>Mi pedido</h1>
        <p>Revise los platos elegidos y confirme el pago con su PoliCuenta.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty pedido.detalles}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-carrito"/></svg>
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
                <section class="tarjeta" aria-labelledby="titulo-pago">
                    <h2 id="titulo-pago">Pago con PoliCuenta</h2>
                    <dl class="resumen-pago">
                        <div>
                            <dt>PoliCuenta</dt>
                            <dd><c:out value="${sessionScope.cliente.policuenta.numero}"/></dd>
                        </div>
                        <div>
                            <dt>Saldo disponible</dt>
                            <dd>$<fmt:formatNumber value="${saldo}" pattern="0.00"/></dd>
                        </div>
                        <div>
                            <dt>Saldo después del pago</dt>
                            <dd>${saldoSuficiente ? '' : '−'}$<fmt:formatNumber value="${saldoSuficiente ? saldo - total : total - saldo}" pattern="0.00"/></dd>
                        </div>
                        <div class="resumen-pago__total">
                            <dt>Total a pagar</dt>
                            <dd id="total-pedido">$<fmt:formatNumber value="${total}" pattern="0.00"/></dd>
                        </div>
                    </dl>
                    <c:if test="${not saldoSuficiente}">
                        <div class="alerta alerta-error alerta--compacta" role="alert">
                            <svg class="icono" aria-hidden="true"><use href="#i-alerta"/></svg>
                            <span>Saldo insuficiente en PoliCuenta. Quite algún ítem o recargue su PoliCuenta.</span>
                        </div>
                    </c:if>
                    <%-- Mensajes 32-33: confirma y paga → confirmar() --%>
                    <form method="post" action="${pageContext.request.contextPath}/cliente/pedido" id="form-pagar" class="acciones-pie">
                        <input type="hidden" name="accion" value="confirmar">
                        <button type="submit" class="btn btn-primario btn-bloque" ${saldoSuficiente ? '' : 'disabled'}>
                            <svg class="icono" aria-hidden="true"><use href="#i-billetera"/></svg>
                            Confirmar y pagar $<fmt:formatNumber value="${total}" pattern="0.00"/>
                        </button>
                    </form>
                </section>
            </div>
        </c:otherwise>
    </c:choose>
</main>

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
        if (!form || !dialogo || typeof dialogo.showModal !== 'function') return;
        var boton = form.querySelector('button');
        var confirmar = document.getElementById('dialogo-pago-confirmar');
        form.addEventListener('submit', function (evento) {
            evento.preventDefault();
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
<jsp:include page="/vistas/comun/pie.jsp"/>
