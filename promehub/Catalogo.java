package promehub;

import java.util.ArrayList;
import java.util.List;

public class Catalogo {
    private List<Videojuego> videojuegos = new ArrayList<>();

    public Catalogo() {
    }

    public Catalogo(List<Videojuego> videojuegos) {
        this.videojuegos = new ArrayList<>(videojuegos);
    }

    public List<Videojuego> getVideojuegos() {
        return videojuegos;
    }

    public void setVideojuegos(List<Videojuego> videojuegos) {
        this.videojuegos = videojuegos;
    }
}