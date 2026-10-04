<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Salida de mensajes de los controladores: éxito, error simple o lista de errores de validación --%>
<c:if test="${not empty exito}">
    <div class="alerta alerta-exito" role="status">
        <svg class="icono" aria-hidden="true"><use href="#i-ok"/></svg>
        <span id="mensaje-exito"><c:out value="${exito}"/></span>
    </div>
</c:if>
<c:if test="${not empty error or not empty errores}">
    <div class="alerta alerta-error" role="alert" tabindex="-1" id="resumen-errores">
        <svg class="icono" aria-hidden="true"><use href="#i-alerta"/></svg>
        <div>
            <c:if test="${not empty error}"><span><c:out value="${error}"/></span></c:if>
            <c:if test="${not empty errores}">
                <span>Revise los datos del formulario:</span>
                <ul>
                    <c:forEach var="e" items="${errores}">
                        <li><a href="#${e.key}"><c:out value="${e.value}"/></a></li>
                    </c:forEach>
                </ul>
            </c:if>
        </div>
    </div>
</c:if>
