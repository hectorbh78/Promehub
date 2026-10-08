package promehub;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {

    // Rutas por defecto de los archivos que utiliza el programa.
    private static final String CSV_ENTRADA = "datos/videojuegos.csv";
    private static final String XML = "datos/catalogo.xml";
    private static final String CSV_SALIDA = "datos/catalogo_exportado.csv";

    // Objetos utilizados para leer datos y gestionar los archivos.
    private final Scanner teclado = new Scanner(System.in);
    private final GestorCSV gestorCSV = new GestorCSV();
    private final GestorXML gestorXML = new GestorXML();

    // Lista donde se guardan los videojuegos del catálogo.
    private List<Videojuego> catalogo = new ArrayList<>();

    // Rutas de los diferentes archivos.
    private Path rutaCsvEntrada = Paths.get(CSV_ENTRADA);
    private Path rutaXml = Paths.get(XML);
    private Path rutaCsvSalida = Paths.get(CSV_SALIDA);

    public static void main(String[] args) {
        new App().ejecutar();
    }

    // Método que controla el funcionamiento principal del menú.
    private void ejecutar() {
        int opcion = -1;

        // El menú se repite hasta seleccionar la opción 0.
        do {
            mostrarMenu();

            if (!teclado.hasNextLine()) {
                System.out.println("\nEntrada cerrada. Saliendo de PromeHub Data Exchange.");
                return;
            }

            try {
                opcion = Integer.parseInt(teclado.nextLine().trim());

                // Ejecutamos una acción dependiendo de la opción elegida.
                switch (opcion) {
                    case 1: cargarCsv(); break;
                    case 2: mostrarCatalogo(); break;
                    case 3: exportarXml(); break;
                    case 4: cargarXml(); break;
                    case 5: exportarCsv(); break;
                    case 6: buscar(); break;
                    case 7: informacionFicheros(); break;
                    case 0: System.out.println("Saliendo de PromeHub Data Exchange. ¡Hasta pronto!"); break;
                    default: System.out.println("Opción no válida: elige un número entre 0 y 7.");
                }

            } catch (NumberFormatException e) {
                // Si se introduce algo que no es un número.
                opcion = -1;
                System.out.println("Opción no válida: debes escribir un número entre 0 y 7.");
            }

        } while (opcion != 0);
    }

    // Muestra las diferentes opciones del programa.
    private void mostrarMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" PROMEHUB DATA EXCHANGE");
        System.out.println("========================================");
        System.out.println("1. Cargar catálogo desde CSV");
        System.out.println("2. Mostrar catálogo");
        System.out.println("3. Exportar catálogo a XML");
        System.out.println("4. Cargar catálogo desde XML");
        System.out.println("5. Exportar catálogo a CSV");
        System.out.println("6. Buscar videojuego");
        System.out.println("7. Información de ficheros");
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
    }

    // Permite introducir una ruta de archivo.
    private Path pedirRuta(String texto, Path porDefecto) {
        System.out.print(texto + " [" + porDefecto + "]: ");

        if (!teclado.hasNextLine()) {
            return porDefecto;
        }

        String entrada = teclado.nextLine().trim();

        // Si no se escribe nada, se utiliza la ruta por defecto.
        return entrada.isEmpty() ? porDefecto : Paths.get(entrada);
    }

    // Carga el catálogo desde un archivo CSV.
    private void cargarCsv() {
        Path ruta = pedirRuta("Ruta del CSV", rutaCsvEntrada);

        try {
            GestorCSV.ResultadoCarga r = gestorCSV.cargar(ruta);

            rutaCsvEntrada = ruta;
            catalogo = r.videojuegos;

            // Mostramos información sobre los datos cargados.
            System.out.println("CSV procesado: " + r.leidos + " registros leídos, "
                    + r.videojuegos.size() + " válidos, " + r.errores.size() + " descartados.");

            r.errores.forEach(System.out::println);

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("ERROR de lectura del CSV: " + e.getMessage());
        }
    }

    // Muestra todos los videojuegos del catálogo.
    private void mostrarCatalogo() {

        // Comprobamos si hay videojuegos cargados.
        if (catalogo.isEmpty()) {
            System.out.println("El catálogo está vacío. Carga antes un CSV (1) o un XML (4).");
            return;
        }

        System.out.println("--- CATÁLOGO (" + catalogo.size() + " videojuegos) ---");

        // Recorremos la lista y mostramos cada videojuego.
        catalogo.forEach(System.out::println);
    }

    // Exporta el catálogo a un archivo XML.
    private void exportarXml() {
        if (catalogo.isEmpty()) {
            System.out.println("No hay datos que exportar. Carga antes un CSV (1) o un XML (4).");
            return;
        }

        Path ruta = pedirRuta("Ruta del XML de salida", rutaXml);

        try {
            gestorXML.exportar(ruta, catalogo);
            rutaXml = ruta;

            System.out.println("XML generado correctamente con " + catalogo.size() + " videojuegos en " + ruta + ".");

        } catch (IOException e) {
            System.out.println("ERROR al generar el XML: " + e.getMessage());
        }
    }

    // Carga el catálogo desde un archivo XML.
    private void cargarXml() {
        Path ruta = pedirRuta("Ruta del XML de entrada", rutaXml);

        try {
            catalogo = gestorXML.cargar(ruta);
            rutaXml = ruta;

            System.out.println("XML cargado correctamente: " + catalogo.size() + " videojuegos.");

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());

        } catch (IOException e) {
            System.out.println("ERROR al procesar el XML: " + e.getMessage());
        }
    }

    // Exporta el catálogo a un archivo CSV.
    private void exportarCsv() {
        if (catalogo.isEmpty()) {
            System.out.println("No hay datos que exportar. Carga antes un CSV (1) o un XML (4).");
            return;
        }

        Path ruta = pedirRuta("Ruta del CSV de salida", rutaCsvSalida);

        try {
            gestorCSV.exportar(ruta, catalogo);
            rutaCsvSalida = ruta;

            System.out.println("CSV generado correctamente con " + catalogo.size() + " videojuegos en " + ruta + ".");

        } catch (IOException e) {
            System.out.println("ERROR de escritura del CSV: " + e.getMessage());
        }
    }

    // Permite buscar videojuegos por identificador (exacto) o por título (contiene el texto).
    private void buscar() {
        if (catalogo.isEmpty()) {
            System.out.println("El catálogo está vacío. Carga antes un CSV (1) o un XML (4).");
            return;
        }

        System.out.print("Introduce el id o el título (o parte del título): ");

        if (!teclado.hasNextLine()) {
            return;
        }

        String texto = teclado.nextLine().trim().toLowerCase(Locale.ROOT);

        if (texto.isEmpty()) {
            System.out.println("No has escrito nada que buscar.");
            return;
        }

        int encontrados = 0;

        for (Videojuego v : catalogo) {
            boolean coincideId = String.valueOf(v.getId()).equals(texto);
            boolean coincideTitulo = v.getTitulo() != null
                    && v.getTitulo().toLowerCase(Locale.ROOT).contains(texto);

            if (coincideId || coincideTitulo) {
                System.out.println(v);
                encontrados++;
            }
        }

        if (encontrados == 0) {
            System.out.println("No se ha encontrado ningún videojuego con id o título '" + texto + "'.");
        }
    }

    // Muestra información sobre los diferentes archivos.
    private void informacionFicheros() {
        System.out.println("--- INFORMACIÓN DE FICHEROS ---");

        describir("CSV de entrada", rutaCsvEntrada);
        describir("XML", rutaXml);
        describir("CSV de salida", rutaCsvSalida);
    }

    // Muestra la ruta, si existe y el tamaño de un archivo.
    private void describir(String etiqueta, Path ruta) {
        System.out.println(etiqueta + ":");
        System.out.println("Ruta: " + ruta.toAbsolutePath());

        // Comprobamos si el archivo existe.
        boolean existe = Files.exists(ruta);

        System.out.println("Existe: " + (existe ? "sí" : "no"));

        if (existe) {
            try {
                // Si existe, mostramos su tamaño.
                System.out.println("Tamaño: " + Files.size(ruta) + " bytes");

            } catch (IOException e) {
                System.out.println("Tamaño: no se pudo leer (" + e.getMessage() + ")");
            }
        }
    }

}

