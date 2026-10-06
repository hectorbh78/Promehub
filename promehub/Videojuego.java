package promehub;

public class Videojuego {
    private int id;
    private String titulo;
    private String plataforma;
    private String genero;
    private double precio;
    private int stock;
    private String codigoProveedor;

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
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCodigoProveedor() { return codigoProveedor; }
    public void setCodigoProveedor(String codigoProveedor) { this.codigoProveedor = codigoProveedor; }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT,
                "[%d] %-32s | %-12s | %-12s | %8.2f EUR | stock %3d | prov. %s",
                id, titulo, plataforma, genero, precio, stock,
                codigoProveedor == null || codigoProveedor.isEmpty() ? "-" : codigoProveedor);
    }
}