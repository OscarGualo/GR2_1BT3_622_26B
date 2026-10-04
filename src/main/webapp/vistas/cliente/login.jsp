<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz de pedidos de comida": inicio de sesión del cliente universitario
     (precondición "login" del Diagrama de secuencia CU03) --%>
<c:set var="titulo" value="Iniciar sesión" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="login" id="contenido">
    <div class="tarjeta login__tarjeta">
        <div class="login__marca">
            <svg class="icono icono-lg" aria-hidden="true"><use href="#i-estudiante"/></svg>
            <div>
                <h1>PoliDinner</h1>
                <p>Pedidos de comida · Estudiantes EPN</p>
            </div>
        </div>
        <jsp:include page="/vistas/comun/mensajes.jsp"/>
        <form method="post" action="${pageContext.request.contextPath}/cliente/login" novalidate>
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
        <p class="ayuda login__ayuda">
            ¿Trabaja en el comedor? <a href="${pageContext.request.contextPath}/personal/login">Ingreso del personal</a>
        </p>
    </div>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
