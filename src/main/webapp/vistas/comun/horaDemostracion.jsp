<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Soporte técnico (no modelado): panel "Modo demostración" para probar las reservas a cualquier hora.
     Atributos (RelojAplicacion.exponer): horaComedor (LocalTime), horaSimulada (boolean), modoDemo (boolean).
     Parámetro de inclusión: volver (/cliente/reservar o /cliente/mi-reserva). --%>
<c:if test="${modoDemo}">
    <section class="tarjeta" aria-labelledby="titulo-demo">
        <h2 id="titulo-demo">Modo demostración</h2>
        <p>
            Hora del comedor: <strong id="hora-comedor"><c:out value="${horaComedor}"/></strong>
            <c:choose>
                <c:when test="${horaSimulada}">(simulada)</c:when>
                <c:otherwise>(real)</c:otherwise>
            </c:choose>
        </p>
        <form method="post" action="${pageContext.request.contextPath}/cliente/hora-demostracion">
            <input type="hidden" name="volver" value="${param.volver}">
            <div class="campo">
                <label for="hora-demo">Fijar hora</label>
                <input class="entrada" type="time" id="hora-demo" name="hora" value="${horaComedor}" required>
            </div>
            <div class="acciones">
                <button type="submit" name="accion" value="fijar" class="btn btn-primario btn-chico">Fijar hora</button>
            </div>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/cliente/hora-demostracion">
            <input type="hidden" name="volver" value="${param.volver}">
            <div class="acciones">
                <%-- Horas de los escenarios de PLANIFICACION_INCREMENTO_RESERVAS.md --%>
                <button type="submit" name="accion" value="fijar" class="btn btn-secundario btn-chico"
                        formaction="${pageContext.request.contextPath}/cliente/hora-demostracion?hora=11:00"
                        title="HU-01 1.1 a 1.3 y 2.1: todas las franjas disponibles">11:00</button>
                <button type="submit" name="accion" value="fijar" class="btn btn-secundario btn-chico"
                        formaction="${pageContext.request.contextPath}/cliente/hora-demostracion?hora=12:00"
                        title="HU-02 1.1: cancelar la franja 12:30 con anticipación">12:00</button>
                <button type="submit" name="accion" value="fijar" class="btn btn-secundario btn-chico"
                        formaction="${pageContext.request.contextPath}/cliente/hora-demostracion?hora=12:15"
                        title="HU-02 1.2: cancelar la franja 12:30 en el límite exacto de 15 minutos">12:15</button>
                <button type="submit" name="accion" value="fijar" class="btn btn-secundario btn-chico"
                        formaction="${pageContext.request.contextPath}/cliente/hora-demostracion?hora=12:20"
                        title="HU-02 1.3: cancelar la franja 12:30 queda fuera de plazo">12:20</button>
                <button type="submit" name="accion" value="fijar" class="btn btn-secundario btn-chico"
                        formaction="${pageContext.request.contextPath}/cliente/hora-demostracion?hora=12:40"
                        title="HU-01 2.2: la franja 12:30 ya comenzó">12:40</button>
                <button type="submit" name="accion" value="fijar" class="btn btn-secundario btn-chico"
                        formaction="${pageContext.request.contextPath}/cliente/hora-demostracion?hora=13:10"
                        title="HU-01 2.3: ya comenzaron todas las franjas">13:10</button>
                <button type="submit" name="accion" value="real" class="btn btn-secundario btn-chico">Usar hora real</button>
            </div>
        </form>
    </section>
</c:if>
