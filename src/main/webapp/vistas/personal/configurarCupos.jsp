<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Examen 1B, HU15 «Configurar el cupo de las franjas» (T15.3).
     Atributos: cupoGeneral (int), franjas (List<DisponibilidadFranja> de hoy, 11:00 a 15:00) y fechaHoy.
     Cada franja tiene su propio formulario: fijar un cupo de hora pico y cerrar o abrir la franja.
     Los rechazos (cupo no válido, cupo menor que los pedidos asignados) llegan por mensajes.jsp. --%>
<fmt:setLocale value="en_US"/>
<c:set var="titulo" value="Cupos de retiro" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Cupos de retiro
    </nav>
    <div class="titulo-pagina">
        <h1>Cupos de retiro</h1>
        <p>Cuántos pedidos para llevar se retiran en cada franja de 15 minutos. Una franja cerrada no admite pedidos nuevos.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <section class="cupo-general" aria-labelledby="titulo-general">
        <h2 id="titulo-general">Cupo general</h2>
        <form method="post" action="${pageContext.request.contextPath}/personal/cupos" class="cupo-general__form">
            <input type="hidden" name="accion" value="cupoGeneral">
            <div class="campo">
                <label for="cupo-general">Pedidos por franja</label>
                <input class="entrada" type="number" inputmode="numeric" min="1" step="1" id="cupo-general" name="cupo"
                       value="${cupoGeneral}" required aria-describedby="ayuda-general">
                <p class="ayuda" id="ayuda-general">Número entero mayor que cero. Rige en todas las franjas que no tengan un cupo propio.</p>
            </div>
            <button type="submit" class="btn btn-primario">Guardar cupo general</button>
        </form>
    </section>

    <section aria-labelledby="titulo-franjas" class="cupos-franjas">
        <h2 id="titulo-franjas">Franjas de hoy</h2>
        <div class="tabla-contenedor">
            <table class="tabla tabla-cupos">
                <caption class="solo-lector">Franjas de retiro de hoy con pedidos asignados, cupo y estado</caption>
                <thead>
                <tr>
                    <th scope="col">Franja</th>
                    <th scope="col" class="numero">Pedidos</th>
                    <th scope="col" class="numero">Cupo</th>
                    <th scope="col">Estado</th>
                    <th scope="col">Cupo propio</th>
                    <th scope="col" class="acciones"><span class="solo-lector">Cerrar o abrir</span></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="franja" items="${franjas}">
                    <tr id="franja-${franja.inicio}">
                        <th scope="row" data-etiqueta="Franja" class="codigo-celda"><c:out value="${franja.etiqueta}"/></th>
                        <td data-etiqueta="Pedidos" class="numero">${franja.asignados}</td>
                        <td data-etiqueta="Cupo" class="numero">${franja.cupoEfectivo}<c:if test="${not empty franja.cupoEspecifico}"><span class="solo-lector"> (propio)</span></c:if></td>
                        <td data-etiqueta="Estado">
                            <c:choose>
                                <c:when test="${franja.cerrada}"><span class="estado estado-cerrado">Cerrada</span></c:when>
                                <c:when test="${franja.estado.name() == 'LLENA'}"><span class="estado estado-agotado">Llena</span></c:when>
                                <c:otherwise><span class="estado estado-disponible">Abierta</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td data-etiqueta="Cupo propio">
                            <form method="post" action="${pageContext.request.contextPath}/personal/cupos" class="cupo-franja">
                                <input type="hidden" name="accion" value="cupoFranja">
                                <input type="hidden" name="franja" value="${franja.inicio}">
                                <label class="solo-lector" for="cupo-${franja.inicio}">Cupo de la franja <c:out value="${franja.etiqueta}"/></label>
                                <input class="entrada entrada--corta" type="number" inputmode="numeric" min="1" step="1"
                                       id="cupo-${franja.inicio}" name="cupo" value="${franja.cupoEspecifico}"
                                       placeholder="General" required>
                                <button type="submit" class="btn btn-secundario btn-chico">Fijar<span class="solo-lector"> cupo de <c:out value="${franja.etiqueta}"/></span></button>
                            </form>
                        </td>
                        <td class="acciones">
                            <form method="post" action="${pageContext.request.contextPath}/personal/cupos">
                                <input type="hidden" name="franja" value="${franja.inicio}">
                                <c:choose>
                                    <c:when test="${franja.cerrada}">
                                        <input type="hidden" name="accion" value="abrir">
                                        <button type="submit" class="btn btn-secundario btn-chico">Abrir<span class="solo-lector"> franja <c:out value="${franja.etiqueta}"/></span></button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="accion" value="cerrar">
                                        <button type="submit" class="btn btn-secundario btn-borde-peligro btn-chico">Cerrar<span class="solo-lector"> franja <c:out value="${franja.etiqueta}"/></span></button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>
