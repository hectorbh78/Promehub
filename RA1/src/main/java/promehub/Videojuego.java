package promehub;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import java.util.Locale;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"titulo", "plataforma", "genero", "precio", "stock"})
public class Videojuego {

    @XmlAttribute(name = "id")
    private int id;

    @XmlElement(name = "titulo")
    private String titulo;

    @XmlElement(name = "plataforma")
    private String plataforma;

    @XmlElement(name = "genero")
    private String genero;

    @XmlElement(name = "precio")
    private double precio;

    @XmlElement(name = "stock")
    private int stock;

    @XmlTransient
    private String codigoProveedor;

    // JAXB necesita un constructor sin argumentos.
    public Videojuego() {
    }

    public Videojuego(int id, String titulo, String plataforma, String genero,
                      double precio, int stock, String codigoProveedor) {
        this.id = id;
        this.titulo = titulo;
        this.plataforma = plataforma;
        this.genero = genero;
        this.precio = precio;
        this.stock = stock;
        this.codigoProveedor = codigoProveedor;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getPlataforma() { return plataforma; }
    public String getGenero() { return genero; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCodigoProveedor() { return codigoProveedor; }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "[%d] %-32s | %-12s | %-12s | %8.2f EUR | stock %3d | prov. %s",
                id, titulo, plataforma, genero, precio, stock,
                codigoProveedor == null || codigoProveedor.isEmpty() ? "-" : codigoProveedor);
    }
}
