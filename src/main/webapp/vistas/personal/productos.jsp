<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Corrección de productos persistentes (Paquete 2): pantalla «Productos» del personal.
     Registra existencias de productos independientes del menú del día (no crea ninguna oferta) y las lista.
     Las existencias se conservan entre jornadas; se agregan al menú desde «Crear menú». --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Productos" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Productos
    </nav>
    <div class="titulo-pagina">
        <h1>Productos</h1>
        <p>Registre las existencias de productos con su fecha de caducidad. Se conservan entre jornadas: las unidades que no se vendan hoy se pueden ofrecer en el menú de otro día.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <div class="rejilla-2">
        <section class="tarjeta" aria-labelledby="titulo-registro">
            <h2 id="titulo-registro">Registrar existencia</h2>
            <form method="post" action="${pageContext.request.contextPath}/personal/productos" novalidate>
                <input type="hidden" name="accion" value="registrar">
                <div class="campo">
                    <label for="nombre">Nombre <span class="requerido" aria-hidden="true">*</span></label>
                    <input class="entrada" type="text" id="nombre" name="nombre" required maxlength="100"
                           value="<c:out value='${param.nombre}'/>"
                           <c:if test="${not empty errores.nombre}">aria-invalid="true" aria-describedby="error-nombre"</c:if>>
                    <c:if test="${not empty errores.nombre}"><p class="error-campo" id="error-nombre"><c:out value="${errores.nombre}"/></p></c:if>
                </div>
                <div class="campo">
                    <label for="descripcion">Presentación o descripción</label>
                    <textarea class="entrada" id="descripcion" name="descripcion" maxlength="255"
                              aria-describedby="ayuda-descripcion"><c:out value="${param.descripcion}"/></textarea>
                    <p class="ayuda" id="ayuda-descripcion">Por ejemplo: botella de 500 ml, porción, unidad.</p>
                </div>
                <div class="campo">
                    <label for="precio">Precio (USD) <span class="requerido" aria-hidden="true">*</span></label>
                    <input class="entrada" type="text" id="precio" name="precio" required inputmode="decimal"
                           placeholder="0.75" value="<c:out value='${param.precio}'/>"
                           <c:if test="${not empty errores.precio}">aria-invalid="true" aria-describedby="error-precio"</c:if>>
                    <c:if test="${not empty errores.precio}"><p class="error-campo" id="error-precio"><c:out value="${errores.precio}"/></p></c:if>
                </div>
                <div class="fila-campos">
                    <div class="campo">
                        <label for="stock">Cantidad inicial <span class="requerido" aria-hidden="true">*</span></label>
                        <input class="entrada" type="number" id="stock" name="stock" min="1" max="10000" step="1" inputmode="numeric"
                               value="<c:out value='${param.stock}'/>"
                               <c:if test="${not empty errores.stock}">aria-invalid="true" aria-describedby="error-stock"</c:if>>
                        <c:if test="${not empty errores.stock}"><p class="error-campo" id="error-stock"><c:out value="${errores.stock}"/></p></c:if>
                    </div>
                    <div class="campo">
                        <label for="fechaCaducidad">Fecha de caducidad <span class="requerido" aria-hidden="true">*</span></label>
                        <input class="entrada" type="date" id="fechaCaducidad" name="fechaCaducidad"
                               value="<c:out value='${param.fechaCaducidad}'/>"
                               <c:if test="${not empty errores.fechaCaducidad}">aria-invalid="true" aria-describedby="error-fechaCaducidad"</c:if>>
                        <c:if test="${not empty errores.fechaCaducidad}"><p class="error-campo" id="error-fechaCaducidad"><c:out value="${errores.fechaCaducidad}"/></p></c:if>
                    </div>
                </div>
                <p class="ayuda">Se vende hasta el día de su caducidad inclusive. Registrar no lo agrega al menú: hágalo desde «Crear menú».</p>
                <button type="submit" class="btn btn-primario btn-bloque">
                    <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                    Registrar producto
                </button>
            </form>
        </section>

        <section class="tarjeta" aria-labelledby="titulo-lista">
            <div class="tarjeta__encabezado">
                <h2 id="titulo-lista">Existencias</h2>
            </div>
            <%-- Filtros de consulta: ninguno habilita una venta --%>
            <nav class="resumen-menu" aria-label="Filtrar existencias">
                <c:forEach var="opcion" items="VIGENTES,AGOTADOS,CADUCADOS,HISTORICOS">
                    <c:set var="etiqueta" value="${opcion == 'VIGENTES' ? 'Vigentes con stock' : (opcion == 'AGOTADOS' ? 'Sin stock' : (opcion == 'CADUCADOS' ? 'Caducados' : 'Históricos'))}"/>
                    <a class="btn btn-secundario btn-chico" href="${pageContext.request.contextPath}/personal/productos?filtro=${opcion}"
                       <c:if test="${filtro == opcion}">aria-current="page"</c:if>>${etiqueta}</a>
                </c:forEach>
            </nav>
            <c:choose>
                <c:when test="${empty productos}">
                    <div class="vacio">
                        <p>No hay productos en esta lista.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <thead>
                                <tr>
                                    <th scope="col">Id</th>
                                    <th scope="col">Producto</th>
                                    <th scope="col" class="numero">Precio</th>
                                    <th scope="col" class="numero">Unidades</th>
                                    <th scope="col">Caduca</th>
                                    <th scope="col">Situación</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="producto" items="${productos}">
                                    <tr>
                                        <td>${producto.id}</td>
                                        <th scope="row">
                                            <c:out value="${producto.nombre}"/>
                                            <c:if test="${not empty producto.descripcion}"><br><small><c:out value="${producto.descripcion}"/></small></c:if>
                                        </th>
                                        <td class="numero">$<fmt:formatNumber value="${producto.precio}" pattern="0.00"/></td>
                                        <td class="numero">${producto.stockActual}</td>
                                        <td><fmt:formatDate value="${producto.fechaCaducidad}" pattern="dd/MM/yyyy"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not producto.habilitado}"><span class="estado estado-cerrado">Histórico</span></c:when>
                                                <c:when test="${not producto.estaVigente(hoy)}"><span class="estado estado-cancelado">Caducado</span></c:when>
                                                <c:when test="${producto.stockActual == 0}"><span class="estado estado-agotado">Sin stock</span></c:when>
                                                <c:otherwise><span class="estado estado-disponible">Vigente</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
