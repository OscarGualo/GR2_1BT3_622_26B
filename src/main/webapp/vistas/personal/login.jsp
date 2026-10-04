<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz del sistema de comida": inicio de sesión del personal del comedor --%>
<c:set var="titulo" value="Iniciar sesión" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="login" id="contenido">
    <div class="tarjeta login__tarjeta">
        <div class="login__marca">
            <svg class="icono icono-lg" aria-hidden="true"><use href="#i-plato"/></svg>
            <div>
                <h1>PoliDinner</h1>
                <p>Personal del comedor · EPN</p>
            </div>
        </div>
        <jsp:include page="/vistas/comun/mensajes.jsp"/>
        <form method="post" action="${pageContext.request.contextPath}/personal/login" novalidate>
            <div class="campo">
                <label for="cedula">Cédula <span class="requerido" aria-hidden="true">*</span></label>
                <input class="entrada" type="text" id="cedula" name="cedula" required
                       inputmode="numeric" autocomplete="username" maxlength="10"
                       value="<c:out value='${cedula}'/>" autofocus>
            </div>
            <div class="campo">
                <label for="clave">Clave <span class="requerido" aria-hidden="true">*</span></label>
                <input class="entrada" type="password" id="clave" name="clave" required
                       autocomplete="current-password">
            </div>
            <button type="submit" class="btn btn-primario btn-bloque">Ingresar</button>
        </form>
    </div>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
