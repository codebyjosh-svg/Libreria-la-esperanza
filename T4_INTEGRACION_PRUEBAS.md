# Integración: autenticación, venta y excepciones JDBC

Todas las pruebas Java están en `test/org/esperanza/test/` con el paquete
`org.esperanza.test`. En NetBeans, abre el proyecto con JDK 21 y ejecuta
**Run File** sobre cada prueba indicada. No requieren JUnit ni MySQL para las
pruebas automáticas siguientes.

Para iniciar la aplicación con tu base, copia `src/db.properties.example` a
`src/db.properties` y completa tus datos locales. El archivo privado
`src/db.properties` se excluye del ZIP y está ignorado por Git.

| Tarea | Prueba y resultado esperado |
| --- | --- |
| T4.I.3 Autenticación | `PruebaAutenticacion`: campos vacíos, contraseña hasheada, usuario activo/inactivo, usuario ausente, contraseña errónea y fallos SQL. |
| T4.I.7 Venta → stock | `PruebaVentaStock`: descuento de unidades al confirmar, rechazo por stock insuficiente y recuperación del stock tras un fallo o error de `commit`. |
| T4.I.11 Excepciones JDBC | `PruebaExcepcionesJdbc`: las consultas fallidas propagan `SQLException` y cierran conexión, procedimiento y resultado; `PruebaVentaStock` también comprueba el `rollback` y sus excepciones suprimidas. |

Las pruebas anteriores junto con `PruebaVentas`, `PruebaDescuentos` y
`PruebaIndicadoresDashboard` se compilan y ejecutan desde su método `main`.
`PruebaStock` es una comprobación **manual** que requiere una base MySQL
configurada y un ISBN existente. `PruebaTicket` y `PruebaComprobante` abren
ventanas JavaFX y se ejecutan con interfaz gráfica.

Cuando una consulta de login falla, la aplicación muestra un error de base de
datos y registra la excepción; ya no muestra «contraseña incorrecta» en ese
caso. Los procedimientos `sp_iniciar_sesion` y `sp_buscar_usuario` deben
devolver la columna `activo` para impedir accesos sin verificar el estado.
La venta mantiene la inserción y el cambio de stock dentro de una misma
transacción JDBC; la conexión se cierra después del `commit` o del `rollback`.

Estas pruebas automáticas simulan JDBC y transacciones. Para comprobar la
integración con tu instalación MySQL, crea una venta de prueba con un ISBN de
stock conocido, verifica que baje exactamente la cantidad vendida y prueba
una venta con stock insuficiente para confirmar que no cambia el inventario.
