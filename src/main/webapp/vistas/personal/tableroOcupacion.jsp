<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Frontera "Interfaz de gestión de reservas" de HU-03 Ver la ocupación de mesas del día (Diagrama de robustez HU-03).
     Diagrama de actividades HU-03: "Completar el tablero", "Escribir el número de mesas libres" y
     "Colocar el tablero en un lugar visible". Atributos: tablero (TableroOcupacion) y franjas (Franja[]).
     Presentación (técnica, no modelada): plano por franja donde cada mesa es una "butaca". Usa solo
     obtenerEstado(), contarMesasLibres(), estaCompleta() y getMesas(); no se agregan datos al modelo. --%>
<c:set var="titulo" value="Ocupación de mesas" scope="request"/>
<c:set var="totalMesas" value="${fn:length(tablero.mesas)}"/>
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

    <section class="tarjeta tablero" aria-labelledby="titulo-tablero">
        <h2 id="titulo-tablero">Plano por franja</h2>

        <%-- Selector de franja: botones de pestaña creados como mejora progresiva (sin JS se ven todas las franjas) --%>
        <div class="franjas" role="tablist" aria-label="Franja" hidden data-pestanas>
            <c:forEach var="franja" items="${franjas}" varStatus="v">
                <button type="button" role="tab" class="franjas__pestana" id="pestana-${franja}"
                        aria-controls="plano-${franja}" aria-selected="${v.first}" tabindex="${v.first ? 0 : -1}">
                    <span class="franjas__hora"><c:out value="${franja.etiqueta}"/></span>
                    <span class="franjas__libres">
                        <c:choose>
                            <c:when test="${tablero.estaCompleta(franja)}">Completa</c:when>
                            <c:otherwise>${tablero.contarMesasLibres(franja)} ${tablero.contarMesasLibres(franja) == 1 ? 'libre' : 'libres'}</c:otherwise>
                        </c:choose>
                    </span>
                </button>
            </c:forEach>
        </div>

        <c:forEach var="franja" items="${franjas}">
            <section class="plano__panel" id="plano-${franja}" role="tabpanel"
                     aria-labelledby="titulo-plano-${franja}" data-panel>
                <div class="plano__cabecera">
                    <h3 id="titulo-plano-${franja}">
                        Franja <c:out value="${franja.etiqueta}"/>
                        <%-- HU-03 / Escenario 2.3 --%>
                        <c:if test="${tablero.estaCompleta(franja)}">
                            <span class="estado estado-agotado">Completa</span>
                        </c:if>
                    </h3>
                    <%-- HU-03 / Escenarios 2.1, 2.2 y 2.3: "Escribir el número de mesas libres" --%>
                    <dl class="cifras plano__cifra">
                        <div class="cifra cifra--principal">
                            <dt>Mesas libres</dt>
                            <dd>${tablero.contarMesasLibres(franja)} <span class="cifra__de">de ${totalMesas}</span></dd>
                        </div>
                    </dl>
                </div>

                <%-- HU-03 / Escenarios 1.1, 1.2 y 1.3: una butaca por mesa con el estado de obtenerEstado() --%>
                <ul class="plano" aria-label="Mesas en la franja ${franja.etiqueta}">
                    <c:forEach var="mesa" items="${tablero.mesas}">
                        <c:set var="estadoCelda" value="${tablero.obtenerEstado(mesa.numero, franja)}"/>
                        <c:set var="libre" value="${estadoCelda == 'Libre'}"/>
                        <li class="butaca ${libre ? 'butaca--libre' : 'butaca--reservada'}">
                            <span class="butaca__numero"><span class="solo-lector">Mesa </span><c:out value="${mesa.numero}"/></span>
                            <c:choose>
                                <c:when test="${libre}">
                                    <span class="butaca__estado">Libre</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="butaca__estado">Reservada</span>
                                    <span class="butaca__cliente"><c:out value="${fn:substringAfter(estadoCelda, ' · ')}"/></span>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:forEach>
                </ul>
            </section>
        </c:forEach>

        <p class="plano__nota">
            Las mesas se muestran en orden de número; el plano no reproduce su ubicación en el comedor.
        </p>
    </section>
</main>
<script>
    // Pestañas de franja (patrón ARIA "tabs"): solo cambia qué panel se ve; los datos ya vienen en la página.
    (function () {
        var lista = document.querySelector('[data-pestanas]');
        if (!lista) return;
        var pestanas = Array.prototype.slice.call(lista.querySelectorAll('[role="tab"]'));
        var paneles = document.querySelectorAll('[data-panel]');
        function mostrar(pestana, enfocar) {
            pestanas.forEach(function (p) {
                var activa = p === pestana;
                p.setAttribute('aria-selected', activa);
                p.tabIndex = activa ? 0 : -1;
                document.getElementById(p.getAttribute('aria-controls')).hidden = !activa;
            });
            if (enfocar) pestana.focus();
        }
        lista.hidden = false;
        paneles.forEach(function (panel) {
            var pestana = document.querySelector('[aria-controls="' + panel.id + '"]');
            panel.setAttribute('aria-labelledby', pestana.id);
            panel.tabIndex = 0;
        });
        pestanas.forEach(function (p, i) {
            p.addEventListener('click', function () { mostrar(p, false); });
            p.addEventListener('keydown', function (e) {
                var destino = null;
                if (e.key === 'ArrowRight') destino = pestanas[(i + 1) % pestanas.length];
                if (e.key === 'ArrowLeft') destino = pestanas[(i - 1 + pestanas.length) % pestanas.length];
                if (e.key === 'Home') destino = pestanas[0];
                if (e.key === 'End') destino = pestanas[pestanas.length - 1];
                if (destino) { e.preventDefault(); mostrar(destino, true); }
            });
        });
        mostrar(pestanas[0], false);
    })();
</script>
<jsp:include page="/vistas/comun/pie.jsp"/>
