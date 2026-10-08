package promehub;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Convierte el catálogo entre objetos Java y XML utilizando JAXB.
public class GestorXML {

    // Objeto → XML (marshalling)
    public void exportar(Path ruta, List<Videojuego> videojuegos) throws IOException {
        try {
            JAXBContext contexto = JAXBContext.newInstance(Catalogo.class);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            Path carpeta = ruta.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            try (OutputStream salida = Files.newOutputStream(ruta)) {
                marshaller.marshal(new Catalogo(videojuegos), salida);
            }
        } catch (JAXBException e) {
            throw new IOException("No se pudo generar el XML con JAXB: " + mensaje(e), e);
        }
    }

    // XML → objeto (unmarshalling)
    public List<Videojuego> cargar(Path ruta) throws IOException {
        if (!Files.exists(ruta)) {
            throw new FileNotFoundException("El fichero XML '" + ruta + "' no existe.");
        }
        if (!Files.isRegularFile(ruta)) {
            throw new IOException("'" + ruta + "' no es un fichero válido.");
        }

        try (InputStream entrada = Files.newInputStream(ruta)) {
            JAXBContext contexto = JAXBContext.newInstance(Catalogo.class);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            // Por defecto JAXB ignora valores no válidos (p. ej. un id no numérico);
            // devolver false obliga a lanzar la excepción ante cualquier problema.
            unmarshaller.setEventHandler(evento -> false);

            Catalogo catalogo = (Catalogo) unmarshaller.unmarshal(origenSeguro(entrada));
            validar(catalogo.getVideojuegos());
            return catalogo.getVideojuegos();
        } catch (JAXBException e) {
            throw new IOException("El XML no es válido o no se pudo procesar: " + mensaje(e), e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("No se pudo preparar el lector XML: " + e.getMessage(), e);
        }
    }

    // Lector XML que no permite DTD ni entidades externas (evita ataques XXE).
    private Source origenSeguro(InputStream entrada) throws ParserConfigurationException, SAXException {
        SAXParserFactory fabrica = SAXParserFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return new SAXSource(fabrica.newSAXParser().getXMLReader(), new InputSource(entrada));
    }

    // Comprueba las reglas básicas de cada videojuego leído del XML.
    private void validar(List<Videojuego> videojuegos) throws IOException {
        Set<Integer> ids = new HashSet<>();
        for (Videojuego v : videojuegos) {
            if (v.getId() <= 0) {
                throw new IOException("El id debe ser mayor que cero (encontrado: " + v.getId() + ").");
            }
            if (!ids.add(v.getId())) {
                throw new IOException("El XML contiene el id repetido " + v.getId() + ".");
            }
            if (vacio(v.getTitulo()) || vacio(v.getPlataforma()) || vacio(v.getGenero())) {
                throw new IOException("Título, plataforma y género son obligatorios (id " + v.getId() + ").");
            }
            if (!Double.isFinite(v.getPrecio()) || v.getPrecio() < 0) {
                throw new IOException("El precio debe ser un número no negativo (id " + v.getId() + ").");
            }
            if (v.getStock() < 0) {
                throw new IOException("El stock no puede ser negativo (id " + v.getId() + ").");
            }
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Obtiene el mensaje más útil de una excepción de JAXB.
    private String mensaje(JAXBException e) {
        Throwable causa = e.getLinkedException() != null ? e.getLinkedException() : e;
        return causa.getMessage() != null ? causa.getMessage() : e.toString();
    }
}
