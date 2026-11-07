# Comprobación de HU07: saldo insuficiente

## Cambio

La JSP del carrito deshabilita el pago inmediato cuando `saldoSuficiente` es falso.
El JavaScript que ya existía en esa JSP actualiza el bloqueo al cambiar de modo.
«Programar para llevar» conserva el envío al servidor para obtener el rechazo
por saldo insuficiente requerido por HU11, escenario 2.1.

No se agregan herramientas, dependencias ni pruebas JavaScript. Las pruebas
automáticas usan Java, JUnit y Mockito, ya presentes en el proyecto.

## Pruebas Java

Desde la carpeta del repositorio, ejecutar las pruebas de saldo y pago:

```powershell
.\mvnw.cmd "-Dtest=PedidoPagoTest,SaldoCarritoTest" test
```

Se esperan siete casos exitosos. Verifican el saldo actual recibido por la vista,
el rechazo del pago sin saldo, el pago con saldo exacto o suficiente y que no
se repita el débito al confirmar dos veces. No ejecutan el navegador ni prueban
automáticamente el atributo `disabled` del botón.

Para buscar regresiones en los demás casos Java, ejecutar toda la suite:

```powershell
.\mvnw.cmd test
```

## Comprobación visual

Reiniciar Tomcat y recargar el carrito. Utilizar datos de prueba: confirmar un
pago exitoso sí descuenta saldo y stock.

1. Preparar un pedido cuyo total supere el saldo. Deben aparecer el aviso y el
   saldo proyectado negativo; el botón de pago inmediato debe estar bloqueado.
2. Seleccionar «Programar para llevar»: el botón debe habilitarse. Elegir una
   franja disponible y confirmar; el servidor debe rechazar por saldo insuficiente
   sin descontar saldo o stock ni ocupar un cupo.
3. Volver a «Pagar ahora»: el botón debe bloquearse otra vez.
4. Quitar productos hasta que el total sea igual o menor al saldo. El pago
   inmediato debe habilitarse al recargar el carrito.
5. Abrir la confirmación y pulsar «Cancelar»: no deben cambiar saldo ni stock.
6. Confirmar un pago permitido: debe aparecer el comprobante y un solo débito.

No se requieren migraciones de base de datos.
