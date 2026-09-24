import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PruebaInterfacesFXML {

    private static int archivosRevisados = 0;
    private static int errores = 0;

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("T4.I.12 - REVISION DE INTERFACES FXML");
        System.out.println("========================================");

        File carpeta = new File(
                "src/org/esperanza/view"
        );

        if (!carpeta.exists()) {

            System.out.println(
                    "ERROR: no existe la carpeta de vistas."
            );

            return;
        }

        List<File> archivosFXML =
                new ArrayList<>();

        buscarFXML(
                carpeta,
                archivosFXML
        );

        for (File archivo : archivosFXML) {

            revisarFXML(archivo);
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("RESULTADO FINAL");
        System.out.println("========================================");

        System.out.println(
                "FXML revisados: "
                + archivosRevisados
        );

        System.out.println(
                "Errores encontrados: "
                + errores
        );

        if (errores == 0) {

            System.out.println(
                    "PRUEBA EXITOSA - T4.I.12"
            );

            System.out.println(
                    "No se encontraron referencias FXML invalidas."
            );

        } else {

            System.out.println(
                    "PRUEBA CON ERRORES - T4.I.12"
            );

            System.out.println(
                    "Se deben corregir las interfaces indicadas."
            );
        }
    }

    private static void buscarFXML(
            File carpeta,
            List<File> archivos) {

        File[] contenido =
                carpeta.listFiles();

        if (contenido == null) {
            return;
        }

        for (File archivo : contenido) {

            if (archivo.isDirectory()) {

                buscarFXML(
                        archivo,
                        archivos
                );

            } else if (
                    archivo.getName()
                            .toLowerCase()
                            .endsWith(".fxml")) {

                archivos.add(archivo);
            }
        }
    }

    private static void revisarFXML(
            File archivo) {

        archivosRevisados++;

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println(
                "Revisando: "
                + archivo.getName()
        );
        System.out.println("----------------------------------------");

        try {

            String contenido =
                    Files.readString(
                            archivo.toPath(),
                            StandardCharsets.UTF_8
                    );

            String controlador =
                    obtenerControlador(contenido);

            if (controlador == null) {

                System.out.println(
                        "SIN CONTROLADOR: "
                        + archivo.getName()
                );

                return;
            }

            System.out.println(
                    "Controlador: "
                    + controlador
            );

            Class<?> claseControlador;

            try {

                claseControlador =
                        Class.forName(controlador);

            } catch (ClassNotFoundException ex) {

                System.out.println(
                        "ERROR: controlador no encontrado."
                );

                errores++;
                return;
            }

            List<String> eventos =
                    obtenerEventos(contenido);

            if (eventos.isEmpty()) {

                System.out.println(
                        "Sin eventos declarados."
                );

            } else {

                for (String evento : eventos) {

                    boolean existe =
                            existeMetodo(
                                    claseControlador,
                                    evento
                            );

                    System.out.println(
                            evento
                            + " -> "
                            + (
                                    existe
                                    ? "CORRECTO"
                                    : "ERROR"
                            )
                    );

                    if (!existe) {
                        errores++;
                    }
                }
            }

        } catch (Exception ex) {

            errores++;

            System.out.println(
                    "ERROR leyendo FXML: "
                    + ex.getMessage()
            );
        }
    }

    private static String obtenerControlador(
            String contenido) {

        Pattern patron =
                Pattern.compile(
                        "fx:controller=\"([^\"]+)\""
                );

        Matcher matcher =
                patron.matcher(contenido);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static List<String> obtenerEventos(
            String contenido) {

        List<String> eventos =
                new ArrayList<>();

        Pattern patron =
                Pattern.compile(
                        "on(?:Action|MouseClicked|MouseEntered|MouseExited|KeyPressed|KeyReleased)=\"#([^\"]+)\""
                );

        Matcher matcher =
                patron.matcher(contenido);

        while (matcher.find()) {

            String metodo =
                    matcher.group(1);

            if (!eventos.contains(metodo)) {
                eventos.add(metodo);
            }
        }

        return eventos;
    }

    private static boolean existeMetodo(
            Class<?> clase,
            String nombreMetodo) {

        Class<?> actual = clase;

        while (actual != null) {

            Method[] metodos =
                    actual.getDeclaredMethods();

            for (Method metodo : metodos) {

                if (metodo.getName()
                        .equals(nombreMetodo)) {

                    return true;
                }
            }

            actual =
                    actual.getSuperclass();
        }

        return false;
    }
}