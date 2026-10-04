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
        <p>
            <c:if test="${not empty menuHoy}"><fmt:formatDate value="${menuHoy.fecha}" pattern="dd/MM/yyyy"/> · </c:if>
            Elija sus platos o productos, revise el total y pague con su PoliCuenta.
        </p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:if test="${not empty sessionScope.ultimoCodigo}">
        <div class="alerta alerta-info" role="note">
            <svg class="icono" aria-hidden="true"><use href="#i-ticket"/></svg>
            <span>Su último código de retiro es <strong><c:out value="${sessionScope.ultimoCodigo}"/></strong>.
                <a href="${pageContext.request.contextPath}/cliente/pedido?vista=comprobante">Ver comprobante</a></span>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty menuHoy or menuHoy.estado != 'Publicado'}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-menu"/></svg>
                <h2>El menú de hoy todavía no está publicado</h2>
                <p>Vuelva en unos minutos; el personal del comedor está preparando la cartelera.</p>
            </section>
        </c:when>
        <c:when test="${empty items}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-plato"/></svg>
                <h2>Se agotaron los platos de hoy</h2>
                <p>Todos los platos y productos del menú se terminaron o fueron retirados.</p>
            </section>
        </c:when>
        <c:otherwise>
            <h2 class="solo-lector">Platos y productos disponibles</h2>
            <div class="catalogo">
                <c:forEach var="item" items="${items}">
                    <c:set var="maximo" value="${item.stockActual < 10 ? item.stockActual : 10}"/>
                    <article class="producto-tarjeta" aria-labelledby="item-${item.id}">
                        <span class="chip-tipo">
                            <svg class="icono" aria-hidden="true"><use href="${item.tipo == 'Producto' ? '#i-producto' : '#i-plato'}"/></svg>
                            <c:out value="${item.tipo}"/>
                        </span>
                        <h2 id="item-${item.id}"><c:out value="${item.nombre}"/></h2>
                        <c:if test="${not empty item.descripcion}">
                            <p class="producto-tarjeta__descripcion"><c:out value="${item.descripcion}"/></p>
                        </c:if>
                        <div class="producto-tarjeta__fila">
                            <span class="precio">$<fmt:formatNumber value="${item.precio}" pattern="0.00"/></span>
                            <span class="existencias">
                                Quedan ${item.stockActual} ${item.tipo == 'Producto' ? 'unidades' : 'porciones'}
                            </span>
                        </div>
                        <%-- Mensaje 13: agregarItem(idItem, cantidad) --%>
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
                            <button type="submit" class="btn btn-primario">
                                <svg class="icono" aria-hidden="true"><use href="#i-carrito"/></svg>
                                Agregar
                            </button>
                        </form>
                    </article>
                </c:forEach>
            </div>
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
