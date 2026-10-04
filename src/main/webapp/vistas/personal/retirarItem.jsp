<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez) para el CU 02 Retirar plato y/o producto.
     Diagrama de secuencia CU02: mensajes 1 (solicita retirar), 7 (muestra ítems "Disponible"),
     8 (selecciona ítem y confirma el retiro) y 19 (muestra mensaje y lista sin el ítem retirado). --%>
<%-- Precios con punto decimal (USD), independientemente del idioma del navegador --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Retirar plato o producto" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Retirar plato o producto
    </nav>
    <div class="titulo-pagina">
        <h1>Retirar plato o producto del menú</h1>
        <p>Marque como agotado un ítem cuyas porciones se terminaron. El ítem deja de ofrecerse a los comensales, pero su registro se conserva.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty menuHoy}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-menu"/></svg>
                <h2>No hay un menú para hoy</h2>
                <p>Primero cree el menú del día; luego podrá retirar sus platos y productos.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/personal/crear-menu">
                    <svg class="icono" aria-hidden="true"><use href="#i-mas"/></svg>
                    Ir a crear menú
                </a>
            </section>
        </c:when>
        <c:otherwise>
            <%-- Mensaje 7: ítems con estado "Disponible" del menú de hoy --%>
            <section class="tarjeta" aria-labelledby="titulo-items">
                <div class="tarjeta__encabezado">
                    <h2 id="titulo-items">Ítems disponibles</h2>
                    <span class="estado estado-${menuHoy.estado.toLowerCase()}">Menú <c:out value="${menuHoy.estado}"/></span>
                </div>
                <div class="resumen-menu">
                    <span>Fecha: <strong><fmt:formatDate value="${menuHoy.fecha}" pattern="dd/MM/yyyy"/></strong></span>
                    <span>Disponibles: <strong>${items.size()}</strong></span>
                    <span>Retirados: <strong>${retirados}</strong></span>
                </div>
                <c:choose>
                    <c:when test="${empty items}">
                        <div class="vacio">
                            <svg class="icono icono-lg" aria-hidden="true"><use href="#i-plato"/></svg>
                            <p>No quedan ítems disponibles en el menú de hoy.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="tabla-contenedor">
                            <table class="tabla">
                                <caption class="solo-lector">Platos y productos disponibles en el menú de hoy</caption>
                                <thead>
                                <tr>
                                    <th scope="col">Nombre</th>
                                    <th scope="col">Tipo</th>
                                    <th scope="col" class="numero">Precio</th>
                                    <th scope="col">Estado</th>
                                    <th scope="col" class="acciones"><span class="solo-lector">Acción</span></th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${items}">
                                    <tr>
                                        <td data-etiqueta="Nombre">
                                            <strong><c:out value="${item.nombre}"/></strong>
                                            <c:if test="${not empty item.descripcion}"><div class="descripcion"><c:out value="${item.descripcion}"/></div></c:if>
                                        </td>
                                        <td data-etiqueta="Tipo" class="tipo">
                                            <span class="tipo__etiqueta">
                                                <svg class="icono" aria-hidden="true"><use href="${item.tipo == 'Producto' ? '#i-producto' : '#i-plato'}"/></svg>
                                                <c:out value="${item.tipo}"/>
                                            </span>
                                            <c:if test="${item.tipo == 'Producto'}"> · stock ${item.stock}</c:if>
                                        </td>
                                        <td data-etiqueta="Precio" class="numero">$<fmt:formatNumber value="${item.precio}" pattern="0.00"/></td>
                                        <td data-etiqueta="Estado"><span class="estado estado-${item.estado.toLowerCase()}"><c:out value="${item.estado}"/></span></td>
                                        <td class="acciones">
                                            <%-- Mensajes 8-9: selecciona el ítem → retirarItem(idItem) --%>
                                            <form method="post" action="${pageContext.request.contextPath}/personal/retirar"
                                                  class="form-retirar" data-nombre="<c:out value='${item.nombre}'/>">
                                                <input type="hidden" name="accion" value="retirar">
                                                <input type="hidden" name="idItem" value="${item.id}">
                                                <input type="hidden" name="version" value="${item.version}">
                                                <button type="submit" class="btn btn-peligro btn-chico">
                                                    <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
                                                    Retirar<span class="solo-lector"> <c:out value="${item.nombre}"/></span>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:otherwise>
    </c:choose>
</main>

<%-- Confirmación del retiro (mensaje 8). Sin JavaScript el formulario se envía directamente. --%>
<dialog class="dialogo" id="dialogo-retiro" aria-labelledby="dialogo-titulo" aria-describedby="dialogo-texto">
    <div class="dialogo__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-retirar"/></svg></div>
    <h2 id="dialogo-titulo">¿Retirar <span id="dialogo-nombre"></span> del menú?</h2>
    <p id="dialogo-texto">El ítem pasará a estado <strong>Agotado</strong> y los comensales ya no podrán pedirlo.</p>
    <div class="dialogo__acciones">
        <button type="button" class="btn btn-secundario" id="dialogo-cancelar">Cancelar</button>
        <button type="button" class="btn btn-peligro" id="dialogo-confirmar">
            <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
            Sí, retirar
        </button>
    </div>
</dialog>
<script>
    (function () {
        var dialogo = document.getElementById('dialogo-retiro');
        if (!dialogo || typeof dialogo.showModal !== 'function') return;
        var formPendiente = null;
        var disparador = null;

        document.querySelectorAll('.form-retirar').forEach(function (form) {
            form.addEventListener('submit', function (evento) {
                evento.preventDefault();
                formPendiente = form;
                disparador = form.querySelector('button');
                document.getElementById('dialogo-nombre').textContent = '"' + form.dataset.nombre + '"';
                dialogo.showModal();
                // Acción destructiva: el foco inicial va a la opción segura
                document.getElementById('dialogo-cancelar').focus();
            });
        });

        document.getElementById('dialogo-cancelar').addEventListener('click', function () {
            dialogo.close();
        });
        document.getElementById('dialogo-confirmar').addEventListener('click', function () {
            if (!formPendiente) return;
            this.disabled = true;
            this.setAttribute('aria-busy', 'true');
            formPendiente.submit();
        });
        dialogo.addEventListener('close', function () {
            if (disparador && !document.getElementById('dialogo-confirmar').disabled) disparador.focus();
            formPendiente = null;
        });
        dialogo.addEventListener('click', function (evento) {
            if (evento.target === dialogo) dialogo.close();
        });
    })();
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
