<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz de reservas de mesa" de HU-02 Cancelar mi reserva (Diagrama de robustez HU-02).
     Diagrama de actividades HU-02: "Solicitar cancelación de su reserva", "Elegir reserva a cancelar"
     y "Recibir confirmación de cancelación exitosa". Atributo: reserva (Reserva activa de hoy, o null). --%>
<c:set var="titulo" value="Mi reserva" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/cliente/pedido">Menú de hoy</a> / Mi reserva
    </nav>
    <div class="titulo-pagina">
        <h1>Mi reserva</h1>
        <p>Puede cancelar hasta 15 minutos antes de su franja. Si cancela más tarde, no podrá reservar otra mesa hoy.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <c:choose>
        <c:when test="${empty reserva}">
            <section class="tarjeta vacio">
                <svg class="icono icono-lg" aria-hidden="true"><use href="#i-ticket"/></svg>
                <h2 id="sin-reserva">No tiene reservas activas para hoy</h2>
                <a class="btn btn-primario" href="${pageContext.request.contextPath}/cliente/reservar">Reservar mesa</a>
            </section>
        </c:when>
        <c:otherwise>
            <section class="tarjeta" aria-labelledby="titulo-reserva">
                <h2 id="titulo-reserva">Reserva de hoy</h2>
                <dl class="resumen-pago">
                    <div><dt>Mesa</dt><dd id="reserva-mesa"><c:out value="${reserva.mesa.numero}"/></dd></div>
                    <div><dt>Franja</dt><dd id="reserva-franja"><c:out value="${reserva.franja.etiqueta}"/></dd></div>
                    <div><dt>Estado</dt><dd><span class="estado estado-pagado"><c:out value="${reserva.estado}"/></span></dd></div>
                </dl>
                <form method="post" action="${pageContext.request.contextPath}/cliente/mi-reserva"
                      onsubmit="return confirm('¿Cancelar su reserva de la mesa ${reserva.mesa.numero}?');">
                    <input type="hidden" name="idReserva" value="${reserva.id}">
                    <button type="submit" class="btn btn-secundario">
                        <svg class="icono" aria-hidden="true"><use href="#i-retirar"/></svg>
                        Cancelar reserva
                    </button>
                </form>
            </section>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>