package gestores;

import promehub.PromeHubStore;
import promehub.Videojuego;

import java.util.List;

public class PromeHubManager {
    private final PromeHubStore store;

    public PromeHubManager() {
        this.store = new PromeHubStore();
    }

    public void cargarDesdeCSV(String rutaArchivo) {
        store.cargarDesdeCSV(rutaArchivo);
    }

    public void exportarACSV(String rutaArchivo) {
        store.exportarACSV(rutaArchivo);
    }

    public void exportarAXML(String rutaArchivo) {
        store.exportarAXML(rutaArchivo);
    }

    public void cargarDesdeXML(String rutaArchivo) {
        store.cargarDesdeXML(rutaArchivo);
    }

    public void mostrarCatalogo() {
        store.mostrarCatalogo();
    }

    public List<Videojuego> buscarVideojuego(String criterio) {
        return store.buscarVideojuego(criterio);
    }

    public void mostrarInformacionFicheros(String... rutas) {
        store.mostrarInformacionFicheros(rutas);
    }
}