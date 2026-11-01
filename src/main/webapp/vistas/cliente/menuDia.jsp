<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Interfaz de pedidos de comida" (Diagrama de robustez del incremento 2) para el CU 03.
     Diagrama de secuencia CU03 parte 1: mensajes 1 (solicita realizar pedido), 11 (muestra ítems "Disponible"
     con stock), 12-13 (elige ítem y cantidad → agregarItem(idItem, cantidad)) y 20 ("Ítem agregado"). --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Menú de hoy" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <div class="titulo-pagina">
        <h1>Menú de hoy</h1>
        <%-- Línea de contexto (pages/menu-dia.md): fecha larga en español; el precio sigue en en_US (punto decimal) --%>
        <p>
            <c:choose>
                <c:when test="${not empty menuHoy}">
                    <fmt:setLocale value="es_EC"/>
                    <span class="fecha-larga"><fmt:formatDate value="${menuHoy.fecha}" pattern="EEEE d 'de' MMMM"/></span> ·
                    <fmt:setLocale value="en_US"/>
                    se paga con PoliCuenta
                </c:when>
                <c:otherwise>Se paga con PoliCuenta</c:otherwise>
            </c:choose>
        </p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <%-- Último código como ticket compacto (componente Ticket del MASTER, no alerta) --%>
    <c:if test="${not empty sessionScope.ultimoCodigo}">
        <aside class="ticket ticket--compacto ticket--aviso" aria-label="Su último código de retiro">
            <p class="ticket__etiqueta">Su último código de retiro</p>
            <p class="ticket__codigo"><c:out value="${sessionScope.ultimoCodigo}"/></p>
            <div class="ticket__pie">
                <p class="ticket__nota">Preséntelo en la ventanilla del comedor.</p>
                <a href="${pageContext.request.contextPath}/cliente/pedido?vista=comprobante">Ver comprobante</a>
            </div>
        </aside>
    </c:if>

    <c:choose>
        <c:when test="${empty menuHoy or menuHoy.estado != 'Publicado'}">
            <section class="tarjeta vacio">
                <%-- Fase 7: texto válido tanto si el menú aún no se publica como si la venta de hoy ya se cerró
                     (buscarMenuDelDia() no devuelve menús "Cerrado", así que la vista no distingue los dos casos) --%>
                <h2>No hay menú a la venta en este momento</h2>
                <p>El personal del comedor publica aquí el menú del día. Si la venta de hoy ya se cerró, no se reciben más pedidos hasta mañana.</p>
            </section>
        </c:when>
        <c:when test="${empty items}">
            <section class="tarjeta vacio">
                <h2>Se agotaron los platos de hoy</h2>
                <p>Todos los platos y productos del menú se terminaron o fueron retirados.</p>
            </section>
        </c:when>
        <c:otherwise>
            <h2 class="solo-lector">Platos y productos disponibles</h2>
            <%-- Carta del día: nombre · puntos guía · precio; metadatos; fila de acción (MASTER §7) --%>
            <ul class="carta carta--doble">
                <c:forEach var="item" items="${items}">
                    <c:set var="maximo" value="${item.stockActual < 10 ? item.stockActual : 10}"/>
                    <li class="carta__item">
                        <div class="carta__linea">
                            <h3 class="carta__nombre" id="item-${item.id}"><c:out value="${item.nombre}"/></h3>
                            <span class="carta__guia" aria-hidden="true"></span>
                            <span class="carta__precio">$<fmt:formatNumber value="${item.precio}" pattern="0.00"/></span>
                        </div>
                        <p class="carta__meta">
                            <c:out value="${item.tipo}"/>
                            <c:if test="${not empty item.descripcion}"> · <c:out value="${item.descripcion}"/></c:if>
                            · quedan ${item.stockActual} ${item.tipo == 'Producto' ? 'unidades' : 'porciones'}
                        </p>
                        <%-- Mensaje 13: agregarItem(idItem, cantidad) --%>
                        <div class="carta__acciones">
                            <form method="post" action="${pageContext.request.contextPath}/cliente/pedido">
                                <input type="hidden" name="accion" value="agregar">
                                <input type="hidden" name="idItem" value="${item.id}">
                                <div class="cantidad" data-cantidad>
                                    <button type="button" data-paso="-1" aria-label="Quitar una unidad de <c:out value='${item.nombre}'/>">
                                        <svg class="icono" aria-hidden="true"><use href="#i-menos"/></svg>
                                    </button>
                                    <input type="number" name="cantidad" value="1" min="1" max="${maximo}" step="1"
                                           inputmode="numeric" aria-label="Cantidad de <c:out value='${item.nombre}'/>">
                                    <button type="button" data-paso="1" aria-label="Agregar una unidad de <c:out value='${item.nombre}'/>">
                                        <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                                    </button>
                                </div>
                                <button type="submit" class="btn btn-secundario" aria-describedby="item-${item.id}">
                                    <svg class="icono" aria-hidden="true"><use href="#i-carrito"/></svg>
                                    Agregar al pedido
                                </button>
                            </form>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>
</main>

<%-- Resumen del pedido en armado (sin total: el total se informa en el carrito, mensajes 23-31) --%>
<c:set var="unidades" value="${0}"/>
<c:forEach var="detalle" items="${pedido.detalles}">
    <c:set var="unidades" value="${unidades + detalle.cantidad}"/>
</c:forEach>
<c:if test="${unidades > 0}">
    <div class="barra-carrito" role="region" aria-label="Su pedido">
        <div class="barra-carrito__contenido">
            <span class="barra-carrito__resumen">
                <svg class="icono" aria-hidden="true"><use href="#i-carrito"/></svg>
                ${unidades} ${unidades == 1 ? 'ítem' : 'ítems'} en su pedido
            </span>
            <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido?vista=carrito">
                Ver pedido y pagar
                <svg class="icono" aria-hidden="true"><use href="#i-flecha"/></svg>
            </a>
        </div>
    </div>
</c:if>
<script>
    // Botones − / + del selector de cantidad (mejora progresiva sobre el campo numérico)
    document.querySelectorAll('[data-cantidad]').forEach(function (grupo) {
        var campo = grupo.querySelector('input');
        var menos = grupo.querySelector('[data-paso="-1"]');
        var mas = grupo.querySelector('[data-paso="1"]');
        function actualizar() {
            var valor = parseInt(campo.value, 10) || 1;
            menos.disabled = valor <= parseInt(campo.min, 10);
            mas.disabled = valor >= parseInt(campo.max, 10);
        }
        [menos, mas].forEach(function (boton) {
            boton.addEventListener('click', function () {
                var valor = (parseInt(campo.value, 10) || 1) + parseInt(boton.dataset.paso, 10);
                campo.value = Math.min(Math.max(valor, parseInt(campo.min, 10)), parseInt(campo.max, 10));
                actualizar();
            });
        });
        campo.addEventListener('input', actualizar);
        actualizar();
    });
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
