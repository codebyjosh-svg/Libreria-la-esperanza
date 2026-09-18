# Integracion y auditoria del historial incluido

Auditoria local del ZIP recibido, realizada el 18 de septiembre de 2026. No se contacto el remoto, no se publicaron cambios y no se hizo `fetch`. Las referencias reflejan exclusivamente el historial incluido en ese ZIP.

## Base de trabajo

La rama local `develop` y `origin/develop` apuntaban a `4d4c5c7` (PR #16, limpieza de estructura). La version ya incorpora los merges de ventas, usuarios, libros, entradas, salidas, stock critico y dashboard de bodega. Los cambios de esta entrega se aplican sobre esa base en el arbol de trabajo.

## Ramas ya integradas

`git branch -r --merged develop` confirma por ascendencia las siguientes referencias. Los hashes son los del ZIP original:

| Referencia origin/ | Commit | Resultado |
| --- | --- | --- |
| main | 2892968 | Ancestro de develop |
| fix/integracion-sprint2 | bc42bb6 | Ancestro de develop |
| fix/integracion-sprint2-final | 1636560 | Ancestro de develop |
| fix/integracion-sprint3 | 5cc85e1 | Ancestro de develop |
| fix/limpieza-estructura-develop | 13baeb5 | Ancestro de develop |
| fix/normalizar-paquetes | 161b70a | Ancestro de develop |
| fix/sprint2-retroalimentacion | 903be06 | Ancestro de develop |
| ft/US-1.1-login | 6f5c405 | Ancestro de develop |
| ft/US-1.1-navegacion-dashboard | 67926d8 | Ancestro de develop |
| ft/US-1.2-gestion-usuarios | b6b7d2d | Ancestro de develop |
| ft/US-1.3-navegacion-por-rol | ead07e6 | Ancestro de develop |
| ft/US-1.3-navegacion-por-rol-pr | 15a96dc | Ancestro de develop |
| ft/US-1.4-cambio-contrasena | e83d734 | Ancestro de develop |
| ft/US-2.1-buscarlibros | 8915d26 | Ancestro de develop |
| ft/US-2.2-registrar-venta | c3b1889 | Ancestro de develop |
| ft/US-2.3-ventas-del-dia | fe85e5a | Ancestro de develop |
| ft/US-3.1-ingreso-inventario | a69f7ac | Ancestro de develop |
| ft/US-3.2-salida-inventario | 290c70f | Ancestro de develop |
| ft/US-3.3-alertas-stock-bajo | 28c714f | Ancestro de develop |
| ft/US-3.4-gestion-libros | 196211e | Ancestro de develop |
| ft/US-3.5-dashboard-bodega | d9432e7 | Ancestro de develop |
| integracion/US-3.5 | 0d47795 | Ancestro de develop |

`origin/develop` coincide con la base y `origin/HEAD` es una referencia simbolica a `origin/main`; no representan trabajo adicional pendiente.

## Excepcion: prototipo con historial independiente

La unica referencia que aparece en `git branch -r --no-merged develop` es `origin/ft/US-1.3-navegacion-por-rol1` (`5916a7c`). Sus dos commits (`ccbdc50` y `5916a7c`) tienen una raiz independiente: `git merge-base develop origin/ft/US-1.3-navegacion-por-rol1` no encuentra ancestro comun.

`git cherry develop origin/ft/US-1.3-navegacion-por-rol1` marca ambos commits con `+`: no hay equivalencia exacta de parches que permita afirmar que fueron cherry-picked. La navegacion y los permisos de sus tres roles estan representados funcionalmente en la rama `ft/US-1.3-navegacion-por-rol-pr`, integrada mediante `15a96dc`, y recibieron cambios posteriores.

El prototipo contiene ademas `AuditoriaDao`, `ReportesController` y `ConfiguracionController`, ausentes en la base actual. Los dos primeros implementan registro de tiempo y actividad; eso no equivale al reporte de ventas del dia actual. Esta funcionalidad adicional **no se ha portado en esta entrega**. La configuracion antigua modifica la contrasena sin pedir la actual y depende de paquetes y metodos que ya cambiaron; actualmente existe un flujo de cambio de contrasena con comprobacion de la clave actual.

No se fusiono ese historial con `--allow-unrelated-histories`: reincorporaria estructura, configuracion y APIs incompatibles. Para incluir el reporte de actividad, se requiere un port delimitado con un esquema de auditoria, llamadas de registro y pruebas propias. Por ello no se afirma que todas las referencias Git esten fusionadas ni que se haya completado una publicacion en el remoto.

## Resolucion de conflictos aplicada

- Se conservan la estructura y los DAOs de `develop`, incluidos sus contratos JDBC y pruebas existentes.
- Los modulos nuevos se incorporan a esa base y se conectan a la sesion y los dashboards existentes.
- La autorizacion de gestion de usuarios se verifica de nuevo en el DAO y al guardar/listar en los formularios. Un formulario abierto antes de cerrar sesion no conserva facultades administrativas.
- Se usa el rol normalizado de sesion; `ADMINISTRADOR` y `BODEGUERO` no quedan bloqueados por comparaciones literales con `ADMIN` o `BODEGA`.
- El ID recibido para cambiar contrasenas debe coincidir con el usuario activo. El administrador no puede desactivar su propia cuenta.

Los conflictos aqui resueltos son de integracion del codigo y sus contratos. No hubo un merge Git nuevo ni se generaron commits artificiales para simular una fusion.

## Esquema y pruebas

El ZIP solo contiene dos scripts SQL parciales en `sql/`. En el historial independiente hay `usuarios_cris.sql` y `usuarios_libreria_la_esperanza.sql`, que tampoco constituyen una base completa: definen usuarios y, en uno de los casos, auditoria. Ademas usan la columna `password`, mientras el DAO actual de cambio de contrasena usa `password_hash`. No se reutilizaron sus datos ni sus credenciales de demostracion.

`test/fixtures/esquema_minimo.sql` reconstruye contratos minimos a partir de los DAOs para ejecutar pruebas en una base vacia y desechable. Incluye tablas InnoDB y procedimientos de usuarios/libros. No es una migracion para una base existente ni demuestra que la base del usuario tenga ese esquema. Las migraciones de proveedores y devoluciones se aplican despues del fixture.

`test/PruebaAdministracion.java` se compilo y ejecuto con Java 21. Verifica denegacion antes de abrir conexiones, roles, sesiones cerradas e inactivas, datos invalidos, alta, duplicados, listado, activacion/desactivacion y cambio de contrasena propia. El JDBC de esta prueba es simulado; las pruebas reales de base y las pruebas de interfaz deben identificarse por separado en la documentacion de entrega.

## Comandos reproducibles sobre el repositorio original

```console
git status --short
git branch -avv
git branch -r --merged develop
git branch -r --no-merged develop
git log --oneline develop..origin/ft/US-1.3-navegacion-por-rol1
git cherry develop origin/ft/US-1.3-navegacion-por-rol1
git merge-base develop origin/ft/US-1.3-navegacion-por-rol1
git log --all --oneline -- '*.sql'
```

Si la entrega se distribuye sin `.git`, estos comandos requieren el historial del ZIP original. No es posible verificar ramas nuevas o cambios del equipo posteriores al archivo recibido sin acceso actualizado al remoto.
