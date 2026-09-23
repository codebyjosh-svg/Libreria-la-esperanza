# US-4.2.3 — Descuentos en ventas

## Reglas

- El descuento se aplica al **subtotal de toda la venta**. Se puede elegir un porcentaje de 0 a 100 % o un monto fijo de Q0.00 hasta el subtotal, con máximo dos decimales.
- Un porcentaje se convierte a quetzales y se redondea a dos decimales. El total es `subtotal - descuento` y nunca es negativo.
- Solo una sesión de **administrador** puede aplicar un descuento mayor que cero. Antes de guardar, el DAO también verifica que ese mismo usuario siga activo y tenga rol de administrador en `usuarios`. El cajero puede seguir registrando ventas sin descuento.
- Si el descuento no es válido o no está autorizado, la venta no se guarda y el carrito conserva sus productos.
- La tabla `ventas` del proyecto ya contiene `descuento`; se almacena el **monto aplicado** en esa columna, junto con `subtotal` y `total`. No hace falta cambiar el esquema para esta tarea.

| Tarea | Archivo o comportamiento |
| --- | --- |
| T4.2.3.1 Definir reglas | Este documento y `DescuentoVenta.java`. |
| T4.2.3.2 Agregar campo | Selector y valor de descuento en `CarritoVenta.fxml`; columna `ventas.descuento` ya existente. |
| T4.2.3.3 Cálculo | `DescuentoVenta.calcularMonto`, también usado por `VentaDao`. |
| T4.2.3.4 Nuevo total | Vista previa en el carrito y cálculo definitivo en `VentaDao`. |
| T4.2.3.5 Validación | Límites del porcentaje, monto, decimales y subtotal. |
| T4.2.3.6 Autorización | Controles deshabilitados para cajero; verificación de sesión y base en `VentaDao`. |
| T4.2.3.7 Comprobante | `TicketVentaController` muestra el descuento aplicado; al reabrir una venta se lee el importe guardado. |
| T4.2.3.8 Pruebas | `test/PruebaDescuentos.java`: cálculo, redondeo, persistencia simulada y rechazo por rol. |

## Ejemplo para comprobar en NetBeans

Con dos libros que sumen Q100.00, un administrador puede elegir `Porcentaje (%)` y escribir `10`: debe verse subtotal Q100.00, descuento Q10.00 y total Q90.00 en el carrito y en el comprobante. Con `Monto fijo (Q)` y `15`, el total debe ser Q85.00. Ingresa como cajero para comprobar que el selector quede bloqueado.

Desde NetBeans puedes ejecutar `PruebaDescuentos.java` y `PruebaVentas.java`. Para una venta real se necesita MySQL configurado y el conector JDBC del proyecto.
