<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Reporte de ventas" (Diagrama de robustez CU05) del CU 05 Cerrar menú y reporte de ventas.
     Diagrama de secuencia CU05: mensaje 17 (tras el cierre se muestra el reporte del día).
     Atributos: fecha (yyyy-MM-dd), esHoy, esFutura, reporte (ReporteVentas), total (calcularTotalVendido()),
     pendientes (pedidos pagados sin entregar) y ventaCerrada. --%>
<%-- Precios con punto decimal (USD), independientemente del idioma del navegador --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Reporte de ventas" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Reporte de ventas
    </nav>
    <%-- pages/reporte-ventas.md: filtro de fecha compacto en la línea del título --%>
    <div class="titulo-con-filtro">
        <div class="titulo-pagina">
            <h1>Reporte de ventas</h1>
            <p>Total vendido del día (pedidos pagados y entregados) y el detalle de todos los pedidos.</p>
        </div>
        <form method="get" action="${pageContext.request.contextPath}/personal/reporte" class="filtro-fecha"
              aria-label="Elegir el día del reporte">
            <div class="campo">
                <label for="fecha">Día</label>
                <input class="entrada" type="date" id="fecha" name="fecha" value="<c:out value='${fecha}'/>">
            </div>
            <div class="filtro-fecha__acciones">
                <button type="submit" class="btn btn-secundario">Ver reporte</button>
                <c:if test="${not esHoy}">
                    <a class="btn btn-secundario" href="${pageContext.request.contextPath}/personal/reporte">Hoy</a>
                </c:if>
            </div>
        </form>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:if test="${not empty reporte}">
        <section class="tarjeta" aria-labelledby="titulo-resumen">
            <div class="tarjeta__encabezado">
                <h2 id="titulo-resumen">Resumen del <fmt:formatDate value="${reporte.fecha}" pattern="dd/MM/yyyy"/></h2>
                <c:choose>
                    <c:when test="${ventaCerrada}"><span class="estado estado-cerrado">Venta cerrada</span></c:when>
                    <c:when test="${esHoy}"><span class="estado estado-publicado">Venta abierta</span></c:when>
                </c:choose>
            </div>
            <%-- Fila de cifras: todas vienen del servlet (calcularTotalVendido(), pedidos y pendientes) --%>
            <dl class="cifras cifras--reporte">
                <div class="cifra cifra--principal">
                    <dt>${esHoy and not ventaCerrada ? 'Total vendido hasta ahora' : 'Total vendido'}</dt>
                    <dd id="total-vendido">$<fmt:formatNumber value="${total}" pattern="0.00"/></dd>
                </div>
                <div class="cifra">
                    <dt>Pedidos registrados</dt>
                    <dd>${reporte.pedidos.size()}</dd>
                </div>
                <div class="cifra">
                    <dt>Pendientes de entrega</dt>
                    <dd>${pendientes}</dd>
                </div>
            </dl>
            <%-- Un único aviso con las dos frases (venta abierta / pendientes) --%>
            <c:if test="${(esHoy and not ventaCerrada) or pendientes > 0}">
                <div class="alerta alerta-info alerta--compacta" role="status">
                    <svg class="icono" aria-hidden="true"><use href="#i-alerta"/></svg>
                    <span>
                        <c:if test="${esHoy and not ventaCerrada}">La venta de hoy sigue abierta: el total puede cambiar. Ciérrela desde "Cerrar venta del día".</c:if>
                        <c:if test="${pendientes > 0}">Hay ${pendientes} ${pendientes == 1 ? 'pedido pagado' : 'pedidos pagados'} sin retirar; se pueden seguir entregando con su código aunque la venta esté cerrada.</c:if>
                    </span>
                </div>
            </c:if>
        </section>

        <c:choose>
            <c:when test="${empty reporte.pedidos}">
                <section class="tarjeta vacio">
                    <h2>${esFutura ? 'Ese día todavía no llega' : 'No hubo ventas este día'}</h2>
                    <p>${esFutura ? 'Elija hoy o un día anterior.' : 'No se registró ningún pedido en la fecha elegida.'}</p>
                </section>
            </c:when>
            <c:otherwise>
                <section class="tarjeta" aria-labelledby="titulo-pedidos">
                    <h2 id="titulo-pedidos">Pedidos del día</h2>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <caption class="solo-lector">Pedidos del día con código, hora, cliente, total y estado. Los cancelados no suman al total.</caption>
                            <thead>
                            <tr>
                                <th scope="col">Código</th>
                                <th scope="col">Hora</th>
                                <th scope="col">Cliente</th>
                                <th scope="col" class="numero">Total</th>
                                <th scope="col">Estado</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="pedido" items="${reporte.pedidos}">
                                <tr>
                                    <td data-etiqueta="Código" class="codigo-celda"><c:out value="${empty pedido.comprobante.codigo ? '—' : pedido.comprobante.codigo}"/></td>
                                    <td data-etiqueta="Hora"><fmt:formatDate value="${pedido.fecha}" pattern="HH:mm"/></td>
                                    <td data-etiqueta="Cliente">
                                        <c:out value="${pedido.cliente.nombres} ${pedido.cliente.apellidos}"/>
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
    </c:if>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
