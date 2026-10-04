<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz del sistema de comida" (Diagrama de robustez): plantilla común, parte superior.
     Uso: <c:set var="titulo" value="..." scope="request"/> y luego <jsp:include page="/vistas/comun/encabezado.jsp"/> --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty titulo ? 'PoliDinner' : titulo.concat(' · PoliDinner')}"/></title>
    <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns=%27http://www.w3.org/2000/svg%27 viewBox=%270 0 24 24%27 fill=%27none%27 stroke=%27%231E3A5F%27 stroke-width=%272%27 stroke-linecap=%27round%27%3E%3Cpath d=%27M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2M7 2v20M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7%27/%3E%3C/svg%3E">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<svg xmlns="http://www.w3.org/2000/svg" style="display:none" aria-hidden="true">
    <defs>
        <symbol id="i-plato" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></symbol>
        <symbol id="i-producto" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m7.5 4.27 9 5.15"/><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/></symbol>
        <symbol id="i-menu" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="8" height="4" x="8" y="2" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/></symbol>
        <symbol id="i-mas" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></symbol>
        <symbol id="i-retirar" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="m4.9 4.9 14.2 14.2"/></symbol>
        <symbol id="i-publicar" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/></symbol>
        <symbol id="i-salir" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></symbol>
        <symbol id="i-ok" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><path d="m9 11 3 3L22 4"/></symbol>
        <symbol id="i-alerta" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/></symbol>
    </defs>
</svg>
<a class="saltar-contenido" href="#contenido">Saltar al contenido</a>
<c:if test="${not empty sessionScope.personal}">
    <header class="barra">
        <div class="barra__contenido">
            <a class="marca" href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-plato"/></svg>
                <span>PoliDinner <small>Sistema de comida</small></span>
            </a>
            <div class="barra__usuario">
                <span>
                    <c:out value="${sessionScope.personal.nombres} ${sessionScope.personal.apellidos}"/>
                    <span class="rol"><c:out value="${sessionScope.personal.rol}"/></span>
                </span>
                <form method="post" action="${pageContext.request.contextPath}/personal/logout">
                    <button type="submit" class="btn btn-secundario btn-chico btn-salir">
                        <svg class="icono" aria-hidden="true"><use href="#i-salir"/></svg>
                        Cerrar sesión
                    </button>
                </form>
            </div>
        </div>
    </header>
</c:if>
<%-- Mensajes "flash" que dejan los controladores antes de redirigir (patrón POST-redirect-GET) --%>
<c:if test="${not empty sessionScope.flashExito}">
    <c:set var="exito" value="${sessionScope.flashExito}" scope="request"/>
    <c:remove var="flashExito" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.flashError}">
    <c:set var="error" value="${sessionScope.flashError}" scope="request"/>
    <c:remove var="flashError" scope="session"/>
</c:if>
