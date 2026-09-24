package org.esperanza.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.esperanza.model.Rol;
import org.esperanza.model.Usuario;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.Conexion;
import org.esperanza.util.PasswordUtil;

public class UsuarioDao {

    private final ProveedorConexion conexiones;

    public UsuarioDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public UsuarioDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(
                conexiones,
                "conexiones"
        );
    }

    private boolean esAdministrador() {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

        Usuario actual =
                sesion.getUsuarioActual();

        return sesion.haySesionActiva()
                && actual != null
                && actual.isActivo()
                && sesion.esAdmin();
    }

    private boolean esUsuarioActual(
            int idUsuario) {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

        Usuario actual =
                sesion.getUsuarioActual();

        return idUsuario > 0
                && sesion.haySesionActiva()
                && actual != null
                && actual.isActivo()
                && actual.getId() == idUsuario;
    }

    public Usuario iniciarSesion(
            String username,
            String passwordHash)
            throws SQLException {

        String sql =
                "{call sp_iniciar_sesion(?, ?)}";

        try (
                Connection con =
                        conexiones.conectar();

                CallableStatement cs =
                        con.prepareCall(sql)
        ) {

            cs.setString(
                    1,
                    username
            );

            cs.setString(
                    2,
                    passwordHash
            );

            try (
                    ResultSet rs =
                            cs.executeQuery()
            ) {

                if (rs.next()) {

                    return mapearUsuario(
                            rs
                    );
                }

                return null;
            }
        }
    }

    public Usuario buscarPorUsername(
            String username)
            throws SQLException {

        String sql =
                "{call sp_buscar_usuario(?)}";

        try (
                Connection con =
                        conexiones.conectar();

                CallableStatement cs =
                        con.prepareCall(sql)
        ) {

            cs.setString(
                    1,
                    username
            );

            try (
                    ResultSet rs =
                            cs.executeQuery()
            ) {

                if (rs.next()) {

                    return mapearUsuario(
                            rs
                    );
                }

                return null;
            }
        }
    }

    public List<Usuario> listarUsuarios() {

        List<Usuario> usuarios =
                new ArrayList<>();

        if (!esAdministrador()) {
            return usuarios;
        }

        String sql = """
                SELECT
                    id,
                    username,
                    rol,
                    nombre,
                    apellido,
                    correo,
                    activo
                FROM usuarios
                ORDER BY id
                """;

        try (
                Connection con =
                        conexiones.conectar();

                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                usuarios.add(
                        mapearUsuario(rs)
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al listar usuarios: "
                    + e.getMessage()
            );
        }

        return usuarios;
    }

    public boolean registrarUsuario(
            String username,
            String passwordHash,
            String rol,
            String nombre,
            String apellido,
            String correo) {

        if (!esAdministrador()
                || !datosAltaValidos(
                        username,
                        passwordHash,
                        rol,
                        nombre,
                        apellido,
                        correo
                )) {

            return false;
        }

        String sql =
                "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";

        try (
                Connection con =
                        conexiones.conectar();

                CallableStatement cs =
                        con.prepareCall(sql)
        ) {

            cs.setString(
                    1,
                    username.trim()
            );

            cs.setString(
                    2,
                    passwordHash
            );

            cs.setString(
                    3,
                    Rol.fromString(rol).name()
            );

            cs.setString(
                    4,
                    nombre.trim()
            );

            cs.setString(
                    5,
                    apellido.trim()
            );

            cs.setString(
                    6,
                    correo.trim()
            );

            cs.execute();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error al registrar usuario: "
                    + e.getMessage()
            );

            return false;
        }
    }

    public void editarUsuario(
            int id,
            String username,
            String rol,
            String nombre,
            String apellido,
            String correo)
            throws SQLException {

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo un administrador puede editar usuarios."
            );
        }

        username = username == null
                ? ""
                : username.trim();

        nombre = nombre == null
                ? ""
                : nombre.trim();

        apellido = apellido == null
                ? ""
                : apellido.trim();

        correo = correo == null
                ? ""
                : correo.trim();

        Rol rolValidado =
                Rol.fromString(rol);

        if (id <= 0
                || !username.matches(
                        "[A-Za-z0-9._-]{4,20}"
                )
                || rolValidado == null
                || nombre.isBlank()
                || apellido.isBlank()
                || !correo.matches(
                        "[^\\s@]+@[^\\s@]+\\.[^\\s@]+"
                )) {

            throw new IllegalArgumentException(
                    "Revisa usuario (4 a 20 caracteres), "
                    + "rol, nombre, apellido y correo."
            );
        }

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

        Usuario actual =
                sesion.getUsuarioActual();

        if (actual != null
                && actual.getId() == id
                && Rol.fromString(
                        actual.getRol()
                ) != rolValidado) {

            throw new IllegalArgumentException(
                    "No puedes cambiar tu propio rol "
                    + "mientras tienes la sesión iniciada."
            );
        }

        try (
                Connection con =
                        conexiones.conectar()
        ) {

            try (
                    PreparedStatement ps =
                            con.prepareStatement(
                                    "SELECT id FROM usuarios "
                                    + "WHERE username = ? "
                                    + "AND id <> ?"
                            )
            ) {

                ps.setString(
                        1,
                        username
                );

                ps.setInt(
                        2,
                        id
                );

                try (
                        ResultSet rs =
                                ps.executeQuery()
                ) {

                    if (rs.next()) {

                        throw new IllegalArgumentException(
                                "Ese usuario ya existe."
                        );
                    }
                }
            }

            String sql =
                    "UPDATE usuarios "
                    + "SET username = ?, "
                    + "rol = ?, "
                    + "nombre = ?, "
                    + "apellido = ?, "
                    + "correo = ? "
                    + "WHERE id = ?";

            try (
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setString(
                        1,
                        username
                );

                ps.setString(
                        2,
                        rolValidado.name()
                );

                ps.setString(
                        3,
                        nombre
                );

                ps.setString(
                        4,
                        apellido
                );

                ps.setString(
                        5,
                        correo
                );

                ps.setInt(
                        6,
                        id
                );

                if (ps.executeUpdate() != 1) {

                    throw new SQLException(
                            "No se pudo actualizar el usuario."
                    );
                }
            }
        }

        if (actual != null
                && actual.getId() == id) {

            actual.setUsrname(
                    username
            );

            actual.setNombre(
                    nombre
            );

            actual.setApellido(
                    apellido
            );

            actual.setCorreo(
                    correo
            );
        }
    }

    public boolean cambiarEstadoUsuario(
            int id,
            boolean activo) {

        if (!esAdministrador()
                || id <= 0
                || esUsuarioActual(id)) {

            return false;
        }

        String sql =
                "UPDATE usuarios "
                + "SET activo = ? "
                + "WHERE id = ?";

        try (
                Connection con =
                        conexiones.conectar();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setBoolean(
                    1,
                    activo
            );

            ps.setInt(
                    2,
                    id
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al cambiar estado: "
                    + e.getMessage()
            );

            return false;
        }
    }

    public boolean desactivarUsuario(
            int id) {

        return cambiarEstadoUsuario(
                id,
                false
        );
    }

    public boolean activarUsuario(
            int id) {

        return cambiarEstadoUsuario(
                id,
                true
        );
    }

    public boolean existeUsername(
            String username) {

        if (!esAdministrador()
                || username == null
                || username.isBlank()) {

            return false;
        }

        String sql =
                "SELECT 1 "
                + "FROM usuarios "
                + "WHERE username = ? "
                + "LIMIT 1";

        try (
                Connection con =
                        conexiones.conectar();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    username
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al validar username: "
                    + e.getMessage()
            );

            return false;
        }
    }

    public boolean validarPasswordActual(
            int idUsuario,
            String password) {

        if (!esUsuarioActual(idUsuario)
                || password == null
                || password.isEmpty()) {

            return false;
        }

        String sql =
                "SELECT password_hash "
                + "FROM usuarios "
                + "WHERE id = ?";

        String hash =
                PasswordUtil.hashSHA256(
                        password
                );

        try (
                Connection con =
                        conexiones.conectar();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        && hash.equals(
                                rs.getString(
                                        "password_hash"
                                )
                        );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al validar contraseña: "
                    + e.getMessage()
            );

            return false;
        }
    }

    public boolean actualizarPassword(
            int idUsuario,
            String nuevaPassword) {

        if (!esUsuarioActual(idUsuario)
                || nuevaPassword == null
                || nuevaPassword.isBlank()
                || nuevaPassword.length() < 6) {

            return false;
        }

        String sql =
                "UPDATE usuarios "
                + "SET password_hash = ? "
                + "WHERE id = ?";

        try (
                Connection con =
                        conexiones.conectar();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    PasswordUtil.hashSHA256(
                            nuevaPassword
                    )
            );

            ps.setInt(
                    2,
                    idUsuario
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar contraseña: "
                    + e.getMessage()
            );

            return false;
        }
    }

    private boolean datosAltaValidos(
            String username,
            String passwordHash,
            String rol,
            String nombre,
            String apellido,
            String correo) {

        return username != null
                && username
                        .trim()
                        .matches(
                                "[A-Za-z0-9._-]{4,20}"
                        )

                && passwordHash != null
                && passwordHash.matches(
                        "[0-9a-fA-F]{64}"
                )

                && rol != null
                && Rol.fromString(rol) != null

                && nombre != null
                && nombre
                        .trim()
                        .matches(
                                "[\\p{L} ]{2,40}"
                        )

                && apellido != null
                && apellido
                        .trim()
                        .matches(
                                "[\\p{L} ]{2,40}"
                        )

                && correo != null
                && correo
                        .trim()
                        .length() <= 120

                && correo
                        .trim()
                        .matches(
                                "^[A-Za-z0-9._%+-]+"
                                + "@[A-Za-z0-9.-]+"
                                + "\\.[A-Za-z]{2,}$"
                        );
    }

    private Usuario mapearUsuario(
            ResultSet rs)
            throws SQLException {

        Usuario usuario =
                new Usuario();

        usuario.setId(
                rs.getInt(
                        "id"
                )
        );

        usuario.setUsrname(
                rs.getString(
                        "username"
                )
        );

        usuario.setRol(
                rs.getString(
                        "rol"
                )
        );

        usuario.setNombre(
                rs.getString(
                        "nombre"
                )
        );

        usuario.setApellido(
                rs.getString(
                        "apellido"
                )
        );

        try {

            usuario.setCorreo(
                    rs.getString(
                            "correo"
                    )
            );

        } catch (SQLException ignored) {
        }

        usuario.setActivo(
                rs.getBoolean(
                        "activo"
                )
        );

        return usuario;
    }
}