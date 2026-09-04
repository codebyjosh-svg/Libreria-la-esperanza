# Gestión de usuarios - Librería La Esperanza

Se agregó un módulo JavaFX para gestión de usuarios con:

- DAO de usuario con listado, alta, desactivación lógica y validación de username.
- Vista JavaFX de listado (`Usuarios.fxml`).
- Vista JavaFX de alta (`UsuarioAlta.fxml`).
- Validaciones de campos, correo, nombres, usuario y contraseñas.
- Confirmación antes de desactivar.
- Estado Activo/Inactivo en el listado.
- Recarga del listado después de alta o desactivación.
- La aplicación inicia directamente, sin pantalla de login.
- Se selecciona el usuario de trabajo desde el panel principal.
- Se registra el tiempo de uso y los cambios realizados por usuario.

## Base de datos esperada

La tabla `usuarios` debe tener, como mínimo, las columnas:

`id, username, password, rol, nombre, apellido, correo, activo`

El alta conserva el procedimiento existente:

`sp_registrar_usuario(username, passwordHash, rol, nombre, apellido, correo)`

El listado y la desactivación usan SQL parametrizado directamente sobre `usuarios`:

- `SELECT ... FROM usuarios ORDER BY id`
- `UPDATE usuarios SET activo = 0 WHERE id = ?`

La contraseña se almacena mediante SHA-256 usando la clase `PasswordUtil` que ya tenía el proyecto.

## Importante

El archivo `src/db.properties` no se incluye por seguridad. Debe existir en el classpath con:

```properties
db.url=jdbc:mysql://localhost:3306/TU_BASE_DE_DATOS
db.user=TU_USUARIO
db.password=TU_PASSWORD
```

Si tu tabla usa nombres de columnas diferentes, ajusta únicamente las consultas de `UsuarioDao` y el procedimiento `sp_registrar_usuario` para que coincidan con tu base de datos.
