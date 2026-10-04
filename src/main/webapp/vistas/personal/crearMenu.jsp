<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez) para el CU 01 Crear Menú.
     Diagrama de secuencia CU01: mensajes 1 (inicia proceso), 8 (formulario de ítems), 9 (ingresa datos del ítem),
     18 (lista de ítems agregados), 19 (solicita publicar menú) y 25 (muestra mensaje). --%>
<%-- Precios con punto decimal (USD), independientemente del idioma del navegador --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Crear menú" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Crear menú
    </nav>
    <div class="titulo-pagina">
        <h1>Crear menú del día</h1>
        <p>Agregue los platos y productos que se ofrecerán hoy y publique el menú para que los comensales lo vean.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <%-- Mensaje 1: el personal inicia el proceso de creación del menú --%>
        <c:when test="${empty menuHoy}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-menu"/></svg>
                <h2>Todavía no ha iniciado el menú de hoy</h2>
                <p>Antes de empezar, revise los ingredientes y productos disponibles en cocina.</p>
                <form method="post" action="${pageContext.request.contextPath}/personal/crear-menu">
                    <input type="hidden" name="accion" value="iniciar">
                    <button type="submit" class="btn btn-primario">
                        <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                        Iniciar creación del menú
                    </button>
                </form>
            </section>
        </c:when>
        <c:otherwise>
            <c:set var="esProducto" value="${param.tipo == 'Producto'}"/>
            <div class="rejilla-2">
                <%-- Mensajes 8-10: formulario del ítem (loop "por cada plato o producto a ofrecer") --%>
                <section class="tarjeta" aria-labelledby="titulo-formulario">
                    <h2 id="titulo-formulario">Agregar ítem</h2>
                    <form method="post" action="${pageContext.request.contextPath}/personal/crear-menu" novalidate>
                        <input type="hidden" name="accion" value="agregar">
                        <fieldset class="selector-tipo">
                            <legend>Tipo de ítem</legend>
                            <div class="selector-tipo__opciones">
                                <input type="radio" id="tipo-plato" name="tipo" value="Plato" ${esProducto ? '' : 'checked'}>
                                <label for="tipo-plato">
                                    <svg class="icono" aria-hidden="true"><use href="#i-plato"/></svg> Plato
                                </label>
                                <input type="radio" id="tipo-producto" name="tipo" value="Producto" ${esProducto ? 'checked' : ''}>
                                <label for="tipo-producto">
                                    <svg class="icono" aria-hidden="true"><use href="#i-producto"/></svg> Producto
                                </label>
                            </div>
                        </fieldset>

                        <div class="campo">
                            <label for="nombre">Nombre <span class="requerido" aria-hidden="true">*</span></label>
                            <input class="entrada" type="text" id="nombre" name="nombre" required maxlength="100"
                                   value="<c:out value='${param.nombre}'/>"
                                   <c:if test="${not empty errores.nombre}">aria-invalid="true" aria-describedby="error-nombre"</c:if>>
                            <c:if test="${not empty errores.nombre}"><p class="error-campo" id="error-nombre"><c:out value="${errores.nombre}"/></p></c:if>
                        </div>
                        <div class="campo">
                            <label for="descripcion">Descripción</label>
                            <textarea class="entrada" id="descripcion" name="descripcion" maxlength="255"
                                      aria-describedby="ayuda-descripcion"><c:out value="${param.descripcion}"/></textarea>
                            <p class="ayuda" id="ayuda-descripcion">Ingredientes o detalle que verá el comensal.</p>
                        </div>
                        <div class="campo">
                            <label for="precio">Precio (USD) <span class="requerido" aria-hidden="true">*</span></label>
                            <input class="entrada" type="text" id="precio" name="precio" required inputmode="decimal"
                                   placeholder="2.50" value="<c:out value='${param.precio}'/>"
                                   <c:if test="${not empty errores.precio}">aria-invalid="true" aria-describedby="error-precio"</c:if>>
                            <c:if test="${not empty errores.precio}"><p class="error-campo" id="error-precio"><c:out value="${errores.precio}"/></p></c:if>
                        </div>
                        <div class="fila-campos solo-producto" id="campos-producto" ${esProducto ? '' : 'hidden'}>
                            <div class="campo">
                                <label for="stock">Stock <span class="requerido" aria-hidden="true">*</span></label>
                                <input class="entrada" type="number" id="stock" name="stock" min="1" step="1" inputmode="numeric"
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
                        <button type="submit" class="btn btn-primario btn-bloque">
                            <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                            Agregar al menú
                        </button>
                    </form>
                </section>

                <%-- Mensaje 18: lista de ítems agregados; mensajes 19-20: solicitar publicar menú --%>
                <section class="tarjeta" aria-labelledby="titulo-items">
                    <div class="tarjeta__encabezado">
                        <h2 id="titulo-items">Menú de hoy</h2>
                        <span class="estado estado-${menuHoy.estado.toLowerCase()}"><c:out value="${menuHoy.estado}"/></span>
                    </div>
                    <div class="resumen-menu">
                        <span>Fecha: <strong><fmt:formatDate value="${menuHoy.fecha}" pattern="dd/MM/yyyy"/></strong></span>
                        <span>Ítems: <strong>${menuHoy.obtenerItem().size()}</strong></span>
                    </div>
                    <c:choose>
                        <c:when test="${empty menuHoy.obtenerItem()}">
                            <div class="vacio">
                                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-plato"/></svg>
                                <p>Aún no hay ítems. Use el formulario para agregar el primer plato o producto.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="tabla-contenedor">
                                <table class="tabla">
                                    <caption class="solo-lector">Ítems agregados al menú de hoy</caption>
                                    <thead>
                                    <tr>
                                        <th scope="col">Nombre</th>
                                        <th scope="col">Tipo</th>
                                        <th scope="col" class="numero">Precio</th>
                                        <th scope="col">Estado</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="item" items="${menuHoy.obtenerItem()}">
                                        <tr>
                                            <td data-etiqueta="Nombre">
                                                <strong><c:out value="${item.nombre}"/></strong>
                                                <c:if test="${not empty item.descripcion}"><div class="descripcion"><c:out value="${item.descripcion}"/></div></c:if>
                                            </td>
                                            <td data-etiqueta="Tipo" class="tipo">
                                                <c:out value="${item.tipo}"/>
                                                <c:if test="${item.tipo == 'Producto'}"> · stock ${item.stock}</c:if>
                                            </td>
                                            <td data-etiqueta="Precio" class="numero">$<fmt:formatNumber value="${item.precio}" pattern="0.00"/></td>
                                            <td data-etiqueta="Estado"><span class="estado estado-${item.estado.toLowerCase()}"><c:out value="${item.estado}"/></span></td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <form method="post" action="${pageContext.request.contextPath}/personal/crear-menu" class="acciones-pie">
                        <input type="hidden" name="accion" value="publicar">
                        <button type="submit" class="btn btn-primario" ${empty menuHoy.obtenerItem() ? 'disabled' : ''}>
                            <svg class="icono" aria-hidden="true"><use href="#i-publicar"/></svg>
                            ${menuHoy.estado == 'Publicado' ? 'Volver a publicar' : 'Publicar menú'}
                        </button>
                    </form>
                </section>
            </div>
        </c:otherwise>
    </c:choose>
</main>
<script>
    // Muestra stock y fecha de caducidad solo cuando el tipo es Producto (alt [tipo = Producto] de la secuencia)
    (function () {
        var campos = document.getElementById('campos-producto');
        if (!campos) return;
        document.querySelectorAll('input[name="tipo"]').forEach(function (radio) {
            radio.addEventListener('change', function () {
                campos.hidden = document.getElementById('tipo-producto').checked === false;
            });
        });
        var resumen = document.getElementById('resumen-errores');
        if (resumen) resumen.focus();
    })();
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
