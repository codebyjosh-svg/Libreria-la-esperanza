# Librería La Esperanza

Sistema de gestión para librería desarrollado con **JavaFX** y **MySQL**.

**Autor: mi nombre**

## Funcionalidades implementadas (US-1.3 Navegación por rol)

1. **Permisos por rol** (`org.esperanza.Model.Rol`)
   - ADMIN: gestión usuarios, reportes, inventario, ventas, configuración
   - CAJERO: ventas, consultar productos
   - BODEGA: gestión inventario, entradas/salidas, consultar productos

2. **Sesión de usuario actual** (`org.esperanza.Service.SesionUsuario`)
   - Singleton que mantiene el usuario autenticado y su rol durante la sesión.

3. **Dashboard Cajero** (`DashboardCajero.fxml` + controller)

4. **Dashboard Bodega** (`DashboardBodega.fxml` + controller)

5. **Dashboard Admin** (`DashboardAdmin.fxml` + controller)
   - Acceso a gestión de usuarios.

Tras el login exitoso se redirige automáticamente al dashboard correspondiente según el rol del usuario.

## Estructura principal

```
src/org/esperanza/
├── Controller/     # Login, Dashboards, Usuarios
├── Model/          # Usuario, Rol
├── Service/        # SesionUsuario
├── Dao/            # UsuarioDao, Conexion
├── Util/           # PasswordUtil
├── system/         # main
└── view/           # FXML + CSS
```

## Requisitos

- JDK 11+
- JavaFX
- MySQL con los procedimientos almacenados de login y tabla `usuarios`

## Ejecución

Abrir el proyecto en NetBeans o compilar con el `build.xml` de Ant.
