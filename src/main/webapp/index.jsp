<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Portal de inicio (soporte técnico): lleva a cada actor del Diagrama de casos de uso a su frontera:
     Cliente universitario → "Interfaz de pedidos de comida"; Personal del comedor → "Interfaz del sistema de comida". --%>
<c:set var="titulo" value="Inicio" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="portal" id="contenido">
    <div class="portal__contenido">
        <div class="portal__marca">
            <svg class="icono icono-lg" aria-hidden="true"><use href="#i-plato"/></svg>
            <h1>PoliDinner</h1>
        </div>
        <p class="portal__lema">Comedor universitario de la Escuela Politécnica Nacional. Pida su comida, pague con PoliCuenta y retírela con su código.</p>
        <div class="opciones">
            <a class="opcion" href="${pageContext.request.contextPath}/cliente/login">
                <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-estudiante"/></svg></span>
                <h2>Soy estudiante</h2>
                <p>Vea el menú de hoy, arme su pedido y pague con su PoliCuenta.</p>
                <span class="opcion__accion">Pedir comida &rarr;</span>
            </a>
            <a class="opcion" href="${pageContext.request.contextPath}/personal/login">
                <span class="opcion__icono"><svg class="icono icono-lg" aria-hidden="true"><use href="#i-chef"/></svg></span>
                <h2>Personal del comedor</h2>
                <p>Cree el menú del día, retire platos agotados y habilite la entrega de pedidos.</p>
                <span class="opcion__accion">Ingresar &rarr;</span>
            </a>
        </div>
    </div>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
