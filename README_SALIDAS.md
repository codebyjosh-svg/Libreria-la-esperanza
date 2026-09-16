# US-3.2 — Salidas de inventario

Desde el dashboard BODEGA, pulsa **Registrar salida**. Ingresa el ISBN, consulta el stock,
selecciona Merma, Traslado o Devolución a proveedor, escribe una cantidad entera positiva
y un motivo de hasta 500 caracteres. Para traslados indica el destino; para devoluciones,
identifica al proveedor en el motivo. El traslado descuenta de esta bodega; no crea una
entrada automática en otra bodega.

## Preparación

1. En tu base MySQL ejecuta `sql/US-3.2-salida-inventario.sql`.
2. Verifica que `libros` use InnoDB. El módulo rechaza motores sin soporte transaccional.
3. Configura `src/db.properties` con tu base, usuario y contraseña.
4. En NetBeans, usa JDK 21, JavaFX 21 y MySQL Connector/J; ejecuta **Clean and Build**.
5. Inicia sesión con un usuario BODEGA.

El script agrega una tabla específica de movimientos de salida y no modifica datos existentes.
No se incluyeron claves foráneas porque el proyecto no aporta el DDL de libros y usuarios;
el ISBN se valida y bloquea contra libros antes del descuento, y el usuario procede de la sesión.

## Transacción

Se comprueba el stock con `SELECT ... FOR UPDATE`, se descuenta con un `UPDATE` condicionado
a existencias suficientes y se inserta el movimiento en la misma conexión. Solo entonces se
hace commit; cualquier fallo previo provoca rollback. Se conserva ISBN, tipo, cantidad,
motivo, usuario y fecha. La consulta mostrada en pantalla es informativa: guardar siempre
vuelve a validar el stock bajo bloqueo. El formulario se bloquea mientras guarda para evitar
dobles clics y el trabajo SQL se realiza fuera del hilo de interfaz.

## Tareas implementadas

| ID | Implementación | Responsable | Horas |
|---|---|---|---|
| T3.2.1 | TipoSalida y SalidaInventario | D1 | 1 |
| T3.2.2 | StockDao.obtenerStockActual, botón Consultar stock | D1 | 1 |
| T3.2.3 | SalidaInventario.fxml y acceso desde bodega | D4 | 1 |
| T3.2.4 | Selector de los tres tipos | D4 | 1 |
| T3.2.5 | Campo de cantidad entera positiva | D4 | 1 |
| T3.2.6 | Validación de existencias bajo bloqueo | D1 | 1 |
| T3.2.7 | StockDao.descontarStock | D1 | 1 |
| T3.2.8 | movimientos_salida_inventario | D1 | 1 |
| T3.2.9 | Commit y rollback en SalidaInventarioDao | D1 | 1 |
| T3.2.10 | Motivo obligatorio y persistido | D4 | 1 |
| T3.2.11 | Prueba automatizada de merma | D2 | 1 |
| T3.2.12 | Prueba automatizada de devolución | D2 | 1 |
| T3.2.13 | Prueba automatizada de exceso de stock | D2 | 1 |

## Pruebas sin base de datos

Desde la raíz del proyecto (PowerShell), con JDK 21 en PATH:

```powershell
New-Item -ItemType Directory -Force build/pruebas-salidas | Out-Null
javac -encoding UTF-8 -d build/pruebas-salidas src/org/esperanza/Model/TipoSalida.java src/org/esperanza/Model/SalidaInventario.java src/org/esperanza/util/Conexion.java src/org/esperanza/dao/ProveedorConexion.java src/org/esperanza/dao/StockDao.java src/org/esperanza/dao/SalidaInventarioDao.java test/PruebaSalidaInventario.java
java -cp build/pruebas-salidas PruebaSalidaInventario
```

Estas pruebas usan un doble JDBC para verificar flujo transaccional, rollback, merma,
traslado, devolución, exceso, stock exacto, libro ausente/inactivo, motor y campos inválidos.
No sustituyen pruebas de integración en MySQL.

## Verificación manual en una base de pruebas

Con un libro activo que tenga 10 unidades:

1. Merma de 2: quedan 8 y se crea un movimiento MERMA con motivo y usuario.
2. Devolución de 3: quedan 5 y se crea un movimiento DEVOLUCION_PROVEEDOR.
3. Solicitar 6: se rechaza; quedan 5 y no se agrega movimiento.
4. Traslado de 5: queda 0 y se crea un movimiento TRASLADO.
5. En dos sesiones, intentar consumir simultáneamente el mismo stock: solo puede
   confirmarse lo que alcance; nunca debe quedar negativo.
6. Con un usuario SQL de pruebas sin permiso INSERT en la tabla de movimientos,
   intentar una salida: el descuento debe revertirse.

Comprueba los movimientos con:

```sql
SELECT * FROM movimientos_salida_inventario ORDER BY id DESC;
```
