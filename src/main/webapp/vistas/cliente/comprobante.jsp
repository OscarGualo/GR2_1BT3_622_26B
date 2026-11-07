<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Fragmento de la frontera "Salida" para el cliente: el Comprobante generado por
     Pedido.generarCodigoEntrega() (Diagrama de secuencia CU03 - mensajes 44-47 y 49-51). --%>
<fmt:setLocale value="en_US"/>
<div class="ticket">
    <p class="ticket__etiqueta">Código de retiro</p>
    <p class="ticket__codigo" id="codigo-retiro"><c:out value="${comprobante.codigo}"/></p>
    <p class="ticket__nota">
        Válido solo hoy, <fmt:formatDate value="${comprobante.fechaGeneracion}" pattern="dd/MM/yyyy"/>, y una sola vez.
    </p>
</div>
<dl class="resumen-pago">
    <%-- HU08 Esc. 1.2: fecha y hora del pago (el comprobante se genera al confirmar el pago) --%>
    <div>
        <dt>Fecha y hora del pago</dt>
        <dd><fmt:formatDate value="${comprobante.fechaGeneracion}" pattern="dd/MM/yyyy HH:mm"/></dd>
    </div>
    <%-- Examen 1B HU11 Esc. 1.1: un pedido programado muestra su franja de retiro --%>
    <c:if test="${not empty pedido.franja}">
        <div>
            <dt>Franja de retiro</dt>
            <dd id="franja-retiro"><c:out value="${pedido.franja.horaInicio}"/>–<c:out value="${pedido.franja.horaInicio.plusMinutes(15)}"/></dd>
        </div>
    </c:if>
    <div>
        <dt>Saldo restante en PoliCuenta <c:out value="${pedido.policuenta.numero}"/></dt>
        <dd>$<fmt:formatNumber value="${saldo}" pattern="0.00"/></dd>
    </div>
</dl>
<%-- CU 06: desde el comprobante el cliente puede ir a "Mis pedidos" para cancelar --%>
<p>
    <c:choose>
        <c:when test="${not empty pedido.franja}">
            <a href="${pageContext.request.contextPath}/cliente/programados">Ver mis pedidos programados, cambiar la franja o cancelar</a>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/cliente/cancelar">Ver mis pedidos o cancelar este pedido</a>
        </c:otherwise>
    </c:choose>
</p>