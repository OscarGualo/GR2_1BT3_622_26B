<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- HU19 Consultar productos por franja de caducidad: registros habilitados con stock de la franja elegida. --%>
<c:set var="titulo" value="Caducidad de productos" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Caducidad de productos
    </nav>
    <div class="titulo-pagina">
        <h1>Caducidad de productos</h1>
        <p>Existencias habilitadas con stock según su fecha de caducidad. Un producto se vende hasta su fecha de caducidad inclusive.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta" aria-labelledby="titulo-franja">
        <h2 id="titulo-franja">Franja</h2>
        <form method="get" action="${pageContext.request.contextPath}/personal/caducidad-productos">
            <div class="campo">
                <label for="franja">Seleccione la franja</label>
                <select class="entrada" id="franja" name="franja">
                    <c:forEach var="f" items="${franjas}">
                        <option value="${f}" <c:if test="${f == franjaSeleccionada}">selected</c:if>>${f.etiqueta}</option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="btn btn-primario">Consultar</button>
        </form>
    </section>

    <c:if test="${consultado}">
        <section class="tarjeta" aria-labelledby="titulo-resultado">
            <h2 id="titulo-resultado">${franjaSeleccionada.etiqueta}</h2>
            <c:choose>
                <c:when test="${empty existencias}">
                    <div class="vacio"><p>No hay productos con stock en la franja seleccionada</p></div>
                </c:when>
                <c:otherwise>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <thead>
                            <tr>
                                <th scope="col">Referencia</th>
                                <th scope="col">Producto</th>
                                <th scope="col" class="numero">Unidades</th>
                                <th scope="col">Caduca</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="e" items="${existencias}">
                                <tr>
                                    <td>${e.referencia}</td>
                                    <th scope="row"><c:out value="${e.nombre}"/></th>
                                    <td class="numero">${e.stock}</td>
                                    <td>${e.caducidadTexto}</td>
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