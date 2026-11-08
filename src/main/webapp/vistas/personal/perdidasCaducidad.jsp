<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- HU20 Registrar pérdidas por caducidad: descarte de unidades de existencias caducadas y su historial. --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Pérdidas por caducidad" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Pérdidas por caducidad
    </nav>
    <div class="titulo-pagina">
        <h1>Pérdidas por caducidad</h1>
        <p>Registre las unidades que retiró físicamente de una existencia caducada. Se descuentan de su stock.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta" aria-labelledby="titulo-registro">
        <h2 id="titulo-registro">Registrar pérdida</h2>
        <c:choose>
            <c:when test="${empty descartables}">
                <div class="vacio"><p>No hay existencias caducadas con stock para descartar.</p></div>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/personal/perdidas"
                      onsubmit="this.querySelector('button[type=submit]').disabled = true;">
                    <input type="hidden" name="token" value="${token}">
                    <div class="campo">
                        <label for="referencia">Existencia caducada <span class="requerido" aria-hidden="true">*</span></label>
                        <select class="entrada" id="referencia" name="referencia" required>
                            <c:forEach var="producto" items="${descartables}">
                                <option value="${producto.id}">
                                        ${producto.id} - <c:out value="${producto.nombre}"/> - ${producto.stockActual} unidades -
                                    caducó el <fmt:formatDate value="${producto.fechaCaducidad}" pattern="dd/MM/yyyy"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="campo">
                        <label for="unidades">Unidades descartadas <span class="requerido" aria-hidden="true">*</span></label>
                        <input class="entrada" type="number" id="unidades" name="unidades" min="1" step="1" required>
                    </div>
                    <button type="submit" class="btn btn-primario">Registrar pérdida</button>
                </form>
            </c:otherwise>
        </c:choose>
    </section>

    <%@ include file="historialPerdidas.jspf" %>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>