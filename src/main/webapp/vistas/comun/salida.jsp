<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Salida" (Diagrama de robustez del incremento 2), común a los dos actores:
     - CU03: mensajes 49-51 (comprobante con el código de retiro o motivo del rechazo) → tipoSalida = "comprobante".
     - CU04: mensajes 12-15 ("Pedido autorizado para despacho" o motivo del rechazo) → tipoSalida = "entrega".
     Atributos: exitoSalida, mensajeSalida, pedido, comprobante y saldo. --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="${tipoSalida == 'entrega' ? 'Habilitar entrega' : 'Comprobante'}" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <section class="tarjeta salida" aria-labelledby="titulo-salida">
        <div class="salida__estado ${exitoSalida ? 'salida__estado--exito' : 'salida__estado--error'}">
            <svg class="icono" aria-hidden="true"><use href="${exitoSalida ? '#i-ok' : '#i-alerta'}"/></svg>
        </div>
        <h1 id="titulo-salida" tabindex="-1">
            <c:choose>
                <c:when test="${tipoSalida == 'entrega' and exitoSalida}">Pedido autorizado para despacho</c:when>
                <c:when test="${tipoSalida == 'entrega'}">Entrega no autorizada</c:when>
                <c:otherwise>Pago confirmado</c:otherwise>
            </c:choose>
        </h1>
        <p role="status"><c:out value="${mensajeSalida}"/></p>

        <c:if test="${tipoSalida == 'comprobante' and not empty comprobante}">
            <jsp:include page="/vistas/cliente/comprobante.jsp"/>
        </c:if>
        <c:if test="${not empty pedido and pedido.id > 0}">
            <jsp:include page="/vistas/comun/detallePedido.jsp"/>
        </c:if>

        <div class="acciones-pie">
            <c:choose>
                <c:when test="${tipoSalida == 'entrega'}">
                    <a class="btn btn-secundario" href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Ir al panel</a>
                    <a class="btn btn-primario" href="${pageContext.request.contextPath}/personal/entrega">
                        <svg class="icono" aria-hidden="true"><use href="#i-entrega"/></svg>
                        Validar otro código
                    </a>
                </c:when>
                <c:otherwise>
                    <button type="button" class="btn btn-secundario" onclick="window.print()">Imprimir</button>
                    <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido">Volver al menú</a>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<script>document.getElementById('titulo-salida').focus();</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
