-- T3.3.1 y T3.3.2: stock actual igual o inferior al mínimo.
-- Ejecutar únicamente esta consulta; no vuelve a insertar datos.
USE libreriadb_in4cm;
SELECT isbn, titulo, stock_actual, stock_minimo
FROM libros
WHERE stock_actual <= stock_minimo AND activo = TRUE
ORDER BY stock_actual ASC, titulo ASC;
