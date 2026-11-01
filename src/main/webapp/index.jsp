<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Portal de inicio (soporte técnico): lleva a cada actor del Diagrama de casos de uso a su frontera:
     Cliente universitario → "Interfaz de pedidos de comida"; Personal del comedor → "Interfaz del sistema de comida".
     La fotografía es local (img/), con su crédito en el pie: la página funciona sin conexión a Internet. --%>
<c:set var="titulo" value="Inicio" scope="request"/>
<c:set var="fotosUsadas" value="electrica" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="portada" id="contenido">
    <div class="portada__texto">
        <div class="portal__marca">
            <h1>PoliDinner</h1>
        </div>
        <p class="portada__lema">Comedor universitario de la Escuela Politécnica Nacional. Pida su comida, pague con PoliCuenta y retírela con su código.</p>
        <h2 class="solo-lector">Elija cómo ingresar</h2>
        <ul class="lista-acciones lista-acciones--portal">
            <li>
                <a class="accion-fila" href="${pageContext.request.contextPath}/cliente/login">
                    <svg class="icono" aria-hidden="true"><use href="#i-estudiante"/></svg>
                    <span class="accion-fila__texto">
                        <strong>Soy estudiante</strong>
                        <span>Vea el menú de hoy, arme su pedido y pague con su PoliCuenta.</span>
                    </span>
                    <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                </a>
            </li>
            <li>
                <a class="accion-fila" href="${pageContext.request.contextPath}/personal/login">
                    <svg class="icono" aria-hidden="true"><use href="#i-chef"/></svg>
                    <span class="accion-fila__texto">
                        <strong>Personal del comedor</strong>
                        <span>Cree el menú del día, retire platos agotados y habilite la entrega de pedidos.</span>
                    </span>
                    <svg class="icono accion-fila__flecha" aria-hidden="true"><use href="#i-flecha"/></svg>
                </a>
            </li>
        </ul>
    </div>
    <figure class="foto-lateral foto-lateral--electrica portada__foto">
        <img src="${pageContext.request.contextPath}/img/facultad-electrica-epn.jpg" width="1280" height="960"
             alt="Torre blanca con franjas azules de la Facultad de Ingeniería Eléctrica, en el campus de la Escuela Politécnica Nacional"
             decoding="async" fetchpriority="high">
        <figcaption>Facultad de Ingeniería Eléctrica, Escuela Politécnica Nacional</figcaption>
    </figure>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
