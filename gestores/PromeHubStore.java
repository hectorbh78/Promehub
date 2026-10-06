package gestores;

import promehub.Videojuego;

import java.util.List;

public class PromeHubStore {
    private final promehub.PromeHubStore delegate = new promehub.PromeHubStore();

    public List<Videojuego> getCatalogo() {
        return delegate.getCatalogo();
    }

    public void cargarDesdeCSV(String rutaArchivo) {
        delegate.cargarDesdeCSV(rutaArchivo);
    }

    public void exportarACSV(String rutaArchivo) {
        delegate.exportarACSV(rutaArchivo);
    }

    public void exportarAXML(String rutaArchivo) {
        delegate.exportarAXML(rutaArchivo);
    }

    public void cargarDesdeXML(String rutaArchivo) {
        delegate.cargarDesdeXML(rutaArchivo);
    }

    public void mostrarCatalogo() {
        delegate.mostrarCatalogo();
    }

    public List<Videojuego> buscarVideojuego(String criterio) {
        return delegate.buscarVideojuego(criterio);
    }

    public void mostrarInformacionFicheros(String... rutas) {
        delegate.mostrarInformacionFicheros(rutas);
    }
}