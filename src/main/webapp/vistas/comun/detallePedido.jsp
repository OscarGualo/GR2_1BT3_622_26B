<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Fragmento de la frontera "Salida": detalle del pedido (cliente, ítems y total) que ven ambos actores --%>
<fmt:setLocale value="en_US"/>
<h2>Pedido N.º <fmt:formatNumber value="${pedido.id}" pattern="0000"/></h2>
<dl class="resumen-pago">
    <div>
        <dt>Cliente</dt>
        <dd><c:out value="${pedido.cliente.nombres} ${pedido.cliente.apellidos}"/></dd>
    </div>
    <div>
        <dt>Fecha</dt>
        <dd><fmt:formatDate value="${pedido.fecha}" pattern="dd/MM/yyyy HH:mm"/></dd>
    </div>
    <div>
        <dt>Estado</dt>
        <dd><span class="estado estado-${pedido.estado.toLowerCase()}"><c:out value="${pedido.estado}"/></span></dd>
    </div>
    <c:forEach var="detalle" items="${pedido.detalles}">
        <div>
            <dt>${detalle.cantidad} × <c:out value="${detalle.item.nombre}"/></dt>
            <dd>$<fmt:formatNumber value="${detalle.cantidad * detalle.precioUnitario}" pattern="0.00"/></dd>
        </div>
    </c:forEach>
    <div class="resumen-pago__total">
        <dt>Total</dt>
        <dd>$<fmt:formatNumber value="${pedido.total}" pattern="0.00"/></dd>
    </div>
</dl>
