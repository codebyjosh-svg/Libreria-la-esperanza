# Proyecto revisado para NetBeans 21

Esta entrega parte del último ZIP recibido del proyecto. `DashboardAdminController`
incluye un constructor público sin parámetros para `FXMLLoader` y una opción
de inyectar `DashboardIndicadoresDao` en pruebas. Ninguna clase de `src` depende
de `PruebaDashboardAdmin` ni de otras clases de `test`.

## Abrir y comprobar

1. Descomprime el ZIP en una carpeta **nueva**. Si lo extraes encima de una
   copia anterior, podrían quedar archivos Java adicionales con errores.
2. Abre esa carpeta como proyecto en NetBeans con JDK 21, JavaFX 21 y el
   conector JDBC de MySQL que usas normalmente.
3. Ejecuta **Clean and Build**. Después puedes ejecutar
   `PruebaIndicadoresDashboard` y `PruebaVentas` desde NetBeans.

La compilación con Ant y JDK 21 de esta copia terminó correctamente; también
se cargaron `Login.fxml` y `DashboardAdmin.fxml`. Las pruebas del DAO de
indicadores y del registro de ventas pasaron.

## Funciones pendientes en el ZIP de origen

Este proyecto no contiene `Libros.fxml`, `ReportesInventario.fxml` ni
`ActualizarPrecio.fxml`. Sus opciones no pueden abrir pantallas que no están
incluidas; el controlador administrativo ya informa si falta una vista. Las
cifras reales del dashboard dependen de una base MySQL con las tablas `ventas`,
`libros` y `usuarios` y de la configuración de `src/db.properties`.
