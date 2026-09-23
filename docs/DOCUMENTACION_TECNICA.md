# Documentación técnica — Sprint 4

## Alcance y estructura

Aplicación Java 21, interfaz JavaFX, FXML, JDBC y MySQL. Los paquetes de los módulos nuevos son `org.esperanza.model`, `org.esperanza.dao` y `org.esperanza.controller`. Se respeta la estructura de directorios del ZIP, que contiene mayúsculas históricas en algunos paquetes anteriores.

| Componente | Responsabilidad |
| --- | --- |
| `model/Proveedor.java` | Modelo inmutable, normalización y validación de campos |
| `dao/ProveedorDAO.java` | Alta, edición, búsqueda, baja lógica y reactivación; autorización previa a JDBC |
| `controller/ProveedoresController.java` + `view/Proveedores.fxml` | Tabla, formulario, estados de carga, confirmación y mensajes |
| `model/EstadoVenta.java` | COMPLETADA, DEVUELTA, ANULADA y DESCONOCIDA para datos legados |
| `model/Venta.java` y `dao/VentaDao.java` | Lectura y exposición del estado real de cada venta |
| `dao/DevolucionVentaDao.java` | Consulta reciente y devolución atómica, con auditoría y control de duplicados |
| `controller/DevolucionesController.java` + `view/Devoluciones.fxml` | Selección de venta, detalle, motivo y confirmación |
| `model/Rol.java` + `service/SesionUsuario.java` | Permisos y validación de la sesión activa |
| `dao/UsuarioDao.java` | Guardas administrativas y restricción de cambio de contraseña al ID propio |

Los nuevos controladores ejecutan JDBC en `Task`, fuera del hilo JavaFX. Durante la operación deshabilitan el formulario. Los dashboards impiden cerrar la ventana mientras `estaOcupado()` es verdadero; la ventana modal evita cambiar de sesión a mitad del trabajo desde el dashboard.

## Proveedores

`proveedores`: `id INT AUTO_INCREMENT`, `nit VARCHAR(25) UNIQUE`, `nombre VARCHAR(150)`, `contacto VARCHAR(100)`, `telefono VARCHAR(25)`, `correo VARCHAR(150)`, `direccion VARCHAR(250)`, `activo`, `creado_en`, `actualizado_en`.

NIT, nombre y teléfono son obligatorios. El NIT se convierte a mayúsculas y se quitan espacios y guiones; su validación es de formato y unicidad local, sin validación fiscal externa. Correo vacío es válido; si se proporciona se valida su formato. Teléfono acepta formato internacional habitual y exige suficientes dígitos. Todos los textos tienen límites consistentes con la tabla.

El borrado es lógico (`activo = FALSE`), con reactivación posterior. Evita perder el identificador usado en otros registros. La migración no crea claves foráneas a libros porque el esquema original no se suministró; el formulario de libros conserva su campo de proveedor existente. Si la base ya tiene una tabla `proveedores`, se requiere comparar tipos y nombres antes de usar esta migración: el script crea la tabla si no existe, no transforma esquemas previos distintos.

API principal:

```java
new ProveedorDAO(proveedorConexion);
int crear(Proveedor proveedor);
void actualizar(Proveedor proveedor);
void cambiarEstado(int id, boolean activo);
List<Proveedor> listar(String filtro, boolean incluirInactivos);
Optional<Proveedor> buscarPorId(int id);
```

Las consultas usan parámetros JDBC y el DAO comprueba `GESTION_PROVEEDORES` en cada lectura/escritura. El índice único de NIT protege también contra altas simultáneas.

## Devoluciones

Transición permitida: `COMPLETADA → DEVUELTA`. `ANULADA`, `DEVUELTA` y los estados desconocidos no son elegibles. Las ventas existentes se interpretan por su valor real; `DESCONOCIDA` no se escribe como nuevo estado. No se implementan devoluciones parciales ni un sistema de reembolso de efectivo.

La pantalla consulta los últimos 30 días, incluidos los estados ya devueltos; el filtro por ID se aplica dentro del mismo período. El límite de 30 días es de consulta, no una política de autorización aplicada por el DAO a ventas antiguas.

`sql/T4_2_5_devoluciones.sql` cambia `ventas.estado` a `VARCHAR(30) NOT NULL DEFAULT 'COMPLETADA'`, conservando valores existentes que cumplan ese tamaño. Antes de ejecutarlo deben comprobarse estados nulos o de más de 30 caracteres, y que las cinco tablas relevantes usen InnoDB. MySQL hace commit implícito en operaciones DDL; la migración se aplica durante mantenimiento, con respaldo.

Tablas nuevas:

- `devoluciones_venta`: `id_venta` clave primaria, motivo de 5–500 caracteres, ID de usuario y fecha.
- `movimientos_devolucion_venta`: ID, venta, ISBN, cantidad y fecha; combinación `(id_venta,isbn)` única y FK hacia la auditoría de devolución.

Se usan tablas específicas para devolución de ventas para no alterar el ENUM de movimientos del inventario existente. No se asumen tipos de claves foráneas de las tablas base ausentes del ZIP. El motivo y el usuario se consultan mediante la cabecera de devolución.

Secuencia de `devolver(int idVenta, String motivo)`:

1. Valida sesión, permiso, ID positivo y motivo; la identidad procede de la sesión, nunca de un ID enviado por el formulario.
2. Verifica que las cinco tablas existen y usan InnoDB; abre una transacción.
3. Bloquea la venta con `SELECT ... FOR UPDATE` y exige COMPLETADA.
4. Bloquea sus detalles; agrupa cantidades por ISBN y rechaza detalle vacío, cantidades inválidas y desbordamientos.
5. Inserta auditoría, cuya PK impide repetir la misma venta incluso si alguien cambió su estado fuera del sistema.
6. Restaura stock en orden estable de ISBN, incluidos libros desactivados; comprueba filas afectadas y límites de enteros.
7. Registra un movimiento por ISBN; actualiza el estado con condición `estado = 'COMPLETADA'`.
8. Ejecuta `commit`; cualquier fallo produce `rollback`. Nunca se restaura stock en una conexión separada.

Dos conexiones que soliciten la misma devolución compiten por la fila bloqueada: después de confirmar la primera, la segunda encuentra DEVUELTA y se rechaza. El resumen diario existente ya filtra COMPLETADA, por lo que las ventas devueltas dejan de contarse allí; no se cambian los importes históricos de la venta.

## Permisos

| Acción | ADMIN | CAJERO | BODEGA |
| --- | --- | --- | --- |
| Gestión de usuarios | Sí | No | No |
| Gestión de proveedores | Sí | No | No |
| Ventas y devolución de ventas | Sí | Sí | No |
| Gestión de inventario / entradas / salidas | Sí | No | Sí |
| Consultar productos | Sí | Sí | Sí |
| Cambiar contraseña propia | Sí | Sí | Sí |

La sesión exige usuario activo, ID positivo y coherencia entre rol actual y rol del usuario. Un intento inválido de iniciar sesión elimina una sesión anterior. Se normalizan alias de rol existentes. Los formularios de administración, libros, inventario, ingresos, ventas y ventas del día comprueban permisos; los DAOs nuevos y `UsuarioDao` añaden comprobaciones antes de abrir conexiones.

`UsuarioDao` impide que un administrador desactive su propia cuenta y que cualquier rol modifique contraseñas de IDs ajenos. Los procedimientos de autenticación deben devolver `activo`; una columna ausente ya no se interpreta como una cuenta habilitada.

Límite de arquitectura: los DAOs heredados de ventas/inventario continúan siendo componentes internos que pueden invocarse desde Java; no se rediseñó toda la aplicación como servidor con autorización centralizada. Las credenciales JDBC tampoco equivalen a permisos por usuario de la aplicación. Las pruebas de permisos documentadas cubren la matriz, los formularios revisados, usuarios y los módulos nuevos.

## Configuración y entrega

`Conexion` admite variables `ESPERANZA_DB_URL`, `ESPERANZA_DB_USER`, `ESPERANZA_DB_PASSWORD` con prioridad sobre `/db.properties`. Se entrega un ejemplo sin credenciales reales. El JAR generado no contiene configuración privada. Las dependencias JavaFX/MySQL no se redistribuyen.

El proyecto Ant/NetBeans original sigue disponible; el script PowerShell permite compilar sin las bibliotecas locales de NetBeans. Para instalar sobre una base existente solo se aplican las dos migraciones Sprint 4; `test/fixtures/esquema_minimo.sql` está separado por ser un esquema sintético de pruebas.

El informe `INTEGRACION_GIT.md` registra la excepción de una rama independiente no portada. No se generaron commits ficticios, no se contactó el remoto y no se afirma que la base personal del usuario haya sido migrada.
