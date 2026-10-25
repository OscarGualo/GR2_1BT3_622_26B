<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz de gestión de reservas" de HU-03 Ver la ocupación de mesas del día (Diagrama de robustez HU-03).
     Diagrama de actividades HU-03: "Completar el tablero", "Escribir el número de mesas libres" y
     "Colocar el tablero en un lugar visible". Atributos: tablero (TableroOcupacion) y franjas (Franja[]). --%>
<c:set var="titulo" value="Ocupación de mesas" scope="request"/>
<jsp:include page="/vistas/comun/encabezado.jsp"/>
<main class="contenido" id="contenido">
    <nav class="migas" aria-label="Ruta">
        <a href="${pageContext.request.contextPath}/vistas/personal/panel.jsp">Panel</a> / Ocupación de mesas
    </nav>
    <div class="titulo-pagina">
        <h1>Ocupación de mesas de hoy</h1>
        <p>Estado de cada mesa en cada franja según las reservas activas.</p>
    </div>
    <jsp:include page="/vistas/comun/mensajes.jsp"/>

    <%-- HU-03 / Escenario 2.1 --%>
    <c:if test="${not tablero.hayReservas()}">
        <div class="alerta alerta-info" role="status">
            <svg class="icono" aria-hidden="true"><use href="#i-alerta"/></svg>
            <span id="sin-reservas">No hay reservas para hoy</span>
        </div>
    </c:if>

    <section class="tarjeta" aria-labelledby="titulo-tablero">
        <h2 id="titulo-tablero">Tablero</h2>
        <div class="tabla-contenedor">
            <table class="tabla">
                <caption class="solo-lector">Estado de cada mesa por franja y mesas libres</caption>
                <thead>
                <tr>
                    <th scope="col">Mesa</th>
                    <c:forEach var="franja" items="${franjas}">
                        <th scope="col">
                            <c:out value="${franja.etiqueta}"/>
                                <%-- HU-03 / Escenario 2.3 --%>
                            <c:if test="${tablero.estaCompleta(franja)}">
                                <span class="estado estado-agotado">Completa</span>
                            </c:if>
                        </th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <%-- HU-03 / Escenarios 1.1, 1.2 y 1.3 --%>
                <c:forEach var="mesa" items="${tablero.mesas}">
                    <tr>
                        <th scope="row">Mesa <c:out value="${mesa.numero}"/></th>
                        <c:forEach var="franja" items="${franjas}">
                            <c:set var="estadoCelda" value="${tablero.obtenerEstado(mesa.numero, franja)}"/>
                            <td data-etiqueta="${franja.etiqueta}">
                                <span class="estado ${estadoCelda == 'Libre' ? 'estado-disponible' : 'estado-pagado'}">
                                    <c:out value="${estadoCelda}"/>
                                </span>
                            </td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                </tbody>
                <tfoot>
                <%-- HU-03 / Escenarios 2.1, 2.2 y 2.3 --%>
                <tr>
                    <th scope="row">Mesas libres</th>
                    <c:forEach var="franja" items="${franjas}">
                        <td class="numero"><strong><c:out value="${tablero.contarMesasLibres(franja)}"/></strong></td>
                    </c:forEach>
                </tr>
                </tfoot>
            </table>
        </div>
    </section>
</main>
<jsp:include page="/vistas/comun/pie.jsp"/>