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

    <%-- pages/mis-pedidos.md: arriba la cifra de saldo actual --%>
    <dl class="cifras">
        <div class="cifra">
            <dt>Saldo actual en PoliCuenta <c:out value="${sessionScope.cliente.policuenta.numero}"/></dt>
            <dd id="saldo-policuenta">$<fmt:formatNumber value="${saldo}" pattern="0.00"/></dd>
        </div>
    </dl>

    <c:choose>
        <c:when test="${empty pedidos}">
            <section class="tarjeta vacio">
                <h2>Todavía no tiene pedidos</h2>
                <p>Cuando pague un pedido aparecerá aquí.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido">Ver menú de hoy</a>
            </section>
        </c:when>
        <c:otherwise>
            <%-- Solo separa la lista recibida por estado; no cambia qué se puede cancelar --%>
            <c:set var="hayPagados" value="${false}"/>
            <c:set var="hayHistorial" value="${false}"/>
            <c:forEach var="pedido" items="${pedidos}">
                <c:if test="${pedido.estado == 'Pagado'}"><c:set var="hayPagados" value="${true}"/></c:if>
                <c:if test="${pedido.estado != 'Pagado'}"><c:set var="hayHistorial" value="${true}"/></c:if>
            </c:forEach>

            <section aria-labelledby="titulo-pendientes" class="pendientes">
                <h2 id="titulo-pendientes">Por retirar</h2>
                <c:choose>
                    <c:when test="${hayPagados}">
                        <ul class="tickets">
                            <c:forEach var="pedido" items="${pedidos}">
                                <c:if test="${pedido.estado == 'Pagado'}">
                                    <li class="ticket ticket--compacto">
                                        <p class="ticket__etiqueta">Código de retiro</p>
                                        <p class="ticket__codigo"><c:out value="${empty pedido.comprobante.codigo ? '—' : pedido.comprobante.codigo}"/></p>
                                        <p class="ticket__nota">
                                            $<fmt:formatNumber value="${pedido.total}" pattern="0.00"/> ·
                                            <fmt:formatDate value="${pedido.fecha}" pattern="dd/MM/yyyy HH:mm"/>
                                        </p>
                                        <div class="ticket__pie">
                                            <span class="estado estado-pagado">Pagado</span>
                                            <%-- Mensajes 6-7: solo un pedido "Pagado" (no entregado) se puede cancelar --%>
                                            <form method="post" action="${pageContext.request.contextPath}/cliente/cancelar"
                                                  onsubmit="return confirm('¿Cancelar este pedido? Se devolverá el total a su PoliCuenta y el código de retiro dejará de valer.');">
                                                <input type="hidden" name="accion" value="cancelar">
                                                <input type="hidden" name="idPedido" value="${pedido.id}">
                                                <button type="submit" class="btn btn-secundario btn-borde-peligro btn-chico">
                                                    <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
                                                    Cancelar<span class="solo-lector"> pedido <c:out value="${pedido.comprobante.codigo}"/></span>
                                                </button>
                                            </form>
                                        </div>
                                    </li>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <p class="pendientes__vacio">No tiene pedidos pendientes de retirar.</p>
                    </c:otherwise>
                </c:choose>
            </section>

            <c:if test="${hayHistorial}">
                <section class="tarjeta" aria-labelledby="titulo-historial">
                    <h2 id="titulo-historial">Historial</h2>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <caption class="solo-lector">Pedidos entregados o cancelados con fecha, código, total y estado</caption>
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
                                <c:if test="${pedido.estado != 'Pagado'}">
                                    <tr>
                                        <td data-etiqueta="Fecha"><fmt:formatDate value="${pedido.fecha}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td data-etiqueta="Código" class="codigo-celda"><c:out value="${empty pedido.comprobante.codigo ? '—' : pedido.comprobante.codigo}"/></td>
                                        <td data-etiqueta="Total" class="numero">$<fmt:formatNumber value="${pedido.total}" pattern="0.00"/></td>
                                        <td data-etiqueta="Estado">
                                            <span class="estado estado-${pedido.estado.toLowerCase()}"><c:out value="${pedido.estado}"/></span>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
            </c:if>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
