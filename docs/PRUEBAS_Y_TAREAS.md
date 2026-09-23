# Pruebas y trazabilidad de tareas

Verificación realizada el 18/09/2026. Las 29 horas son la suma de tus estimaciones, no un registro de tiempo real empleado. T4.I.18 no tenía horas asignadas.

| ID | h previstas | Entrega / evidencia | Estado |
| --- | ---: | --- | --- |
| T4.4.6 | 1 | `sql/T4_4_proveedores.sql` aplicada en MySQL temporal | Implementado y probado |
| T4.4.7 | 1 | Modelo `Proveedor` con normalización y límites | Implementado y probado |
| T4.4.8 | 2 | `ProveedorDAO`, parámetros JDBC y control de permisos | Implementado y probado |
| T4.4.9 | 1 | `Proveedores.fxml`, tabla y formulario; captura JavaFX | Implementado y verificado |
| T4.4.10 | 2 | Alta, consulta, edición, baja lógica y reactivación | Probado con MySQL real |
| T4.4.11 | 1 | Campos, NIT único, correo, teléfono y límites | Pruebas automatizadas |
| T4.2.5.1 | 1 | `EstadoVenta` y transición COMPLETADA → DEVUELTA | Implementado y probado |
| T4.2.5.2 | 1 | Migración de estado y tablas de auditoría/movimientos | Aplicada en MySQL temporal |
| T4.2.5.3 | 1 | Consulta de 30 días y filtro por ID | Probada con MySQL real |
| T4.2.5.4 | 2 | `Devoluciones.fxml`, controles y estados de carga | Carga y revisión visual |
| T4.2.5.5 | 1 | Selección de venta y carga de detalles | Implementado |
| T4.2.5.6 | 1 | Motivo obligatorio 5–500 caracteres y confirmación | Validación automatizada |
| T4.2.5.7 | 1 | Actualización condicional de estado en transacción | Probada con MySQL real |
| T4.2.5.8 | 2 | Stock por ISBN, incluidos libros inactivos | Probado con MySQL real |
| T4.2.5.9 | 1 | Auditoría y movimiento por ISBN | Probados con MySQL real |
| T4.2.5.10 | 1 | Bloqueo de venta, PK/UNIQUE y rechazo de repetición | Concurrencia real aprobada |
| T4.2.5.11 | 2 | Pruebas simuladas y de integración | Aprobadas |
| T4.I.1 | 2 | Código nuevo integrado con develop; historial auditado | Parcial: rama independiente no portada |
| T4.I.2 | 2 | Contratos compatibles y guía de conflictos Git | Documentado; sin coordinación con equipo/remoto |
| T4.I.6 | 1 | `PruebaAdministracion` y carga de pantallas administrativas | Aprobadas |
| T4.I.10 | 2 | Matriz de tres roles, guardas DAO y bloqueo FXML para BODEGA | Aprobadas en alcance descrito |
| T4.I.18 | — | README, documentación técnica, pruebas y auditoría Git | Entregado |
| Total conocido | 29 | | |

## Resultados ejecutados

- Compilación completa: **72 fuentes Java**, incluidos código de aplicación y pruebas, con JDK 21.0.11.
- `PruebaStockCritico`: pasó; comparación de stock normal, mínimo, inferior e inactivo.
- `PruebaSalidaInventario`: pasó; tipos de salida, exceso, rollback y datos inválidos.
- `PruebaVentas`: pasó; venta correcta, insuficiencia de stock, venta vacía y rollback.
- `PruebaAdministracion`: pasó; matriz de roles, denegación antes de JDBC, datos inválidos, alta/listado/duplicado, activación/desactivación y contraseña propia. También sesión inválida que elimina privilegios previos y rol cambiado.
- `PruebaProveedores`: pasó; CRUD, unicidad incluso tras baja, normalización, búsqueda, validaciones, cierre de recursos y roles.
- `PruebaDevoluciones`: pasó; cinco puntos de fallo transaccional, datos inválidos, falta de tablas/motores, doble devolución, permisos y concurrencia simulada.
- `PruebaIntegracionMySql`: pasó contra MySQL **8.0.46**, Connector/J **8.0.33**, instancia aislada en puerto 13317, base sintética `esperanza_test`. Validó devolución multilibro, ISBN repetido, libro inactivo, auditoría, filtro temporal, rollback provocado por trigger, reintento y dos conexiones concurrentes. También CRUD/duplicados y permisos de proveedores. Las filas sintéticas se limpian al terminar.
- Migraciones SQL de proveedores y devoluciones aplicadas correctamente sobre `test/fixtures/esquema_minimo.sql` en esa instancia temporal.
- `PruebaVistas`: cargó mediante FXMLLoader **DashboardAdmin, Usuarios, UsuarioAlta, Proveedores, Devoluciones, DashboardCajero y DashboardBodega**; esperó las consultas asíncronas y generó capturas. Comprobó que BODEGA encuentra los formularios nuevos deshabilitados. Se revisaron visualmente proveedores y devoluciones: sin campos o botones recortados al tamaño previsto.
- Sintaxis PowerShell de los scripts de compilación/pruebas validada con el parser.

La compilación y las capturas dentro del entorno aislado usaron las clases de las dependencias cargadas desde un gestor de archivos del compilador en memoria, debido a un `AccessDeniedException` de la resolución de rutas JAR del sandbox. Esto no cambió fuentes ni lógica. JavaFX emitió aviso por cargar clases en el módulo sin nombre; la carga y capturas terminaron correctamente. El script entregado usa la ruta de módulos JavaFX estándar para ejecución local.

No se contabilizan como pruebas aprobadas las antiguas `PruebaStock` y `PruebaIngresoInventario`, que dependen de datos concretos no incluidos y capturan sus propios errores, ni las pruebas interactivas de impresión `PruebaTicket`/`PruebaComprobante`. El ZIP no incluía una base personal completa. No se probó impresión física ni reembolso de dinero.

## Repetir las pruebas sin base de datos

```powershell
.\scripts\build.ps1 -JavaFxLib 'C:\ruta\javafx-sdk-21\lib' -Test
```

## Repetir la integración MySQL

Utiliza una base **vacía, local y desechable**, llamada exactamente `esperanza_test`. La prueba rechaza otro catálogo o un host remoto. Debe usarse una instancia o cuenta de pruebas; el usuario necesita crear triggers, además de leer/escribir tablas. No se ejecuta automáticamente sobre la configuración personal.

En el cliente MySQL, conectado a esa base:

```sql
SOURCE C:/ruta/proyecto/test/fixtures/esquema_minimo.sql;
SOURCE C:/ruta/proyecto/sql/T4_4_proveedores.sql;
SOURCE C:/ruta/proyecto/sql/T4_2_5_devoluciones.sql;
```

Después, PowerShell:

```powershell
$env:ESPERANZA_TEST_URL='jdbc:mysql://127.0.0.1:3306/esperanza_test?useSSL=false&allowPublicKeyRetrieval=true'
$env:ESPERANZA_TEST_USER='tu_usuario_de_pruebas'
$env:ESPERANZA_TEST_PASSWORD='tu_clave_de_pruebas'
.\scripts\test-mysql.ps1 -JavaFxLib 'C:\ruta\javafx-sdk-21\lib' -MySqlJar 'C:\ruta\mysql-connector-j-8.0.33.jar' -Vistas
```

La opción `-Vistas` genera capturas en `build/sprint4/capturas`. La prueba de base inserta solamente registros sintéticos identificados con UUID y borra sus propias filas; no reinicia ni borra el esquema. No vuelvas a aplicar el fixture sobre tablas que ya existen.

## Comprobación de aceptación en la instalación real

1. Con ADMIN, dar de alta un proveedor; buscarlo, editarlo, desactivarlo y reactivarlo. Intentar repetir el NIT y verificar el mensaje.
2. Registrar una venta con varios libros, anotar stock e importe y devolverla con ADMIN o CAJERO. Confirmar estado, detalle, motivo y stock exacto.
3. Intentar devolver otra vez la misma venta y verificar que no cambia el stock.
4. Abrir la aplicación con BODEGA y comprobar que conserva inventario/entradas/salidas pero no accede a los módulos nuevos.
5. Revisar que el reporte diario refleja únicamente las ventas COMPLETADAS.
6. Verificar los procedimientos, columnas y relaciones de la base real frente a las migraciones; comprobar las credenciales locales y las rutas JavaFX/MySQL de NetBeans.

Estos pasos de instalación real y la eventual integración del prototipo Git independiente quedan separados de las pruebas automatizadas ya ejecutadas.
