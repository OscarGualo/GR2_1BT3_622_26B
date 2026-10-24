<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez CU05) para el CU 05 Cerrar menú.
     Diagrama de secuencia CU05: mensajes 1 (abre "Cerrar venta del día"), 5 (muestra el menú y su estado),
     6 (confirma el cierre) y 10/13 (muestra el motivo si no se pudo cerrar). --%>
<%-- Precios con punto decimal (USD), independientemente del idioma del navegador --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Cerrar venta del día" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Cerrar venta del día
    </nav>
    <div class="titulo-pagina">
        <h1>Cerrar venta del día</h1>
        <p>Al cerrar la venta, los estudiantes ya no podrán pagar pedidos del menú de hoy y se mostrará el reporte de ventas.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty menuHoy and not empty menuCerrado}">
            <%-- La venta de hoy ya se cerró: no se ofrece crear otro menú (reabriría la venta) --%>
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-ok"/></svg>
                <h2>La venta de hoy ya se cerró</h2>
                <p>Los estudiantes ya no pueden pagar pedidos. Los pedidos pagados se siguen entregando con su código.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/personal/reporte">
                    <svg class="icono" aria-hidden="true"><use href="#i-billetera"/></svg>
                    Ver reporte de ventas
                </a>
            </section>
        </c:when>
        <c:when test="${empty menuHoy}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-menu"/></svg>
                <h2>No hay un menú abierto hoy</h2>
                <p>Todavía no se ha creado el menú del día.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/personal/crear-menu">
                    <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                    Ir a crear menú
                </a>
            </section>
        </c:when>
        <c:when test="${menuHoy.estado == 'Borrador'}">
            <%-- Regla de Menu.cerrarParaVenta(): un menú en Borrador nunca estuvo a la venta y no se cierra --%>
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-alerta"/></svg>
                <h2>El menú de hoy todavía no se publicó</h2>
                <p>Solo se puede cerrar la venta de un menú publicado. Publíquelo primero desde "Crear menú".</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/personal/crear-menu">
                    <svg class="icono" aria-hidden="true"><use href="#i-publicar"/></svg>
                    Ir a crear menú
                </a>
            </section>
        </c:when>
        <c:otherwise>
            <%-- Mensaje 5: menú del día publicado y sus ítems --%>
            <section class="tarjeta" aria-labelledby="titulo-menu">
                <div class="tarjeta__encabezado">
                    <h2 id="titulo-menu">Menú de hoy</h2>
                    <span class="estado estado-${menuHoy.estado.toLowerCase()}">Menú <c:out value="${menuHoy.estado}"/></span>
                </div>
                <div class="resumen-menu">
                    <span>Fecha: <strong><fmt:formatDate value="${menuHoy.fecha}" pattern="dd/MM/yyyy"/></strong></span>
                    <span>Ítems: <strong>${menuHoy.obtenerItems().size()}</strong></span>
                </div>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <caption class="solo-lector">Platos y productos del menú de hoy</caption>
                        <thead>
                        <tr>
                            <th scope="col">Nombre</th>
                            <th scope="col">Tipo</th>
                            <th scope="col" class="numero">Precio</th>
                            <th scope="col">Estado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${menuHoy.obtenerItems()}">
                            <tr>
                                <td data-etiqueta="Nombre"><strong><c:out value="${item.nombre}"/></strong></td>
                                <td data-etiqueta="Tipo" class="tipo">
                                    <span class="tipo__etiqueta">
                                        <svg class="icono" aria-hidden="true"><use href="${item.tipo == 'Producto' ? '#i-producto' : '#i-plato'}"/></svg>
                                        <c:out value="${item.tipo}"/>
                                    </span>
                                </td>
                                <td data-etiqueta="Precio" class="numero">$<fmt:formatNumber value="${item.precio}" pattern="0.00"/></td>
                                <td data-etiqueta="Estado"><span class="estado estado-${item.estado.toLowerCase()}"><c:out value="${item.estado}"/></span></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <%-- Mensajes 6-7: el personal confirma el cierre → cerrar() --%>
                <form method="post" action="${pageContext.request.contextPath}/personal/cerrar-menu" id="form-cerrar">
                    <input type="hidden" name="accion" value="cerrar">
                    <button type="submit" class="btn btn-peligro">
                        <svg class="icono" aria-hidden="true"><use href="#i-salir"/></svg>
                        Cerrar venta del día
                    </button>
                </form>
            </section>
        </c:otherwise>
    </c:choose>
</main>

<%-- Confirmación del cierre (mensaje 6). Sin JavaScript el formulario se envía directamente. --%>
<dialog class="dialogo" id="dialogo-cierre" aria-labelledby="dialogo-titulo" aria-describedby="dialogo-texto">
    <div class="dialogo__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-salir"/></svg></div>
    <h2 id="dialogo-titulo">¿Cerrar la venta del día?</h2>
    <p id="dialogo-texto">El menú pasará a estado <strong>Cerrado</strong> y los estudiantes ya no podrán pagar pedidos. Esta acción no se puede deshacer.</p>
    <div class="dialogo__acciones">
        <button type="button" class="btn btn-secundario" id="dialogo-cancelar">Cancelar</button>
        <button type="button" class="btn btn-peligro" id="dialogo-confirmar">
            <svg class="icono" aria-hidden="true"><use href="#i-salir"/></svg>
            Sí, cerrar la venta
        </button>
    </div>
</dialog>
<script>
    (function () {
        var dialogo = document.getElementById('dialogo-cierre');
        var form = document.getElementById('form-cerrar');
        if (!dialogo || !form || typeof dialogo.showModal !== 'function') return;
        var disparador = form.querySelector('button');

        form.addEventListener('submit', function (evento) {
            evento.preventDefault();
            dialogo.showModal();
            // Acción destructiva: el foco inicial va a la opción segura
            document.getElementById('dialogo-cancelar').focus();
        });
        document.getElementById('dialogo-cancelar').addEventListener('click', function () {
            dialogo.close();
        });
        document.getElementById('dialogo-confirmar').addEventListener('click', function () {
            this.disabled = true;
            this.setAttribute('aria-busy', 'true');
            form.submit();
        });
        dialogo.addEventListener('close', function () {
            if (!document.getElementById('dialogo-confirmar').disabled) disparador.focus();
        });
        dialogo.addEventListener('click', function (evento) {
            if (evento.target === dialogo) dialogo.close();
        });
    })();
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
