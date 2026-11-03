<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Examen 1B, HU13 «Ver pedidos programados por franja» (T13.3).
     Atributos: franjas (List<ResumenFranja>: franja, pedidos Pagados y total de cada producto) y fechaHoy.
     Solo lectura. La comanda de cada franja empieza por lo que hay que preparar (totales por producto) y
     sigue con los códigos de retiro, que es lo que el despacho necesita al entregar. --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Pedidos programados" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Pedidos programados
    </nav>
    <div class="titulo-pagina">
        <h1>Pedidos programados</h1>
        <p>Hoy, <fmt:parseDate value="${fechaHoy}" pattern="yyyy-MM-dd" var="hoyFecha"/><fmt:formatDate value="${hoyFecha}" pattern="dd/MM/yyyy"/>. Pedidos pagados por franja de retiro, con lo que hay que preparar en cada una.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty franjas}">
            <section class="vacio" aria-labelledby="titulo-vacio">
                <h2 id="titulo-vacio">No hay pedidos programados para hoy</h2>
                <p>Cuando un estudiante programe un pedido para llevar, aparecerá en su franja.</p>
            </section>
        </c:when>
        <c:otherwise>
            <c:forEach var="franja" items="${franjas}">
                <section class="comanda" aria-labelledby="franja-${franja.horaInicio}">
                    <header class="comanda__cabecera">
                        <h2 id="franja-${franja.horaInicio}"><c:out value="${franja.etiqueta}"/></h2>
                        <p class="comanda__conteo">${franja.cantidadPedidos} ${franja.cantidadPedidos == 1 ? 'pedido' : 'pedidos'}</p>
                    </header>
                    <div class="comanda__cuerpo">
                        <div class="comanda__preparar">
                            <h3>Para preparar</h3>
                            <ul class="comanda__totales">
                                <c:forEach var="total" items="${franja.totales}">
                                    <li><span><c:out value="${total.key}"/></span> <strong>× ${total.value}</strong></li>
                                </c:forEach>
                            </ul>
                        </div>
                        <div class="comanda__pedidos tabla-contenedor">
                            <table class="tabla">
                                <caption class="solo-lector">Pedidos de la franja <c:out value="${franja.etiqueta}"/></caption>
                                <thead>
                                <tr>
                                    <th scope="col">Código</th>
                                    <th scope="col">Estudiante</th>
                                    <th scope="col">Productos</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="pedido" items="${franja.pedidos}">
                                    <tr>
                                        <td data-etiqueta="Código" class="codigo-celda"><c:out value="${pedido.comprobante.codigo}"/></td>
                                        <td data-etiqueta="Estudiante"><c:out value="${pedido.cliente.nombres}"/> <c:out value="${pedido.cliente.apellidos}"/></td>
                                        <td data-etiqueta="Productos">
                                            <c:forEach var="detalle" items="${pedido.detalles}" varStatus="vuelta"><c:out value="${detalle.item.nombre}"/> × ${detalle.cantidad}<c:if test="${not vuelta.last}">, </c:if></c:forEach>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
