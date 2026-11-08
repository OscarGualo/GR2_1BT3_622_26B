<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- HU18 Consultar ventas de productos: unidades vendidas por registro de producto en un periodo.
     Cuentan los pedidos Pagados o Entregados según la fecha de pago (comprobante). --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Ventas de productos" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Ventas de productos
    </nav>
    <div class="titulo-pagina">
        <h1>Ventas de productos</h1>
        <p>Unidades vendidas de cada registro de producto. Cuentan los pedidos pagados o entregados, según la fecha de pago.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta" aria-labelledby="titulo-filtro">
        <h2 id="titulo-filtro">Periodo</h2>
        <form method="get" action="${pageContext.request.contextPath}/personal/ventas-productos">
            <fieldset class="campo">
                <legend>Seleccione el periodo</legend>
                <label><input type="radio" name="periodo" value="hoy" <c:if test="${opcion == 'hoy'}">checked</c:if>> Hoy</label>
                <label><input type="radio" name="periodo" value="7dias" <c:if test="${opcion == '7dias'}">checked</c:if>> Últimos 7 días</label>
                <label><input type="radio" name="periodo" value="rango" <c:if test="${opcion == 'rango'}">checked</c:if>> Rango de fechas</label>
            </fieldset>
            <div class="fila-campos">
                <div class="campo">
                    <label for="inicio">Desde</label>
                    <input class="entrada" type="date" id="inicio" name="inicio" value="<c:out value='${param.inicio}'/>">
                </div>
                <div class="campo">
                    <label for="fin">Hasta</label>
                    <input class="entrada" type="date" id="fin" name="fin" value="<c:out value='${param.fin}'/>">
                </div>
            </div>
            <button type="submit" class="btn btn-primario">Consultar</button>
        </form>
    </section>

    <c:if test="${consultado}">
        <section class="tarjeta" aria-labelledby="titulo-resultado">
            <h2 id="titulo-resultado">Del ${periodo.inicio} al ${periodo.fin}</h2>
            <c:choose>
                <c:when test="${empty filas}">
                    <div class="vacio"><p>No hay ventas de productos en el periodo seleccionado</p></div>
                </c:when>
                <c:otherwise>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <thead>
                                <tr>
                                    <th scope="col">Referencia</th>
                                    <th scope="col">Producto</th>
                                    <th scope="col">Caduca</th>
                                    <th scope="col" class="numero">Unidades vendidas</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="fila" items="${filas}">
                                    <tr>
                                        <td>${fila.referencia}</td>
                                        <th scope="row"><c:out value="${fila.nombre}"/></th>
                                        <td><fmt:formatDate value="${fila.fechaCaducidad}" pattern="dd/MM/yyyy"/></td>
                                        <td class="numero">${fila.unidades}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>