package promehub;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "catalogo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Catalogo {

    @XmlElement(name = "videojuego")
    private List<Videojuego> videojuegos = new ArrayList<>();

    // JAXB necesita un constructor sin argumentos.
    public Catalogo() {
    }

    public Catalogo(List<Videojuego> videojuegos) {
        this.videojuegos = new ArrayList<>(videojuegos);
    }

    public List<Videojuego> getVideojuegos() {
        return videojuegos;
    }
}
