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
    <div class="opciones">
        <a class="opcion" href="${pageContext.request.contextPath}/personal/crear-menu">
            <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-menu"/></svg></span>
            <h2>Crear menú</h2>
            <p>Arme el menú del día con platos y productos y publíquelo para la venta.</p>
            <span class="opcion__accion">Ir a crear menú &rarr;</span>
        </a>
        <a class="opcion opcion--peligro" href="${pageContext.request.contextPath}/personal/retirar">
            <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-retirar"/></svg></span>
            <h2>Retirar plato y/o producto</h2>
            <p>Marque como agotado un plato o producto cuyas porciones se terminaron.</p>
            <span class="opcion__accion">Ir a retirar ítems &rarr;</span>
        </a>
        <a class="opcion" href="${pageContext.request.contextPath}/personal/entrega">
            <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-entrega"/></svg></span>
            <h2>Habilitar entrega</h2>
            <p>Valide el código de retiro del comprobante y autorice el despacho del pedido.</p>
            <span class="opcion__accion">Validar código &rarr;</span>
        </a>
        <a class="opcion opcion--peligro" href="${pageContext.request.contextPath}/personal/cerrar-menu">
            <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-salir"/></svg></span>
            <h2>Cerrar venta del día</h2>
            <p>Cierre el menú publicado para que los estudiantes ya no puedan pagar pedidos.</p>
            <span class="opcion__accion">Ir a cerrar venta &rarr;</span>
        </a>
        <a class="opcion" href="${pageContext.request.contextPath}/personal/reporte">
            <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-billetera"/></svg></span>
            <h2>Reporte de ventas</h2>
            <p>Consulte el total vendido del día y la lista de pedidos con su estado.</p>
            <span class="opcion__accion">Ver reporte &rarr;</span>
        </a>
    </div>
            <a class="opcion" href="${pageContext.request.contextPath}/personal/tablero">
                <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-menu"/></svg></span>
                <h2>Ocupación de mesas</h2>
                <p>Vea qué mesas están reservadas o libres en cada franja de hoy.</p>
                <span class="opcion__accion">Ver tablero &rarr;</span>
            </a>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
