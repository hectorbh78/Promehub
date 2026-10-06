package promehub;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GestorXML {

    public void exportar(Path ruta, List<Videojuego> videojuegos) throws IOException {
        try {
            Document document = crearFabricaSegura().newDocumentBuilder().newDocument();
            Element raiz = document.createElement("catalogo");
            document.appendChild(raiz);

            for (Videojuego videojuego : videojuegos) {
                Element juego = document.createElement("videojuego");
                juego.setAttribute("id", String.valueOf(videojuego.getId()));
                agregarTexto(document, juego, "titulo", videojuego.getTitulo());
                agregarTexto(document, juego, "plataforma", videojuego.getPlataforma());
                agregarTexto(document, juego, "genero", videojuego.getGenero());
                agregarTexto(document, juego, "precio", String.valueOf(videojuego.getPrecio()));
                agregarTexto(document, juego, "stock", String.valueOf(videojuego.getStock()));
                agregarTexto(document, juego, "codigoProveedor", videojuego.getCodigoProveedor());
                raiz.appendChild(juego);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

            Path carpeta = ruta.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            transformer.transform(new DOMSource(document), new StreamResult(ruta.toFile()));
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException("No se pudo generar el XML: " + e.getMessage(), e);
        }
    }

    public List<Videojuego> cargar(Path ruta) throws IOException {
        if (!Files.exists(ruta)) {
            throw new FileNotFoundException("El fichero XML '" + ruta + "' no existe.");
        }
        if (!Files.isRegularFile(ruta)) {
            throw new IOException("'" + ruta + "' no es un fichero válido.");
        }

        try {
            DocumentBuilder constructor = crearFabricaSegura().newDocumentBuilder();
            Document document = constructor.parse(ruta.toFile());
            Element raiz = document.getDocumentElement();
            if (raiz == null || !"catalogo".equals(raiz.getTagName())) {
                throw new IOException("La raíz del XML debe ser <catalogo>.");
            }

            NodeList nodos = raiz.getElementsByTagName("videojuego");
            List<Videojuego> videojuegos = new ArrayList<>();
            Set<Integer> ids = new HashSet<>();
            for (int i = 0; i < nodos.getLength(); i++) {
                Element elemento = (Element) nodos.item(i);
                Videojuego videojuego = convertir(elemento);
                if (!ids.add(videojuego.getId())) {
                    throw new IOException("El XML contiene el id repetido " + videojuego.getId() + ".");
                }
                videojuegos.add(videojuego);
            }
            return videojuegos;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("El XML no es válido o no se pudo analizar: " + e.getMessage(), e);
        }
    }

    private DocumentBuilderFactory crearFabricaSegura() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private Videojuego convertir(Element elemento) throws IOException {
        String idTexto = elemento.hasAttribute("id")
                ? elemento.getAttribute("id")
                : obtenerTexto(elemento, "id", true);
        int id = entero("id", idTexto);
        String titulo = obtenerTexto(elemento, "titulo", true);
        String plataforma = obtenerTexto(elemento, "plataforma", true);
        String genero = obtenerTexto(elemento, "genero", true);
        double precio = decimal(obtenerTexto(elemento, "precio", true));
        int stock = entero("stock", obtenerTexto(elemento, "stock", true));
        String codigoProveedor = obtenerTexto(elemento, "codigoProveedor", false);

        if (id <= 0) {
            throw new IOException("El id debe ser mayor que cero.");
        }
        if (titulo.isBlank() || plataforma.isBlank() || genero.isBlank()) {
            throw new IOException("Título, plataforma y género son obligatorios (id " + id + ").");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IOException("El precio debe ser un número finito no negativo (id " + id + ").");
        }
        if (stock < 0) {
            throw new IOException("El stock no puede ser negativo (id " + id + ").");
        }
        return new Videojuego(id, titulo, plataforma, genero, precio, stock, codigoProveedor);
    }

    private String obtenerTexto(Element padre, String etiqueta, boolean obligatorio) throws IOException {
        StringBuilder valor = new StringBuilder();
        NodeList hijos = padre.getChildNodes();
        int coincidencias = 0;
        for (int i = 0; i < hijos.getLength(); i++) {
            Node hijo = hijos.item(i);
            if (hijo instanceof Element && etiqueta.equals(hijo.getNodeName())) {
                coincidencias++;
                valor.append(hijo.getTextContent());
            }
        }
        if (coincidencias > 1) {
            throw new IOException("El elemento <" + etiqueta + "> aparece más de una vez.");
        }
        if (coincidencias == 0 && obligatorio) {
            throw new IOException("Falta el elemento obligatorio <" + etiqueta + ">.");
        }
        return valor.toString().trim();
    }

    private int entero(String campo, String valor) throws IOException {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IOException("El campo '" + campo + "' no es un entero válido: '" + valor + "'.", e);
        }
    }

    private double decimal(String valor) throws IOException {
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IOException("El precio no es un número válido: '" + valor + "'.", e);
        }
    }

    private void agregarTexto(Document document, Element padre, String etiqueta, String valor) {
        Element elemento = document.createElement(etiqueta);
        elemento.setTextContent(valor == null ? "" : valor);
        padre.appendChild(elemento);
    }
}
