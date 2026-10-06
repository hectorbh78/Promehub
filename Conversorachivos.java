import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Conversorachivos {
    public static List<String> leerLineas(String rutaArchivo) throws IOException {
        Path path = Paths.get(rutaArchivo);
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void escribirTexto(String rutaArchivo, String contenido) throws IOException {
        Path path = Paths.get(rutaArchivo);
        Path carpeta = path.getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        Files.writeString(path, contenido, StandardCharsets.UTF_8);
    }
}
