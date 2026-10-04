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
    <div>
        <dt>Saldo restante en PoliCuenta <c:out value="${pedido.policuenta.numero}"/></dt>
        <dd>$<fmt:formatNumber value="${saldo}" pattern="0.00"/></dd>
    </div>
</dl>
