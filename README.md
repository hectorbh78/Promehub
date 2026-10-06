# 🎮 PromeHub Data Exchange

Aplicación Java de consola que convierte el catálogo de videojuegos de PromeHub entre **CSV** (PHManager) y **XML** (PHStore), sin conectar las dos aplicaciones directamente.

```
CSV → Java → XML
XML → Java → CSV
```

Práctica de Acceso a Datos (DAM) · U1 · Persistencia en ficheros · Curso 2026-27

## Requisitos

- JDK 17 o superior
- Maven (con las dependencias de JAXB, `jakarta.xml.bind-api` y `jaxb-runtime`)

## Ejecución

```bash
mvn clean package
java -jar target/promehub-data-exchange.jar
```

## Menú

```
1. Cargar catálogo desde CSV
2. Mostrar catálogo
3. Exportar catálogo a XML
4. Cargar catálogo desde XML
5. Exportar catálogo a CSV
6. Buscar videojuego (por id o título)
7. Información de ficheros
0. Salir
```

## Modelo de datos

Cada `Videojuego` tiene: `id`, `titulo`, `plataforma`, `genero`, `precio`, `stock` y `codigoProveedor`.

> `codigoProveedor` es interno de PHManager y **no aparece en el XML** (`@XmlTransient`).

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

| Anotación | Uso |
|---|---|
| `@XmlRootElement` | Elemento raíz `<catalogo>` |
| `@XmlAccessorType` | Mapeo por atributos de la clase |
| `@XmlAttribute` | `id` como atributo XML |
| `@XmlElement` | Resto de campos como elementos |
| `@XmlTransient` | Excluye `codigoProveedor` |

## Errores controlados

Con mensajes en español, la aplicación gestiona: fichero inexistente, errores de lectura, registros CSV incorrectos, errores de conversión numérica, errores de XML y opciones de menú inválidas.

## Pruebas

| Prueba | Resultado |
|---|---|
| Cargar CSV correctamente | ☐ |
| Mostrar el catálogo | ☐ |
| Generar el XML | ☐ |
| `codigoProveedor` no aparece en el XML | ☐ |
| Cargar de nuevo el XML | ☐ |
| Generar CSV desde el XML | ☐ |
| Cargar un fichero que no existe | ☐ |
| Registro CSV incorrecto | ☐ |

## Equipo

| Integrante | Rol |
|---|---|
| _Agustín_ | Team Leader |
| _Sergio_ | Programador experto |
| _Hector_ | QA |
| _JP_ | Documentación |

