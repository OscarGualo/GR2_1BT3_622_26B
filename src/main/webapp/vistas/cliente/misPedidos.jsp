<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Mis pedidos" del CU 06 Cancelar pedido con reembolso a PoliCuenta.
     Diagrama de secuencia CU06: mensajes 1 (abre "Mis pedidos") y 5 (lista fecha, código, total y estado).
     Atributos: pedidos (List<Pedido>) y saldo (saldo actual de la PoliCuenta). --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Mis pedidos" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/cliente/pedido">Menú de hoy</a> / Mis pedidos
    </nav>
    <div class="titulo-pagina">
        <h1>Mis pedidos</h1>
        <p>Puede cancelar un pedido pagado que aún no retiró; el total vuelve a su PoliCuenta.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <dl class="resumen-pago">
        <div>
            <dt>Saldo actual en PoliCuenta <c:out value="${sessionScope.cliente.policuenta.numero}"/></dt>
            <dd id="saldo-policuenta">$<fmt:formatNumber value="${saldo}" pattern="0.00"/></dd>
        </div>
    </dl>

    <c:choose>
        <c:when test="${empty pedidos}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-ticket"/></svg>
                <h2>Todavía no tiene pedidos</h2>
                <p>Cuando pague un pedido aparecerá aquí.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido">Ver menú de hoy</a>
            </section>
        </c:when>
        <c:otherwise>
            <section class="tarjeta" aria-labelledby="titulo-pedidos">
                <h2 id="titulo-pedidos">Pedidos</h2>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <caption class="solo-lector">Sus pedidos con fecha, código, total y estado</caption>
                        <thead>
                        <tr>
                            <th scope="col">Fecha</th>
                            <th scope="col">Código</th>
                            <th scope="col" class="numero">Total</th>
                            <th scope="col">Estado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="pedido" items="${pedidos}">
                            <tr>
                                <td data-etiqueta="Fecha"><fmt:formatDate value="${pedido.fecha}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td data-etiqueta="Código">
                                    <strong><c:out value="${empty pedido.comprobante.codigo ? '—' : pedido.comprobante.codigo}"/></strong>
                                </td>
                                <td data-etiqueta="Total" class="numero">$<fmt:formatNumber value="${pedido.total}" pattern="0.00"/></td>
                                <td data-etiqueta="Estado">
                                    <span class="estado estado-${pedido.estado.toLowerCase()}"><c:out value="${pedido.estado}"/></span>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
