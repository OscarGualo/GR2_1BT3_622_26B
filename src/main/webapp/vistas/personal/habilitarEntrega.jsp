<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez del incremento 2) para el CU 04.
     Diagrama de secuencia CU04: mensajes 1 (el personal ingresa el código del comprobante) y
     2 (habilitarEntrega(codigo)). El resultado se muestra en la frontera "Salida". --%>
<c:set var="titulo" value="Habilitar entrega" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Habilitar entrega
    </nav>
    <div class="titulo-pagina">
        <h1>Habilitar entrega de comida</h1>
        <p>Pida al cliente su comprobante e ingrese el código de retiro para autorizar el despacho del pedido.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="tarjeta login__tarjeta" aria-labelledby="titulo-codigo">
        <h2 id="titulo-codigo">Código del comprobante</h2>
        <form method="post" action="${pageContext.request.contextPath}/personal/entrega" novalidate>
            <div class="campo">
                <label for="codigo">Código de retiro <span class="requerido" aria-hidden="true">*</span></label>
                <input class="entrada entrada-codigo" type="text" id="codigo" name="codigo" required
                       maxlength="12" autocomplete="off" autocapitalize="characters" spellcheck="false"
                       placeholder="PD-0000" value="<c:out value='${param.codigo}'/>" autofocus
                       aria-describedby="ayuda-codigo${not empty errores.codigo ? ' error-codigo' : ''}"
                       <c:if test="${not empty errores.codigo}">aria-invalid="true"</c:if>>
                <p class="ayuda" id="ayuda-codigo">Formato PD-0000. También puede escribir solo el número (por ejemplo, 1).</p>
                <c:if test="${not empty errores.codigo}"><p class="error-campo" id="error-codigo"><c:out value="${errores.codigo}"/></p></c:if>
            </div>
            <button type="submit" class="btn btn-primario btn-bloque">
                <svg class="icono" aria-hidden="true"><use href="#i-entrega"/></svg>
                Validar y autorizar despacho
            </button>
        </form>
    </section>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
