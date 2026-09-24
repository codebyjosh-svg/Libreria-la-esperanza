# US-4.1 — Dashboard administrativo

El panel del administrador muestra tres indicadores:

- **Ventas totales:** suma del campo `total` de todas las ventas con estado `COMPLETADA`. Las ventas canceladas quedan fuera. Es un acumulado general, no solo del día.
- **Libros activos:** cantidad de registros de `libros` cuyo campo `activo` vale `1`. Se cuentan títulos, no ejemplares en stock.
- **Usuarios activos:** cantidad de registros de `usuarios` cuyo campo `activo` vale `1`.

| Tarea | Implementación |
| --- | --- |
| T4.1.1 Identificar indicadores | Definiciones anteriores. |
| T4.1.2 Consulta de ventas | Subconsulta de `SUM(total)` con `estado = 'COMPLETADA'` en `DashboardIndicadoresDao`. |
| T4.1.3 Consulta de libros | Subconsulta de `COUNT(*)` con `activo = 1` en `DashboardIndicadoresDao`. |
| T4.1.4 Consulta de usuarios | Subconsulta de `COUNT(*)` con `activo = 1` en `DashboardIndicadoresDao`. |
| T4.1.5 DAO | `DashboardIndicadoresDao` e `IndicadoresDashboardAdmin`. |
| T4.1.6 Diseño del panel | Sección de indicadores en `DashboardAdmin.fxml`. |
| T4.1.7 Tarjetas KPI | Tres tarjetas con estilos en `view/style/dashboard.css`. |
| T4.1.8 Integración | `DashboardAdminController` consulta y muestra los resultados. |
| T4.1.9 Actualización | Botón Actualizar y nueva carga al volver de los módulos de ventas y usuarios. |
| T4.1.10 Pruebas | `test/PruebaIndicadoresDashboard.java` y comprobación manual descrita abajo. |

## Cómo comprobarlo en NetBeans

1. Abre el proyecto con JDK 21 y las bibliotecas JavaFX y MySQL configuradas. Conecta la aplicación a tu base de datos usando el archivo `src/db.properties` del proyecto.
2. Ejecuta `PruebaIndicadoresDashboard.java`. Debe imprimir `OK - Indicadores Dashboard Admin`.
3. Ingresa como administrador. Compara cada tarjeta con la siguiente consulta en MySQL Workbench:

```sql
SELECT COALESCE(SUM(total), 0) AS ventas_totales
FROM ventas WHERE estado = 'COMPLETADA';

SELECT COUNT(*) AS cantidad_libros
FROM libros WHERE activo = 1;

SELECT COUNT(*) AS usuarios_activos
FROM usuarios WHERE activo = 1;
```

4. Registra una venta completada y vuelve al dashboard: el importe debe aumentar. Si desactivas un usuario o libro, pulsa **Actualizar** y verifica que baja el contador correspondiente.
5. Si la base no está disponible, las tarjetas mantienen el valor anterior (o muestran `—` al inicio) y se muestra un mensaje de error. Ninguna consulta fallida se representa como cero.
