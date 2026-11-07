<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Examen 1B, HU14 «Consultar pedidos programados» (T14.3), HU12 (T12.3 cancelar) y HU17 (T17.3 «Cambiar franja»).
     Atributos: pedidos (List<Pedido> Pagados con franja, ordenados por hora), franjas (DisponibilidadFranja de hoy
     que aún no empiezan), ahora (LocalTime) y fechaHoy (LocalDate).
     Los botones siguen visibles aunque el plazo haya vencido: el servidor responde con el motivo (HU12 Esc. 2.1,
     HU17 Esc. 2.1) y el texto de cada pedido dice hasta qué hora se puede cancelar o cambiar. --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Mis pedidos programados" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/cliente/pedido">Menú de hoy</a> / Pedidos programados
    </nav>
    <div class="titulo-pagina">
        <h1>Mis pedidos programados</h1>
        <p>Pedidos pagados que retirará hoy en la franja que eligió. Puede cancelar o cambiar la franja hasta 30 minutos antes.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty pedidos}">
            <section class="vacio" aria-labelledby="titulo-vacio">
                <h2 id="titulo-vacio">No tienes pedidos programados</h2>
                <p>Cuando programes un pedido para llevar, aparecerá aquí con su franja y su código de retiro.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido">Ver menú de hoy</a>
            </section>
        </c:when>
        <c:otherwise>
            <ul class="programados" aria-label="Pedidos programados pendientes de retiro">
                <c:forEach var="pedido" items="${pedidos}">
                    <c:set var="limite" value="${pedido.franja.horaInicio.minusMinutes(30)}"/>
                    <c:set var="enPlazo" value="${not ahora.isAfter(limite)}"/>
                    <li class="programado" id="pedido-${pedido.id}">
                        <div class="programado__franja">
                            <h2 class="programado__hora"><c:out value="${pedido.franja.horaInicio}"/>–<c:out value="${pedido.franja.horaInicio.plusMinutes(15)}"/></h2>
                            <p class="programado__codigo"><c:out value="${empty pedido.comprobante.codigo ? '—' : pedido.comprobante.codigo}"/></p>
                        </div>
                        <div class="programado__detalle">
                            <ul class="programado__items">
                                <c:forEach var="detalle" items="${pedido.detalles}">
                                    <li><c:out value="${detalle.item.nombre}"/> × ${detalle.cantidad}</li>
                                </c:forEach>
                            </ul>
                            <p class="programado__total">Total <strong>$<fmt:formatNumber value="${pedido.total}" pattern="0.00"/></strong></p>
                            <p class="programado__plazo">
                                <c:choose>
                                    <c:when test="${enPlazo}">Puede cancelar o cambiar la franja hasta las <c:out value="${limite}"/>.</c:when>
                                    <c:otherwise><span class="estado estado-creado">Plazo vencido</span> Ya no se puede cancelar ni cambiar la franja.</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <div class="programado__acciones">
                            <%-- HU17: abre «Cambiar franja» con el pedido y su franja actual --%>
                            <button type="button" class="btn btn-secundario btn-chico boton-cambiar"
                                    data-id="${pedido.id}" data-codigo="${pedido.comprobante.codigo}"
                                    data-actual="${pedido.franja.horaInicio}">
                                <svg class="icono" aria-hidden="true"><use href="#i-reloj"/></svg>
                                Cambiar franja<span class="solo-lector"> del pedido <c:out value="${pedido.comprobante.codigo}"/></span>
                            </button>
                            <%-- HU12: la confirmación la pide dialogoCancelar.jsp --%>
                            <form method="post" action="${pageContext.request.contextPath}/cliente/cancelar" class="form-cancelar"
                                  data-codigo="${pedido.comprobante.codigo}" data-total="<fmt:formatNumber value="${pedido.total}" pattern="0.00"/>">
                                <input type="hidden" name="accion" value="cancelar">
                                <input type="hidden" name="idPedido" value="${pedido.id}">
                                <input type="hidden" name="origen" value="programados">
                                <button type="submit" class="btn btn-secundario btn-borde-peligro btn-chico">
                                    <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
                                    Cancelar pedido<span class="solo-lector"> <c:out value="${pedido.comprobante.codigo}"/></span>
                                </button>
                            </form>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>

    <jsp:include page="/vistas/comun/horaDemostracion.jsp">
        <jsp:param name="volver" value="/cliente/programados"/>
    </jsp:include>
</main>

<jsp:include page="/vistas/comun/dialogoCancelar.jsp"/>

<%-- HU17: «Cambiar franja». El selector repite el de la compra, con la disponibilidad actual --%>
<dialog class="dialogo dialogo--ancho" id="dialogo-cambiar" aria-labelledby="dialogo-cambiar-titulo">
    <form method="post" action="${pageContext.request.contextPath}/cliente/programados" id="form-cambiar">
        <div class="dialogo__icono dialogo__icono--info"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-reloj"/></svg></div>
        <h2 id="dialogo-cambiar-titulo">Cambiar la franja de retiro</h2>
        <input type="hidden" name="accion" value="reprogramar">
        <input type="hidden" name="idPedido" id="cambiar-id" value="">
        <input type="hidden" name="fecha" value="${fechaHoy}">
        <fieldset class="franjas-retiro franjas-retiro--dialogo">
            <legend>Nueva franja de hoy</legend>
            <c:choose>
                <c:when test="${empty franjas}">
                    <p class="franjas-retiro__vacio">Ya no quedan franjas de retiro para hoy.</p>
                </c:when>
                <c:otherwise>
                    <ul class="franjas-retiro__lista">
                        <c:forEach var="franja" items="${franjas}">
                            <li class="franja-fila" data-inicio="${franja.inicio}">
                                <input type="radio" name="franja" id="cambiar-${franja.inicio}" value="${franja.inicio}" ${franja.elegible ? '' : 'disabled'}>
                                <label for="cambiar-${franja.inicio}">
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
        <div class="dialogo__acciones">
            <button type="button" class="btn btn-secundario" id="cambiar-volver">Volver</button>
            <button type="submit" class="btn btn-primario" id="cambiar-confirmar">Cambiar franja</button>
        </div>
    </form>
</dialog>
<script>
    (function () {
        var dialogo = document.getElementById('dialogo-cambiar');
        var botones = document.querySelectorAll('.boton-cambiar');
        if (!dialogo || typeof dialogo.showModal !== 'function' || !botones.length) return;
        var form = document.getElementById('form-cambiar');
        var titulo = document.getElementById('dialogo-cambiar-titulo');
        var confirmar = document.getElementById('cambiar-confirmar');
        var disparador = null;
        for (var i = 0; i < botones.length; i++) {
            botones[i].addEventListener('click', function (evento) {
                disparador = evento.currentTarget;
                document.getElementById('cambiar-id').value = disparador.getAttribute('data-id');
                titulo.textContent = 'Cambiar la franja del pedido ' + disparador.getAttribute('data-codigo');
                // La franja actual no se puede volver a elegir
                var actual = disparador.getAttribute('data-actual');
                var filas = form.querySelectorAll('.franja-fila');
                for (var j = 0; j < filas.length; j++) {
                    var radio = filas[j].querySelector('input');
                    var esActual = filas[j].getAttribute('data-inicio') === actual;
                    filas[j].classList.toggle('franja-fila--actual', esActual);
                    if (radio.dataset.origen === undefined) radio.dataset.origen = String(radio.disabled);
                    radio.disabled = radio.dataset.origen === 'true' || esActual;
                    radio.checked = false;
                }
                dialogo.showModal();
                document.getElementById('cambiar-volver').focus();
            });
        }
        document.getElementById('cambiar-volver').addEventListener('click', function () { dialogo.close(); });
        form.addEventListener('submit', function () {
            confirmar.disabled = true;
            confirmar.textContent = 'Cambiando…';
        });
        dialogo.addEventListener('close', function () { if (disparador) disparador.focus(); });
        dialogo.addEventListener('click', function (evento) { if (evento.target === dialogo) dialogo.close(); });
    })();
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
