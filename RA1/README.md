# 🎮 PromeHub Data Exchange

Aplicación Java de consola que convierte el catálogo de videojuegos de PromeHub entre **CSV** (PHManager) y **XML** (PHStore), sin conectar las dos aplicaciones directamente.

```
CSV → Java → XML
XML → Java → CSV
```

Práctica de Acceso a Datos (DAM) · U1 · Persistencia en ficheros · RA1 · Curso 2026-27

## Requisitos

- JDK 17 o superior
- Maven (descarga solo `jakarta.xml.bind-api` y `jaxb-runtime`, definidos en el `pom.xml`)

## Ejecución

```bash
mvn clean package
java -jar target/promehub-data-exchange.jar
```

Hay que ejecutarlo desde la raíz del proyecto, porque las rutas por defecto (`datos/...`) son relativas.
Los `.class` se generan solo en `target/` (carpeta ignorada por Git); no se guardan compilados en el repositorio.

## Estructura del proyecto

```
RA1/
├── pom.xml
├── README.md
├── datos/
│   ├── videojuegos.csv            ← CSV de entrada (PHManager)
│   ├── videojuegos_erroneo.csv    ← CSV con registros incorrectos (para las pruebas)
│   └── catalogo.xml               ← XML de intercambio (PHStore)
└── src/main/java/promehub/
    ├── App.java                   ← menú por consola y control del flujo
    ├── Videojuego.java            ← modelo de datos (con anotaciones JAXB)
    ├── Catalogo.java              ← elemento raíz <catalogo> (JAXB)
    ├── GestorCSV.java             ← lectura secuencial y escritura del CSV
    └── GestorXML.java             ← conversión objetos ↔ XML con JAXB
```

| Clase | Responsabilidad |
|---|---|
| `App` | Muestra el menú, lee las opciones y llama a los gestores. Busca y muestra información de ficheros. |
| `Videojuego` | Representa un videojuego. Define cómo se mapea a XML. |
| `Catalogo` | Contenedor de la lista de videojuegos; es la raíz del XML. |
| `GestorCSV` | Lee el CSV de forma secuencial (`BufferedReader`), valida cada registro y exporta a CSV. |
| `GestorXML` | `Marshaller` (objetos → XML) y `Unmarshaller` (XML → objetos). |

## Flujo de datos

- **CSV → Java → XML:** `GestorCSV.cargar` lee línea a línea, crea un `Videojuego` por registro válido y los guarda en una lista → `GestorXML.exportar` mete la lista en un `Catalogo` y JAXB genera el XML.
- **XML → Java → CSV:** `GestorXML.cargar` usa el `Unmarshaller` para reconstruir el `Catalogo` y su lista → `GestorCSV.exportar` escribe el CSV.

## Menú

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

## Modelo de datos

Cada `Videojuego` tiene: `id`, `titulo`, `plataforma`, `genero`, `precio`, `stock` y `codigoProveedor`.

> `codigoProveedor` es interno de PHManager y **no aparece en el XML** (`@XmlTransient`).
> Por eso, al generar un CSV a partir de un XML, esa columna sale vacía.

## Formato del XML

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

## Anotaciones JAXB

| Anotación | Dónde | Uso |
|---|---|---|
| `@XmlRootElement(name = "catalogo")` | `Catalogo` | Elemento raíz `<catalogo>` |
| `@XmlAccessorType(XmlAccessType.FIELD)` | `Catalogo`, `Videojuego` | JAXB mapea los campos de la clase directamente |
| `@XmlAttribute` | `Videojuego.id` | `id` como atributo XML |
| `@XmlElement` | `Videojuego` (resto de campos) y `Catalogo.videojuegos` | Campos como elementos XML; la lista como `<videojuego>` repetido |
| `@XmlTransient` | `Videojuego.codigoProveedor` | Excluye el campo del XML |
| `@XmlType(propOrder = …)` | `Videojuego` | Fija el orden de los elementos hijos |

## Excepciones controladas

Todos los mensajes están en español e indican qué ha fallado:

| Situación | Dónde se gestiona |
|---|---|
| Fichero inexistente | `FileNotFoundException` en `GestorCSV` / `GestorXML` |
| Error de lectura / escritura | `IOException` en `App` |
| Registro CSV incorrecto (nº de campos, comillas, id repetido…) | `IllegalArgumentException` por línea: se descarta y se informa |
| Error de conversión numérica | `NumberFormatException` → mensaje del campo afectado |
| Error de procesamiento XML | `JAXBException` → `IOException` con mensaje claro |
| Opción de menú incorrecta | `NumberFormatException` y `default` del `switch` |

## Pruebas

Ejecutar desde la raíz del proyecto. Marcar el resultado obtenido tras cada ejecución.

| Nº | Prueba | Cómo se ejecuta | Resultado esperado | Resultado obtenido |
|---|---|---|---|---|
| 1 | Cargar CSV correctamente | Opción 1, Enter (ruta por defecto) | "5 registros leídos, 5 válidos, 0 descartados" | ☐ |
| 2 | Mostrar el catálogo | Opción 2 | Se listan los 5 videojuegos | ☐ |
| 3 | Generar el XML | Opción 3 | Se crea `datos/catalogo.xml` con 5 `<videojuego>` | ☐ |
| 4 | `codigoProveedor` no aparece en el XML | Abrir `datos/catalogo.xml` | Ninguna etiqueta `<codigoProveedor>` | ☐ |
| 5 | Cargar nuevamente el XML | Opción 4, Enter | "XML cargado correctamente: 5 videojuegos" | ☐ |
| 6 | Generar un CSV a partir del XML | Opción 5, Enter | Se crea `datos/catalogo_exportado.csv` con 5 filas (`codigoProveedor` vacío) | ☐ |
| 7 | Cargar un fichero que no existe | Opción 1 y ruta `no_existe.csv` | "ERROR: El fichero CSV 'no_existe.csv' no existe." | ☐ |
| 8 | Registro CSV incorrecto | Opción 1 y ruta `datos/videojuegos_erroneo.csv` | 2 válidos, 3 descartados, con el motivo de cada línea | ☐ |
| 9 | Buscar por id y por título | Opción 6 con `3` y con `gta` | Se muestran Minecraft y GTA V | ☐ |
| 10 | Información de ficheros | Opción 7 | Ruta, existencia y tamaño de los 3 ficheros | ☐ |
| 11 | Opción de menú incorrecta | Escribir `9` y `abc` | Mensaje de opción no válida; el menú se repite | ☐ |
| 12 | XML mal formado | Editar `catalogo.xml` (p. ej. `id="abc"`) y opción 4 | "ERROR al procesar el XML: …" sin cerrar la app | ☐ |

## Equipo

| Integrante | Rol |
|---|---|
| _Agustín_ | Team Leader |
| _Sergio_ | Programador experto |
| _Hector_ | QA |
| _JP_ | Documentación |
