package org.esperanza.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.esperanza.dao.DashboardIndicadoresDao;
import org.esperanza.model.IndicadoresDashboardAdmin;

/** Pruebas sin base de datos para la consulta de indicadores. */
public class PruebaIndicadoresDashboard {

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> tipo, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                tipo.getClassLoader(), new Class<?>[]{tipo}, handler);
    }

    private static final class BaseSimulada {
        BigDecimal ventas = new BigDecimal("125.50");
        long libros = 4;
        long usuarios = 2;
        boolean fallar;
        boolean sinResultado;
        boolean conexionCerrada;
        boolean consultaCerrada;
        boolean resultadoCerrado;
        int consultas;

        Connection conectar() {
            return proxy(Connection.class, (obj, metodo, args) -> {
                return switch (metodo.getName()) {
                    case "prepareStatement" -> consulta((String) args[0]);
                    case "close" -> {
                        conexionCerrada = true;
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(metodo.getName());
                };
            });
        }

        PreparedStatement consulta(String sql) {
            verificar(sql.contains("FROM ventas")
                    && sql.contains("estado = 'COMPLETADA'"),
                    "Las ventas deben incluir solo las completadas");
            verificar(sql.contains("FROM libros")
                    && sql.contains("FROM usuarios")
                    && sql.split("activo = 1", -1).length == 3,
                    "Libros y usuarios deben contar solo los activos");
            consultas++;
            return proxy(PreparedStatement.class, (obj, metodo, args) -> {
                return switch (metodo.getName()) {
                    case "executeQuery" -> {
                        if (fallar) {
                            throw new SQLException("Fallo simulado de la base de datos");
                        }
                        yield resultado();
                    }
                    case "close" -> {
                        consultaCerrada = true;
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(metodo.getName());
                };
            });
        }

        ResultSet resultado() {
            boolean[] leido = {false};
            return proxy(ResultSet.class, (obj, metodo, args) -> {
                return switch (metodo.getName()) {
                    case "next" -> {
                        if (sinResultado || leido[0]) {
                            yield false;
                        }
                        leido[0] = true;
                        yield true;
                    }
                    case "getBigDecimal" -> {
                        verificar("ventas_totales".equals(args[0]), "Alias de ventas incorrecto");
                        yield ventas;
                    }
                    case "getLong" -> switch ((String) args[0]) {
                        case "cantidad_libros" -> libros;
                        case "usuarios_activos" -> usuarios;
                        default -> throw new AssertionError("Alias inesperado: " + args[0]);
                    };
                    case "close" -> {
                        resultadoCerrado = true;
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(metodo.getName());
                };
            });
        }
    }

    public static void main(String[] args) throws Exception {
        BaseSimulada normal = new BaseSimulada();
        IndicadoresDashboardAdmin datos =
                new DashboardIndicadoresDao(normal::conectar).obtenerIndicadores();
        verificar(datos.getVentasTotales().compareTo(new BigDecimal("125.50")) == 0,
                "Total de ventas incorrecto");
        verificar(datos.getCantidadLibros() == 4 && datos.getUsuariosActivos() == 2,
                "Conteos incorrectos");
        verificar(normal.consultas == 1 && normal.conexionCerrada
                && normal.consultaCerrada && normal.resultadoCerrado,
                "La consulta debe ejecutarse una vez y cerrar sus recursos");

        BaseSimulada vacia = new BaseSimulada();
        vacia.ventas = BigDecimal.ZERO;
        vacia.libros = 0;
        vacia.usuarios = 0;
        datos = new DashboardIndicadoresDao(vacia::conectar).obtenerIndicadores();
        verificar(datos.getVentasTotales().signum() == 0
                && datos.getCantidadLibros() == 0
                && datos.getUsuariosActivos() == 0,
                "Los indicadores vacíos deben mostrar cero");

        BaseSimulada fallida = new BaseSimulada();
        fallida.fallar = true;
        try {
            new DashboardIndicadoresDao(fallida::conectar).obtenerIndicadores();
            throw new AssertionError("El error SQL debe llegar al controlador");
        } catch (SQLException esperado) {
            verificar(fallida.conexionCerrada && fallida.consultaCerrada,
                    "La consulta fallida debe cerrar sus recursos");
        }

        BaseSimulada sinFilas = new BaseSimulada();
        sinFilas.sinResultado = true;
        try {
            new DashboardIndicadoresDao(sinFilas::conectar).obtenerIndicadores();
            throw new AssertionError("Una consulta sin datos no puede aparentar cero ventas");
        } catch (SQLException esperado) {
            verificar(sinFilas.resultadoCerrado, "Se debe cerrar el resultado vacío");
        }

        System.out.println("OK - Indicadores Dashboard Admin");
    }
}
