package promehub;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Clase encargada de leer y escribir el catálogo de videojuegos en archivos CSV. */
public class GestorCSV {

    // Cabecera por defecto para guardar/exportar archivos en formato CSV con ','
    private static final String CABECERA_EXPORTACION =
            "id,titulo,plataforma,genero,precio,stock,codigoProveedor";
    
    // Lista con los nombres exactos de columnas que se exigen al importar un CSV
    private static final List<String> CABECERAS = List.of(
            "id", "titulo", "plataforma", "genero", "precio", "stock", "codigoproveedor");

    // Clase auxiliar para guardar el resultado de la lectura (juegos válidos, errores y contador)
    public static class ResultadoCarga {
        public final List<Videojuego> videojuegos = new ArrayList<>();
        public final List<String> errores = new ArrayList<>();
        public int leidos;
    }

    // Lee un archivo CSV desde un disco y devuelve los videojuegos válidos cargados
    public ResultadoCarga cargar(Path ruta) throws IOException {
        // Comprueba si el archivo existe en el disco
        if (!Files.exists(ruta)) {
            throw new FileNotFoundException("El fichero CSV '" + ruta + "' no existe.");
        }
        // Comprueba que la ruta sea un archivo normal y no una carpeta
        if (!Files.isRegularFile(ruta)) {
            throw new IOException("'" + ruta + "' no es un fichero válido.");
        }

        ResultadoCarga resultado = new ResultadoCarga();
        Set<Integer> idsVistos = new HashSet<>(); // Conjunto para detectar IDs duplicados

        // Abre y lee el archivo en formato UTF-8 de forma segura
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String cabecera = lector.readLine();
            if (cabecera == null) {
                throw new IOException("El fichero CSV está vacío.");
            }
           

            // Detecta si se usa coma o punto y coma como separador
            String separador = detectarSeparador(cabecera);
            List<String> nombres = parsearLinea(cabecera, separador);
            validarCabecera(nombres); // Verifica que la cabecera tenga las columnas correctas

            String linea;
            int numLinea = 1;
            // Acceso secuencial: se lee el archivo línea por línea, de principio a fin
            while ((linea = lector.readLine()) != null) {
                numLinea++;
                if (linea.isBlank()) {
                    continue; // Ignora líneas en blanco
                }
                resultado.leidos++;
                try {
                    // Convierte los datos de la línea en un objeto Videojuego
                    Videojuego videojuego = convertir(parsearLinea(linea, separador));
                    // Evita guardar videojuegos con un ID repetido
                    if (!idsVistos.add(videojuego.getId())) {
                        throw new IllegalArgumentException(
                                "el id " + videojuego.getId() + " está repetido");
                    }
                    resultado.videojuegos.add(videojuego);
                } catch (IllegalArgumentException e) {
                    // Si ocurre un error en la línea, lo registra y salta esa fila
                    resultado.errores.add("Línea " + numLinea + " descartada: " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    // Identifica si la cabecera usa más comas o puntos y comas para determinar el separador
    private String detectarSeparador(String cabecera) {
        int comas = contarSeparadores(cabecera, ',');
        int puntosYComas = contarSeparadores(cabecera, ';');
        return puntosYComas > comas ? ";" : ",";
    }

    // Cuenta cuántos separadores hay en una línea, ignorando los que estén dentro de comillas
    private int contarSeparadores(String linea, char separador) {
        boolean entreComillas = false;
        int total = 0;
        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);
            if (caracter == '"') {
                // Gestiona comillas dobles escapadas ("")
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (!entreComillas && caracter == separador) {
                total++;
            }
        }
        return total;
    }

    // Divide una línea de texto CSV en sus diferentes campos respetando comillas
    private List<String> parsearLinea(String linea, String separador) {
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreComillas = false;
        boolean comillasCerradas = false;

        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);
            if (entreComillas) {
                if (caracter == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        entreComillas = false;
                        comillasCerradas = true;
                    }
                } else {
                    campo.append(caracter);
                }
            } else if (caracter == separador.charAt(0)) {
                campos.add(campo.toString());
                campo.setLength(0);
                comillasCerradas = false;
            } else if (caracter == '"' && campo.toString().trim().isEmpty() && !comillasCerradas) {
                campo.setLength(0);
                entreComillas = true;
            } else if (comillasCerradas && !Character.isWhitespace(caracter)) {
                throw new IllegalArgumentException("hay texto después del cierre de comillas");
            } else if (caracter == '"') {
                throw new IllegalArgumentException("comillas no válidas en un campo sin entrecomillar");
            } else if (!comillasCerradas) {
                campo.append(caracter);
            }
        }

        if (entreComillas) {
            throw new IllegalArgumentException("campo entrecomillado sin cierre");
        }
        campos.add(campo.toString());
        return campos;
    }

    // Comprueba que las columnas del archivo coincidan exactamente con la estructura esperada
    private void validarCabecera(List<String> nombres) throws IOException {
        if (nombres.size() != CABECERAS.size()) {
            throw new IOException("La cabecera CSV debe contener exactamente estos 7 campos: "
                    + String.join(", ", CABECERAS) + ".");
        }
        for (int i = 0; i < CABECERAS.size(); i++) {
            String nombre = nombres.get(i).trim().toLowerCase(Locale.ROOT);
            if (!CABECERAS.get(i).equals(nombre)) {
                throw new IOException("Cabecera CSV no válida en la columna " + (i + 1)
                        + ": se esperaba '" + CABECERAS.get(i) + "' y se encontró '"
                        + nombres.get(i) + "'.");
            }
        }
    }

    // Convierte una lista de 7 campos de texto en un objeto de tipo Videojuego validando sus datos
    private Videojuego convertir(List<String> campos) {
        if (campos.size() != CABECERAS.size()) {
            throw new IllegalArgumentException(
                    "se esperaban 7 campos y hay " + campos.size());
        }
        int id = entero("id", campos.get(0));
        String titulo = campos.get(1).trim();
        String plataforma = campos.get(2).trim();
        String genero = campos.get(3).trim();
        double precio = decimal("precio", campos.get(4));
        int stock = entero("stock", campos.get(5));
        String proveedor = campos.get(6).trim();

        // Validaciones de reglas de negocio
        if (id <= 0) {
            throw new IllegalArgumentException("el id debe ser mayor que cero");
        }
        if (titulo.isEmpty() || plataforma.isEmpty() || genero.isEmpty()) {
            throw new IllegalArgumentException("título, plataforma y género son obligatorios");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("el precio debe ser un número finito no negativo");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("el stock no puede ser negativo");
        }
        return new Videojuego(id, titulo, plataforma, genero, precio, stock, proveedor);
    }

    // Intenta convertir un String a número entero
    private int entero(String campo, String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "el campo '" + campo + "' no es un entero válido ('" + valor + "')");
        }
    }

    // Intenta convertir un String a número decimal (acepta tanto comas como puntos)
    private double decimal(String campo, String valor) {
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "el campo '" + campo + "' no es un número válido ('" + valor + "')");
        }
    }

    // Guarda o exporta la lista de videojuegos a un archivo CSV en disco
    public void exportar(Path ruta, List<Videojuego> videojuegos) throws IOException {
        Path carpetaDestino = ruta.getParent();
        if (carpetaDestino != null) {
            Files.createDirectories(carpetaDestino); // Crea las carpetas del directorio si no existen
        }

        try (BufferedWriter escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            escritor.write(CABECERA_EXPORTACION); // Escribe la primera línea (cabecera)
            escritor.newLine();
            for (Videojuego videojuego : videojuegos) {
                escritor.write(String.join(",",
                        Integer.toString(videojuego.getId()),
                        escaparCSV(videojuego.getTitulo()),
                        escaparCSV(videojuego.getPlataforma()),
                        escaparCSV(videojuego.getGenero()),
                        String.format(Locale.ROOT, "%.2f", videojuego.getPrecio()),
                        Integer.toString(videojuego.getStock()),
                        escaparCSV(videojuego.getCodigoProveedor())));
                escritor.newLine();
            }
        }
    }

    // Envuelve entre comillas los textos si contienen caracteres especiales del CSV (;, ", saltos de línea)
    private String escaparCSV(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.indexOf(',') >= 0 || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}