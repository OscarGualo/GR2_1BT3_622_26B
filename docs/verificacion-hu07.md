# Verificación de HU07: pago con saldo insuficiente

El pago inmediato se deshabilita cuando el saldo mostrado no alcanza. En el modo
«Programar para llevar» se permite enviar la solicitud para que el servidor
muestre el rechazo requerido por HU11, escenario 2.1. Las validaciones del
servidor siguen siendo obligatorias en ambos modos.

## Pruebas automáticas

Desde la carpeta del repositorio, ejecutar las seis pruebas del JavaScript real
del carrito (requiere Node.js 18 o posterior, sin paquetes adicionales):

```powershell
node --test src/test/js/carrito.test.cjs
```

Estas pruebas simulan los controles del formulario; no compilan la JSP ni
sustituyen la comprobación en navegador. Se ejecutan por separado de Maven.

Ejecutar también las pruebas Java de pagos, cupos, cancelaciones y reprogramación:

```powershell
.\mvnw.cmd test
```

## Comprobación en la aplicación

Reiniciar o volver a desplegar la aplicación en Tomcat y recargar el carrito.
Usar datos de prueba; los pagos exitosos sí descuentan saldo y stock.

| Caso | Acción | Resultado esperado |
| --- | --- | --- |
| HU07, escenario 2.1 | Preparar un pedido cuyo total supere el saldo y abrir el carrito. | Aparecen el aviso y el saldo proyectado negativo. El botón de pago inmediato está deshabilitado. |
| Cambio de modo | Con saldo insuficiente, seleccionar «Programar para llevar» y después volver a «Pagar ahora». | El botón se habilita al programar y vuelve a bloquearse en pago inmediato. |
| HU11, escenario 2.1 | Con saldo insuficiente, elegir una franja disponible y confirmar «Programar y pagar». | El servidor rechaza por saldo insuficiente. No cambia el saldo, el stock ni la ocupación de la franja. |
| Saldo suficiente o exacto | Preparar un pedido que se pueda pagar y confirmar el pago inmediato. | Se permite confirmar, se realiza un solo cobro y aparece el comprobante. |
| Cancelar confirmación | Abrir la confirmación de un pago permitido y pulsar «Cancelar». | No se envía el pago ni cambian saldo y stock. |
| Rechazo en servidor | Enviar una solicitud de pago inmediato sin saldo mediante una prueba de controlador o un cliente HTTP autenticado. | El servidor rechaza el pago aunque se eluda el bloqueo del botón. |

La corrección no cambia tablas ni requiere migraciones de base de datos.
