<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Soporte técnico (no modelado): página de error para 404 y 500 declarada en web.xml.
     Muestra un mensaje comprensible sin detalles internos; el error completo queda en el log de Tomcat. --%>
<c:set var="noEncontrada" value="${pageContext.errorData.statusCode == 404}"/>
<c:set var="titulo" value="${noEncontrada ? 'Página no encontrada' : 'Ocurrió un error'}" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <section class="tarjeta vacio">
        <svg class="icono icono-lg" aria-hidden="true"><use href="#i-alerta"/></svg>
        <c:choose>
            <c:when test="${noEncontrada}">
                <h1>Página no encontrada</h1>
                <p>La dirección que abrió no existe en PoliDinner.</p>
            </c:when>
            <c:otherwise>
                <h1>Ocurrió un error inesperado</h1>
                <p>No se pudo completar la operación. Intente de nuevo en unos minutos; si el problema continúa, avise al administrador.</p>
            </c:otherwise>
        </c:choose>
        <a class="btn btn-primario" href="${pageContext.request.contextPath}/">Ir al inicio</a>
    </section>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
