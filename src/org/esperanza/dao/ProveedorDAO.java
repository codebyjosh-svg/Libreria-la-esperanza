package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.esperanza.model.Proveedor;
import org.esperanza.model.Usuario;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.Conexion;

/** CRUD protegido: la baja lógica conserva las referencias y el NIT único. */
public final class ProveedorDAO {
    private static final String COLUMNAS = "id, nit, nombre, contacto, telefono, correo, direccion, activo";
    private final ProveedorConexion conexiones;

    public ProveedorDAO() { this(() -> Conexion.getInstancia().conectar()); }
    public ProveedorDAO(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones, "La conexión es obligatoria.");
    }

    public List<Proveedor> listar(String filtro, boolean incluirInactivos) throws SQLException {
        autorizar();
        String texto = filtro == null ? "" : filtro.trim();
        if (texto.length() > 150) throw new IllegalArgumentException("La búsqueda admite hasta 150 caracteres.");
        // LOCATE interpreta literalmente %, _ y las comillas; no construye SQL con la entrada.
        String sql = "SELECT " + COLUMNAS + " FROM proveedores WHERE (? = TRUE OR activo = TRUE)"
                + " AND (? = '' OR LOCATE(?, nombre) > 0 OR LOCATE(?, nit) > 0"
                + " OR LOCATE(?, contacto) > 0) ORDER BY nombre, id";
        try (Connection con = conexiones.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, incluirInactivos);
            for (int i = 2; i <= 5; i++) ps.setString(i, texto);
            String nitBuscado = texto.toUpperCase(Locale.ROOT).replace("-", "").replace(" ", "");
            ps.setString(4, nitBuscado.isEmpty() ? texto : nitBuscado);
            try (ResultSet rs = ps.executeQuery()) {
                List<Proveedor> resultado = new ArrayList<>();
                while (rs.next()) resultado.add(leer(rs));
                return resultado;
            }
        }
    }

    public Optional<Proveedor> buscarPorId(int id) throws SQLException {
        autorizar();
        validarId(id);
        try (Connection con = conexiones.conectar();
                PreparedStatement ps = con.prepareStatement("SELECT " + COLUMNAS + " FROM proveedores WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(leer(rs)) : Optional.empty(); }
        }
    }

    public int crear(Proveedor proveedor) throws SQLException {
        autorizar();
        Objects.requireNonNull(proveedor, "El proveedor es obligatorio.");
        if (proveedor.getId() != 0) throw new IllegalArgumentException("Un proveedor nuevo debe tener identificador 0.");
        if (!proveedor.isActivo()) throw new IllegalArgumentException("Un proveedor nuevo debe estar activo.");
        String sql = "INSERT INTO proveedores (nit, nombre, contacto, telefono, correo, direccion, activo)"
                + " VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection con = conexiones.conectar();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            escribirDatos(ps, proveedor);
            if (ps.executeUpdate() != 1) throw new SQLException("No se pudo crear el proveedor.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No se recibió el identificador del proveedor creado.");
                return rs.getInt(1);
            }
        } catch (SQLException ex) { throw traducirDuplicado(ex); }
    }

    /** El estado se modifica únicamente mediante cambiarEstado, nunca desde la edición. */
    public void actualizar(Proveedor proveedor) throws SQLException {
        autorizar();
        Objects.requireNonNull(proveedor, "El proveedor es obligatorio.");
        validarId(proveedor.getId());
        String sql = "UPDATE proveedores SET nit = ?, nombre = ?, contacto = ?, telefono = ?, correo = ?, direccion = ? WHERE id = ?";
        try (Connection con = conexiones.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            escribirDatos(ps, proveedor);
            ps.setInt(7, proveedor.getId());
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("El proveedor ya no existe o no se pudo actualizar.");
        } catch (SQLException ex) { throw traducirDuplicado(ex); }
    }

    public void cambiarEstado(int id, boolean activo) throws SQLException {
        autorizar();
        validarId(id);
        String sql = "UPDATE proveedores SET activo = ? WHERE id = ? AND activo <> ?";
        try (Connection con = conexiones.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.setBoolean(3, activo);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("El proveedor ya no existe o ya tiene el estado solicitado. Actualice el listado.");
            }
        }
    }

    private static void autorizar() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        Usuario usuario = sesion.getUsuarioActual();
        if (!sesion.haySesionActiva() || usuario == null || !usuario.isActivo()
                || !sesion.tienePermiso("GESTION_PROVEEDORES")) {
            throw new SecurityException("Solo un administrador con sesión activa puede gestionar proveedores.");
        }
    }

    private static void validarId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Seleccione un proveedor válido.");
    }

    private static void escribirDatos(PreparedStatement ps, Proveedor proveedor) throws SQLException {
        ps.setString(1, proveedor.getNit());
        ps.setString(2, proveedor.getNombre());
        ps.setString(3, proveedor.getContacto());
        ps.setString(4, proveedor.getTelefono());
        ps.setString(5, proveedor.getCorreo());
        ps.setString(6, proveedor.getDireccion());
    }

    private static Proveedor leer(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id"), rs.getString("nit"), rs.getString("nombre"),
                rs.getString("contacto"), rs.getString("telefono"), rs.getString("correo"),
                rs.getString("direccion"), rs.getBoolean("activo"));
    }

    private static SQLException traducirDuplicado(SQLException ex) {
        if (ex.getErrorCode() == 1062 || "23505".equals(ex.getSQLState())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese NIT. Incluya inactivos para editarlo o reactivarlo.", ex);
        }
        return ex;
    }
}
