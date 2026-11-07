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
        <p>Agregue los platos que se prepararán hoy, elija los productos del inventario que se ofrecerán y publique el menú para que los comensales lo vean.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <%-- CU05: la venta de hoy ya se cerró; crear otro menú la reabriría --%>
        <c:when test="${empty menuHoy and ventaCerrada}">
            <section class="tarjeta vacio">
                <h2>La venta de hoy ya se cerró</h2>
                <p>No se puede crear otro menú para hoy. El próximo menú se crea mañana.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/personal/reporte">
                    <svg class="icono" aria-hidden="true"><use href="#i-billetera"/></svg>
                    Ver reporte de ventas
                </a>
            </section>
        </c:when>
        <%-- Mensaje 1: el personal inicia el proceso de creación del menú --%>
        <c:when test="${empty menuHoy}">
            <section class="tarjeta vacio">
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
            <div class="rejilla-2">
                <div>
                <%-- Mensajes 8-10: formulario del plato (loop "por cada plato a preparar") --%>
                <section class="tarjeta" aria-labelledby="titulo-formulario">
                    <h2 id="titulo-formulario">Agregar plato</h2>
                    <form method="post" action="${pageContext.request.contextPath}/personal/crear-menu" novalidate>
                        <input type="hidden" name="accion" value="agregar">
                        <input type="hidden" name="tipo" value="Plato">
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
                        <div class="fila-campos">
                            <%-- Incremento 2 (Fig. 15): ItemMenu.stockActual; porciones del plato o unidades del producto --%>
                            <div class="campo">
                                <label for="stock">Porciones <span class="requerido" aria-hidden="true">*</span></label>
                                <input class="entrada" type="number" id="stock" name="stock" min="1" step="1" inputmode="numeric"
                                       value="<c:out value='${param.stock}'/>"
                                       <c:if test="${not empty errores.stock}">aria-invalid="true" aria-describedby="error-stock"</c:if>>
                                <c:if test="${not empty errores.stock}"><p class="error-campo" id="error-stock"><c:out value="${errores.stock}"/></p></c:if>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primario btn-bloque">
                            <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                            Agregar al menú
                        </button>
                    </form>
                </section>

                <%-- Corrección Paquete 2: los productos se registran en «Productos» y aquí se elige cuáles ofrecer hoy,
                     sin copiar sus existencias (solo vigentes, con stock y que el menú aún no ofrece) --%>
                <section class="tarjeta" aria-labelledby="titulo-inventario">
                    <h2 id="titulo-inventario">Agregar producto del inventario</h2>
                    <c:choose>
                        <c:when test="${empty productosSeleccionables}">
                            <p>No hay productos vigentes con unidades para agregar.
                                <a href="${pageContext.request.contextPath}/personal/productos">Registre uno en Productos</a>.</p>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${pageContext.request.contextPath}/personal/crear-menu">
                                <input type="hidden" name="accion" value="agregarProducto">
                                <div class="campo">
                                    <label for="idProducto">Producto</label>
                                    <select class="entrada" id="idProducto" name="idProducto" required>
                                        <c:forEach var="producto" items="${productosSeleccionables}">
                                            <option value="${producto.id}">#${producto.id} · <c:out value="${producto.nombre}"/><c:if test="${not empty producto.descripcion}"> (<c:out value="${producto.descripcion}"/>)</c:if> · caduca <fmt:formatDate value="${producto.fechaCaducidad}" pattern="dd/MM/yyyy"/> · ${producto.stockActual} u.</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <button type="submit" class="btn btn-secundario btn-bloque">
                                    <svg class="icono" aria-hidden="true"><use href="#i-producto"/></svg>
                                    Agregar producto al menú
                                </button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                    <p class="ayuda"><a href="${pageContext.request.contextPath}/personal/productos">Ir a Productos</a> para registrar nuevas existencias.</p>
                </section>
                </div>

                <%-- Mensaje 18: lista de ítems agregados; mensajes 19-20: solicitar publicar menú --%>
                <section class="tarjeta" aria-labelledby="titulo-items">
                    <div class="tarjeta__encabezado">
                        <h2 id="titulo-items">Menú de hoy</h2>
                        <span class="estado estado-${menuHoy.estado.toLowerCase()}"><c:out value="${menuHoy.estado}"/></span>
                    </div>
                    <div class="resumen-menu">
                        <span>Fecha: <strong><fmt:formatDate value="${menuHoy.fecha}" pattern="dd/MM/yyyy"/></strong></span>
                        <span>Ítems: <strong>${menuHoy.obtenerItems().size()}</strong></span>
                    </div>
                    <c:choose>
                        <c:when test="${empty menuHoy.obtenerItems()}">
                            <div class="vacio">
                                <p>Aún no hay ítems. Use el formulario para agregar el primer plato o producto.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <%-- Carta en construcción: el mismo componente que ve el estudiante (vista previa fiel) --%>
                            <ul class="carta">
                                <c:forEach var="oferta" items="${menuHoy.obtenerOfertas()}">
                                    <c:set var="item" value="${oferta.item}"/>
                                    <c:set var="situacion" value="${oferta.estadoEn(hoy)}"/>
                                    <li class="carta__item">
                                        <div class="carta__linea">
                                            <h3 class="carta__nombre"><c:out value="${item.nombre}"/></h3>
                                            <span class="carta__guia" aria-hidden="true"></span>
                                            <span class="carta__precio">$<fmt:formatNumber value="${item.precio}" pattern="0.00"/></span>
                                        </div>
                                        <p class="carta__meta">
                                            <c:out value="${item.tipo}"/>
                                            <c:if test="${not empty item.descripcion}"> · <c:out value="${item.descripcion}"/></c:if>
                                            · ${item.tipo == 'Producto' ? 'stock' : 'porciones'} ${item.stockActual}
                                            <c:if test="${item.tipo == 'Producto'}"> · caduca <fmt:formatDate value="${item.fechaCaducidad}" pattern="dd/MM/yyyy"/></c:if>
                                            <span class="estado ${situacion == 'Disponible' ? 'estado-disponible' : 'estado-agotado'}"><c:out value="${situacion == 'Retirado' ? 'Retirado hoy' : situacion}"/></span>
                                        </p>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                    <form method="post" action="${pageContext.request.contextPath}/personal/crear-menu" class="acciones-pie">
                        <input type="hidden" name="accion" value="publicar">
                        <button type="submit" class="btn btn-primario" ${empty menuHoy.obtenerItems() ? 'disabled' : ''}>
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
    (function () {
        var resumen = document.getElementById('resumen-errores');
        if (resumen) resumen.focus();
    })();
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
