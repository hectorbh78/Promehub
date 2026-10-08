# 🎮 PromeHub Data Exchange

PromeHub tiene dos aplicaciones que no se entienden entre sí: **PHManager** recibe los videojuegos en **CSV** y **PHStore** solo trabaja con **XML**. Como no queremos conectarlas directamente, hemos hecho una pequeña aplicación Java de consola que hace de intermediaria y traduce en los dos sentidos:

```
CSV → Java → XML
XML → Java → CSV
```

Práctica de Acceso a Datos (DAM) · U1 · Persistencia en ficheros · RA1 · Curso 2026-27

## Cómo ejecutarla

Necesitas JDK 17 o superior y Maven. Maven se encarga de descargar JAXB (`jakarta.xml.bind-api` y `jaxb-runtime`), que ya están en el `pom.xml`.

```bash
mvn clean package
java -jar target/promehub-data-exchange.jar
```

Lánzala siempre desde la carpeta raíz del proyecto, porque las rutas por defecto (`datos/...`) son relativas.

Una cosa más: en el repositorio no guardamos ningún `.class`. Todo lo compilado se genera en `target/`, que Git ignora.

## Qué hay en el proyecto

```
RA1/
├── pom.xml
├── README.md
├── datos/
│   ├── videojuegos.csv            ← CSV de entrada (el que manda PHManager)
│   ├── videojuegos_erroneo.csv    ← CSV con registros mal puestos, para probar errores
│   └── catalogo.xml               ← XML de intercambio (el que lee PHStore)
└── src/main/java/promehub/
    ├── App.java                   ← el menú y el control del flujo
    ├── Videojuego.java            ← el modelo de datos, con sus anotaciones JAXB
    ├── Catalogo.java              ← la raíz <catalogo> del XML
    ├── GestorCSV.java             ← lee (en secuencial) y escribe el CSV
    └── GestorXML.java             ← pasa de objetos a XML y al revés con JAXB
```

Cada clase tiene una única responsabilidad:

- **App**: enseña el menú, lee lo que escribe el usuario y llama a los gestores. También se encarga de buscar y de mostrar la información de los ficheros.
- **Videojuego**: representa un videojuego y define cómo se convierte a XML.
- **Catalogo**: guarda la lista de videojuegos y es el elemento raíz del XML.
- **GestorCSV**: lee el CSV línea a línea con `BufferedReader`, valida cada registro y también exporta a CSV.
- **GestorXML**: usa el `Marshaller` para pasar de objetos a XML y el `Unmarshaller` para volver.

## Cómo viajan los datos

- **CSV → Java → XML.** `GestorCSV` lee el fichero de arriba abajo, crea un `Videojuego` por cada registro válido y los guarda en una lista. Después `GestorXML` mete esa lista en un `Catalogo` y JAXB escribe el XML.
- **XML → Java → CSV.** `GestorXML` lee el XML, y el `Unmarshaller` reconstruye el `Catalogo` con su lista de videojuegos. Por último, `GestorCSV` escribe esa lista en un CSV.

## El menú

```
1. Cargar catálogo desde CSV
2. Mostrar catálogo
3. Exportar catálogo a XML
4. Cargar catálogo desde XML
5. Exportar catálogo a CSV
6. Buscar videojuego (por id o título)
7. Información de ficheros (existencia, tamaño, ruta)
0. Salir
```

## Los datos

Cada videojuego tiene `id`, `titulo`, `plataforma`, `genero`, `precio`, `stock` y `codigoProveedor`.

Ojo con `codigoProveedor`: es información interna de PHManager y **no debe salir en el XML**, así que lo marcamos con `@XmlTransient`. La consecuencia es que, si generas un CSV a partir de un XML, esa columna sale vacía (el XML no la trae).

El XML queda así:

```xml
<catalogo>
    <videojuego id="1">
        <titulo>Cyberpunk 2077</titulo>
        <plataforma>PC</plataforma>
        <genero>RPG</genero>
        <precio>39.99</precio>
        <stock>12</stock>
    </videojuego>
</catalogo>
```

## Las anotaciones JAXB que usamos

| Anotación | Dónde | Para qué |
|---|---|---|
| `@XmlRootElement(name = "catalogo")` | `Catalogo` | Que `<catalogo>` sea el elemento raíz |
| `@XmlAccessorType(XmlAccessType.FIELD)` | `Catalogo` y `Videojuego` | Que JAXB lea directamente los campos de la clase |
| `@XmlAttribute` | `Videojuego.id` | Que el `id` salga como atributo (`<videojuego id="1">`) |
| `@XmlElement` | Resto de campos de `Videojuego` y la lista de `Catalogo` | Que salgan como elementos; la lista, como varios `<videojuego>` |
| `@XmlTransient` | `Videojuego.codigoProveedor` | Dejar ese campo fuera del XML |
| `@XmlType(propOrder = …)` | `Videojuego` | Fijar el orden de los elementos hijos |

## Qué pasa cuando algo falla

La aplicación no se cae: avisa en español de qué ha ido mal y vuelve al menú.

| Problema | Cómo lo gestionamos |
|---|---|
| El fichero no existe | `FileNotFoundException` en `GestorCSV` y `GestorXML` |
| Error al leer o escribir | `IOException`, capturada en `App` |
| Registro CSV incorrecto (campos de menos, comillas mal cerradas, id repetido…) | Se descarta esa línea, se dice cuál y por qué, y se sigue con las demás |
| Un número que no es número | `NumberFormatException`, con el nombre del campo afectado |
| XML roto o con datos inválidos | `JAXBException`, convertida en un `IOException` con un mensaje claro |
| Opción de menú incorrecta | `NumberFormatException` y el `default` del `switch` |

## Pruebas

Se ejecutan desde la raíz del proyecto. ✅ significa que la hemos probado y sale bien; ⏳ significa que falta ejecutarla porque necesita JAXB (que se descarga con Maven). Cuando se pruebe, hay que cambiar el ⏳ por el resultado real.

| Nº | Qué probamos | Cómo | Qué esperamos | Qué ha pasado |
|---|---|---|---|---|
| 1 | Cargar el CSV | Opción 1 y Enter (ruta por defecto) | "5 registros leídos, 5 válidos, 0 descartados" | ✅ Correcto: «CSV procesado: 5 registros leídos, 5 válidos, 0 descartados.» |
| 2 | Mostrar el catálogo | Opción 2 | Se listan los 5 videojuegos | ✅ Correcto: «--- CATÁLOGO (5 videojuegos) ---» con los 5 juegos |
| 3 | Generar el XML | Opción 3 | Se crea `datos/catalogo.xml` con 5 `<videojuego>` | ⏳ Pendiente: necesita JAXB (`mvn clean package`) |
| 4 | Que `codigoProveedor` no esté en el XML | Abrir `datos/catalogo.xml` | Ninguna etiqueta `<codigoProveedor>` | ⏳ Pendiente: se mira en el XML generado en la prueba 3 |
| 5 | Volver a cargar el XML | Opción 4 y Enter | "XML cargado correctamente: 5 videojuegos" | ⏳ Pendiente: necesita JAXB |
| 6 | Generar un CSV desde el XML | Opción 5 y Enter | Se crea `datos/catalogo_exportado.csv` con 5 filas (`codigoProveedor` vacío) | ⏳ Pendiente: depende de la prueba 5 |
| 7 | Cargar un fichero que no existe | Opción 1 y ruta `no_existe.csv` | "ERROR: El fichero CSV 'no_existe.csv' no existe." | ✅ Correcto: sale ese mensaje y se vuelve al menú |
| 8 | Registros CSV incorrectos | Opción 1 y ruta `datos/videojuegos_erroneo.csv` | 2 válidos y 3 descartados, con el motivo de cada línea | ✅ Correcto: «5 registros leídos, 2 válidos, 3 descartados»; línea 3, precio 'abc' no válido; línea 4, 5 campos en vez de 7; línea 5, stock 'seis' no válido |
| 9 | Buscar por id y por título | Opción 6 con `3` y con `gta` | Salen Minecraft y GTA V | ✅ Correcto: con `3` sale Minecraft y con `gta` sale GTA V |
| 10 | Información de ficheros | Opción 7 | Ruta, si existe y tamaño de los 3 ficheros | ✅ Correcto: ruta absoluta, «Existe: sí/no» y tamaño (283 bytes el CSV de entrada) |
| 11 | Opción de menú incorrecta | Escribir `9` y `abc` | Aviso de opción no válida y el menú vuelve a salir | ✅ Correcto: «Opción no válida: elige un número entre 0 y 7.» y «debes escribir un número entre 0 y 7.» |
| 12 | XML mal formado | Cambiar algo en `catalogo.xml` (por ejemplo `id="abc"`) y opción 4 | "ERROR al procesar el XML: …" sin que se cierre la app | ⏳ Pendiente: necesita JAXB |

## Equipo

| Integrante | Rol |
|---|---|
| _Agustín_ | Team Leader |
| _Sergio_ | Programador experto |
| _Hector_ | QA |
| _JP_ | Documentación |
