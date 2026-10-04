<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez): punto de entrada a los casos de uso
     del personal del comedor (Diagrama de casos de uso: Crear Menú, Retirar plato y/o producto del menú). --%>
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
    </div>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
