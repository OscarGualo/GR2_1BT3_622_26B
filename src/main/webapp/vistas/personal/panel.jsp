<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez): punto de entrada a los casos de uso
     del personal del comedor (Diagramas de casos de uso: Crear Menú, Retirar plato y/o producto del menú
     y, en el incremento 2, Habilitar entrega de comida; en el incremento 3, Cerrar menú y reporte de ventas). --%>
<c:set var="titulo" value="Panel del personal" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <div class="titulo-pagina">
        <h1>Hola, <c:out value="${sessionScope.personal.nombres}"/></h1>
        <p>Elija qué desea hacer con el menú del día del comedor.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>
    <%-- Tareas agrupadas por momento del día: preparar el menú, atender el servicio y consultar --%>
    <div class="grupos-acciones">
        <section class="grupo-acciones" aria-labelledby="grupo-menu">
            <h2 id="grupo-menu">Menú del día</h2>
            <ul class="lista-acciones">
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/crear-menu">
                        <svg class="icono" aria-hidden="true"><use href="#i-menu"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Crear menú</strong>
                            <span>Arme el menú del día con sus platos y los productos del inventario, y publíquelo para la venta.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/productos">
                        <svg class="icono" aria-hidden="true"><use href="#i-producto"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Productos</strong>
                            <span>Registre productos con su caducidad; sus unidades se conservan para los menús de otros días.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila accion-fila--peligro" href="${pageContext.request.contextPath}/personal/retirar">
                        <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Retirar plato y/o producto</strong>
                            <span>Retire un plato o producto de la oferta de hoy; los productos conservan sus unidades.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila accion-fila--peligro" href="${pageContext.request.contextPath}/personal/cerrar-menu">
                        <svg class="icono" aria-hidden="true"><use href="#i-salir"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Cerrar venta del día</strong>
                            <span>Cierre el menú publicado para que los estudiantes ya no puedan pagar pedidos.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
            </ul>
        </section>
        <section class="grupo-acciones" aria-labelledby="grupo-servicio">
            <h2 id="grupo-servicio">Servicio y ventas</h2>
            <ul class="lista-acciones">
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/entrega">
                        <svg class="icono" aria-hidden="true"><use href="#i-entrega"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Habilitar entrega</strong>
                            <span>Valide el código de retiro del comprobante y autorice el despacho del pedido.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/programados">
                        <svg class="icono" aria-hidden="true"><use href="#i-reloj"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Pedidos programados</strong>
                            <span>Vea los pedidos de hoy agrupados por franja y el total de cada producto a preparar.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/cupos">
                        <svg class="icono" aria-hidden="true"><use href="#i-menu"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Cupos de retiro</strong>
                            <span>Defina cuántos pedidos se retiran en cada franja de 15 minutos y cierre las que no pueda atender.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/reporte">
                        <svg class="icono" aria-hidden="true"><use href="#i-billetera"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Reporte de ventas</strong>
                            <span>Consulte el total vendido del día y la lista de pedidos con su estado.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
                <li>
                    <a class="accion-fila" href="${pageContext.request.contextPath}/personal/tablero">
                        <svg class="icono" aria-hidden="true"><use href="#i-menu"/></svg>
                        <span class="accion-fila__texto">
                            <strong>Ocupación de mesas</strong>
                            <span>Vea qué mesas están reservadas o libres en cada franja de hoy.</span>
                        </span>
                        <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                    </a>
                </li>
            </ul>
        </section>
    </div>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
