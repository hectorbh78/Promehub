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

/** Lectura y escritura del catálogo en CSV con separador coma o punto y coma. */
public class GestorCSV {

    private static final String CABECERA_EXPORTACION =
            "id;titulo;plataforma;genero;precio;stock;codigoProveedor";
    private static final List<String> CABECERAS = List.of(
            "id", "titulo", "plataforma", "genero", "precio", "stock", "codigoproveedor");

    public static class ResultadoCarga {
        public final List<Videojuego> videojuegos = new ArrayList<>();
        public final List<String> errores = new ArrayList<>();
        public int leidos;
    }

    public ResultadoCarga cargar(Path ruta) throws IOException {
        if (!Files.exists(ruta)) {
            throw new FileNotFoundException("El fichero CSV '" + ruta + "' no existe.");
        }
        if (!Files.isRegularFile(ruta)) {
            throw new IOException("'" + ruta + "' no es un fichero válido.");
        }

        ResultadoCarga resultado = new ResultadoCarga();
        Set<Integer> idsVistos = new HashSet<>();

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String cabecera = lector.readLine();
            if (cabecera == null) {
                throw new IOException("El fichero CSV está vacío.");
            }
            if (!cabecera.isEmpty() && cabecera.charAt(0) == '\uFEFF') {
                cabecera = cabecera.substring(1);
            }

            String separador = detectarSeparador(cabecera);
            List<String> nombres = parsearLinea(cabecera, separador);
            validarCabecera(nombres);

            String linea;
            int numLinea = 1;
            while ((linea = lector.readLine()) != null) {
                numLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                resultado.leidos++;
                try {
                    Videojuego videojuego = convertir(parsearLinea(linea, separador));
                    if (!idsVistos.add(videojuego.getId())) {
                        throw new IllegalArgumentException(
                                "el id " + videojuego.getId() + " está repetido");
                    }
                    resultado.videojuegos.add(videojuego);
                } catch (IllegalArgumentException e) {
                    resultado.errores.add("Línea " + numLinea + " descartada: " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    private String detectarSeparador(String cabecera) {
        int comas = contarSeparadores(cabecera, ',');
        int puntosYComas = contarSeparadores(cabecera, ';');
        return puntosYComas > comas ? ";" : ",";
    }

    private int contarSeparadores(String linea, char separador) {
        boolean entreComillas = false;
        int total = 0;
        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);
            if (caracter == '"') {
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

    private int entero(String campo, String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "el campo '" + campo + "' no es un entero válido ('" + valor + "')");
        }
    }

    private double decimal(String campo, String valor) {
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "el campo '" + campo + "' no es un número válido ('" + valor + "')");
        }
    }

    public void exportar(Path ruta, List<Videojuego> videojuegos) throws IOException {
        Path carpetaDestino = ruta.getParent();
        if (carpetaDestino != null) {
            Files.createDirectories(carpetaDestino);
        }

        try (BufferedWriter escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            escritor.write(CABECERA_EXPORTACION);
            escritor.newLine();
            for (Videojuego videojuego : videojuegos) {
                escritor.write(String.join(";",
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

    private String escaparCSV(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.indexOf(';') >= 0 || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
