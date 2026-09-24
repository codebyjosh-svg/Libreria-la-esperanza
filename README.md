# Librería La Esperanza — entrega Sprint 4

Esta entrega implementa proveedores y devoluciones sobre el proyecto Java 21 / JavaFX / MySQL recibido. Conserva la arquitectura de modelos, DAO, controladores y FXML, así como los cambios locales que ya contenía el ZIP.

## Inicio rápido

1. Instala o usa JDK 21, JavaFX SDK 21 y MySQL Connector/J. Se verificó con Java 21.0.11, JavaFX 21.0.11, Connector/J 8.0.33 y MySQL 8.0.46.
2. En tu base **existente**, revisa el esquema y ejecuta `sql/T4_4_proveedores.sql` y después `sql/T4_2_5_devoluciones.sql`. No ejecutes el fixture de pruebas sobre tus datos. Las tablas de ventas, detalle_venta y libros deben usar InnoDB. Si ya existe `proveedores`, compara sus columnas con el script: `CREATE TABLE IF NOT EXISTS` no adapta una tabla anterior incompatible.
3. Copia `db.properties.example` a `src/db.properties` y configura tu conexión. No se distribuye tu contraseña. También puedes definir `ESPERANZA_DB_URL`, `ESPERANZA_DB_USER` y `ESPERANZA_DB_PASSWORD`; tienen prioridad sobre el archivo.
4. Desde PowerShell, en la carpeta del proyecto:

```powershell
.\scripts\build.ps1 -JavaFxLib 'C:\ruta\javafx-sdk-21\lib' -MySqlJar 'C:\ruta\mysql-connector-j-8.0.33.jar' -Test -Run
```

El script compila desde fuentes, ejecuta las seis suites sin base de datos y genera `dist/Libreria-la-esperanza.jar`. `-Run` inicia la aplicación. Se puede omitir `-Run` o `-Test`. También acepta `JAVAFX_HOME` y `MYSQL_CONNECTOR_JAR` como valores predeterminados. El JAR no incorpora credenciales ni dependencias externas.

NetBeans puede abrir el proyecto Ant original; hay que configurar sus bibliotecas JavaFX/MySQL y la ruta de módulos de acuerdo con tu equipo. El script anterior evita depender de los nombres de bibliotecas y rutas personales de `nbproject/project.properties`.

## Uso

**ADMIN → Proveedores:** crea con NIT, nombre y teléfono; contacto, correo y dirección son opcionales. Selecciona una fila para editar. Desactivar conserva el ID y el NIT; marca «Incluir inactivos» para reactivar. El NIT debe ser único incluso entre inactivos.

**ADMIN o CAJERO → Devoluciones:** consulta ventas de los últimos 30 días, opcionalmente por ID; selecciona una venta COMPLETADA, revisa sus productos, escribe un motivo de 5 a 500 caracteres y confirma. La devolución es total. Cambia el estado a DEVUELTA, restaura stock y conserva motivo, fecha y usuario. Una venta ya devuelta no se puede volver a devolver.

BODEGA no puede gestionar proveedores ni devoluciones de ventas; conserva entradas, salidas e inventario. «Devolución al proveedor» del módulo de salidas y «devolución de una venta» son operaciones distintas.

## Documentación

- `docs/DOCUMENTACION_TECNICA.md`: arquitectura, tablas, permisos, contratos y decisiones.
- `docs/PRUEBAS_Y_TAREAS.md`: correspondencia con tus IDs, resultados y pruebas reproducibles.
- `docs/INTEGRACION_GIT.md`: evidencia de ramas ya integradas y la rama antigua no fusionada.
- `docs/capturas/`: capturas de proveedores y devoluciones cargadas con JavaFX.

El ZIP original no incluía el esquema completo de la base. Las pruebas reales se realizaron con un esquema temporal reconstruido a partir de los contratos del código; tu base local aún debe comprobarse al instalar. El fixture `test/fixtures/esquema_minimo.sql` sirve exclusivamente para crear una base de pruebas vacía.

La integración de código está terminada sobre `develop`. No se publicaron commits ni PR. Una rama antigua con historial independiente y módulos de auditoría adicionales no fue fusionada: el informe Git explica los archivos incompatibles y el trabajo pendiente para portarla.
