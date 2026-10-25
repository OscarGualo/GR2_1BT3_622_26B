<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz de reservas de mesa" de HU-01 Reservar una mesa (Diagrama de robustez HU-01).
     Diagrama de actividades HU-01: "Revisar mesas y horarios disponibles" y "Elegir una mesa y una franja de horario".
     Atributos: mesas (List<Mesa>) y franjas (List<Franja> que aún no comienzan). --%>
<c:set var="titulo" value="Reservar mesa" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/cliente/pedido">Menú de hoy</a> / Reservar mesa
    </nav>
    <div class="titulo-pagina">
        <h1>Reservar mesa</h1>
        <p>Elija una mesa y una franja de hoy. Puede tener una sola reserva activa por día.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty franjas}">
            <%-- HU-01 / Escenario 2.3 --%>
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-alerta"/></svg>
                <h2 id="sin-franjas">No hay franjas disponibles para hoy</h2>
                <p>Todas las franjas de reserva de hoy ya comenzaron.</p>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/pedido">Ver menú de hoy</a>
            </section>
        </c:when>
        <c:otherwise>
            <section class="tarjeta">
                <form method="post" action="${pageContext.request.contextPath}/cliente/reservar">
                    <div class="campo">
                        <label for="mesa">Mesa <span class="requerido" aria-hidden="true">*</span></label>
                        <select class="entrada" id="mesa" name="mesa" required>
                            <c:forEach var="mesa" items="${mesas}">
                                <option value="${mesa.numero}">Mesa <c:out value="${mesa.numero}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="campo">
                        <label for="franja">Franja <span class="requerido" aria-hidden="true">*</span></label>
                        <select class="entrada" id="franja" name="franja" required>
                            <c:forEach var="franja" items="${franjas}">
                                <option value="${franja}"><c:out value="${franja.etiqueta}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primario btn-bloque">
                        <svg class="icono" aria-hidden="true"><use href="#i-ok"/></svg>
                        Reservar
                    </button>
                </form>
            </section>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>