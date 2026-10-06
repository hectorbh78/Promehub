package promehub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Fachada de operaciones del catálogo usada por PromeHubManager. */
public class PromeHubStore {
    private final List<Videojuego> catalogo = new ArrayList<>();
    private final GestorCSV gestorCSV = new GestorCSV();
    private final GestorXML gestorXML = new GestorXML();

    public List<Videojuego> getCatalogo() {
        return catalogo;
    }

    public void cargarDesdeCSV(String rutaArchivo) {
        try {
            GestorCSV.ResultadoCarga resultado = gestorCSV.cargar(resolverRuta(rutaArchivo));
            catalogo.clear();
            catalogo.addAll(resultado.videojuegos);
            resultado.errores.forEach(System.out::println);
            System.out.println("CSV cargado: " + catalogo.size() + " videojuegos válidos; "
                    + resultado.errores.size() + " líneas descartadas.");
        } catch (IOException e) {
            System.out.println("ERROR al cargar CSV: " + e.getMessage());
        }
    }

    public void exportarACSV(String rutaArchivo) {
        try {
            gestorCSV.exportar(resolverRuta(rutaArchivo), catalogo);
            System.out.println("Catálogo exportado a CSV: " + resolverRuta(rutaArchivo).toAbsolutePath());
        } catch (IOException e) {
            System.out.println("ERROR al exportar CSV: " + e.getMessage());
        }
    }

    public void exportarAXML(String rutaArchivo) {
        try {
            gestorXML.exportar(resolverRuta(rutaArchivo), catalogo);
            System.out.println("Catálogo exportado a XML: " + resolverRuta(rutaArchivo).toAbsolutePath());
        } catch (IOException e) {
            System.out.println("ERROR al exportar XML: " + e.getMessage());
        }
    }

    public void cargarDesdeXML(String rutaArchivo) {
        try {
            List<Videojuego> videojuegos = gestorXML.cargar(resolverRuta(rutaArchivo));
            catalogo.clear();
            catalogo.addAll(videojuegos);
            System.out.println("XML cargado correctamente. Registros cargados: " + catalogo.size());
        } catch (IOException e) {
            System.out.println("ERROR al cargar XML: " + e.getMessage());
        }
    }

    public void mostrarCatalogo() {
        if (catalogo.isEmpty()) {
            System.out.println("El catálogo está vacío.");
            return;
        }
        System.out.println("CATÁLOGO DE VIDEOJUEGOS");
        catalogo.forEach(System.out::println);
    }

    public List<Videojuego> buscarVideojuego(String criterio) {
        List<Videojuego> resultados = new ArrayList<>();
        if (criterio == null || criterio.isBlank()) {
            System.out.println("Debes introducir un criterio de búsqueda.");
            return resultados;
        }

        String texto = criterio.trim().toLowerCase(Locale.ROOT);
        for (Videojuego videojuego : catalogo) {
            if (String.valueOf(videojuego.getId()).equals(texto)
                    || contiene(videojuego.getTitulo(), texto)
                    || contiene(videojuego.getPlataforma(), texto)
                    || contiene(videojuego.getGenero(), texto)
                    || contiene(videojuego.getCodigoProveedor(), texto)) {
                resultados.add(videojuego);
            }
        }

        if (resultados.isEmpty()) {
            System.out.println("No se han encontrado coincidencias para: " + criterio);
        } else {
            System.out.println("Resultados de la búsqueda para '" + criterio + "':");
            resultados.forEach(System.out::println);
        }
        return resultados;
    }

    public void mostrarInformacionFicheros(String... rutas) {
        if (rutas == null || rutas.length == 0) {
            System.out.println("No se proporcionaron rutas para consultar.");
            return;
        }

        for (String ruta : rutas) {
            Path path = resolverRuta(ruta);
            System.out.println("Archivo: " + path.toAbsolutePath());
            if (!Files.exists(path)) {
                System.out.println("No existe.");
                continue;
            }
            try {
                System.out.println("Es directorio: " + Files.isDirectory(path));
                System.out.println("Tamaño: " + Files.size(path) + " bytes");
                System.out.println("Última modificación: " + Files.getLastModifiedTime(path));
                System.out.println("Extensión: " + obtenerExtension(path));
            } catch (IOException e) {
                System.out.println("ERROR al consultar el archivo: " + e.getMessage());
            }
            System.out.println("----------------------------------------");
        }
    }

    private Path resolverRuta(String rutaArchivo) {
        return Paths.get(rutaArchivo);
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(texto);
    }

    private String obtenerExtension(Path path) {
        Path nombrePath = path.getFileName();
        String nombre = nombrePath == null ? "" : nombrePath.toString();
        int indice = nombre.lastIndexOf('.');
        return indice >= 0 ? nombre.substring(indice + 1) : "sin extensión";
    }
}
