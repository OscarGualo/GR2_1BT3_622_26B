<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Frontera "Interfaz del sistema de comida": plantilla común, parte inferior.
     Si la página muestra una fotografía (atributo fotosUsadas: "electrica", "cec" u "observatorio"),
     aquí va el crédito que exige su licencia. Lista completa en img/credits.json. --%>
<footer class="pie">
    <div class="pie__contenido">
        <p>PoliDinner · Comedor universitario · Escuela Politécnica Nacional</p>
        <c:if test="${not empty fotosUsadas}">
            <p class="pie__creditos">Fotografía:
                <c:choose>
                    <c:when test="${fotosUsadas == 'electrica'}">
                        <a href="https://commons.wikimedia.org/wiki/File:Facultad_de_Ingenier%C3%ADa_El%C3%A9ctrica,_Escuela_Polit%C3%A9cnica_Nacional_Quito,_Ecuador..JPG">“Facultad de Ingeniería Eléctrica, Escuela Politécnica Nacional Quito, Ecuador.”</a>
                        de Aracely Morales, <a href="https://creativecommons.org/licenses/by-sa/4.0/deed.es">CC BY-SA 4.0</a>, vía Wikimedia Commons.
                    </c:when>
                    <c:when test="${fotosUsadas == 'cec'}">
                        <a href="https://commons.wikimedia.org/wiki/File:CEC_EPN_Classroom_Building_at_Escuela_Polit%C3%A9cnica_Nacional_Quito,_Ecuador..JPG">“CEC EPN Classroom Building at Escuela Politécnica Nacional Quito, Ecuador.”</a>
                        de Aracely Morales, <a href="https://creativecommons.org/licenses/by-sa/4.0/deed.es">CC BY-SA 4.0</a>, vía Wikimedia Commons.
                    </c:when>
                    <c:when test="${fotosUsadas == 'observatorio'}">
                        <a href="https://commons.wikimedia.org/wiki/File:Quito_Observatory.JPG">“Quito Observatory”</a>
                        de Marc Figueras, <a href="https://creativecommons.org/licenses/by/2.5/deed.es">CC BY 2.5</a>, vía Wikimedia Commons.
                    </c:when>
                </c:choose>
            </p>
        </c:if>
    </div>
</footer>
</body>
</html>
