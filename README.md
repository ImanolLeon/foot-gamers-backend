# FootGamer's — Backend

API REST para gestionar usuarios, jugadores, torneos, partidos, grupos, bandos, categorías y trofeos. Incluye autenticación JWT, acceso con Google y Discord e imágenes de perfil.

## Tecnologías

- Java 25 y Spring Boot 4.1.1.
- Spring Data JPA y PostgreSQL.
- Spring Security, BCrypt, JWT (JJWT 0.12.6) y OAuth2.
- Maven Wrapper y Docker.

## Requisitos

JDK 25 con `JAVA_HOME` configurado, PostgreSQL disponible e Internet para descargar las dependencias. Maven se ejecuta mediante el wrapper incluido. Docker es opcional.

## Inicio local

Ejecuta los comandos desde la raíz del repositorio.

### 1. Crear la base de datos

Desde un cliente PostgreSQL:

```sql
CREATE DATABASE footgamers;
```

Concede permisos al usuario de la aplicación sobre esa base. La configuración actual utiliza `ddl-auto: update`: Hibernate puede crear o modificar tablas al arrancar.

### 2. Configurar el entorno

Si aún no tienes `.env`, copia la plantilla:

```powershell
Copy-Item .env.example .env
```

Completa tus valores y agrega las variables de imágenes, actualmente ausentes en `.env.example`:

```dotenv
DB_USER=tu_usuario
DB_PASSWORD=tu_contrasena
DB_URL=jdbc:postgresql://localhost:5432/footgamers
JWT_SECRET=REEMPLAZAR_POR_UN_SECRETO_ALEATORIO
JWT_EXPIRATION_MS=86400000
IMAGENES_DIR=uploads
IMAGENES_FORMATOS=png,jpg,webp
FRONTEND_URL=http://localhost:5173
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
DISCORD_CLIENT_ID=
DISCORD_CLIENT_SECRET=
```

`JWT_SECRET` se interpreta como texto UTF-8 y necesita al menos 32 bytes. Reemplaza el marcador por un secreto aleatorio propio. La expiración se expresa en milisegundos; el ejemplo equivale a 24 horas. No publiques `.env`.

Para utilizar solo el login local, conserva las variables OAuth declaradas y vacías. El código omite los proveedores cuyo identificador de cliente está vacío.

### 3. Iniciar la aplicación

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

API local: `http://localhost:8080`. Ejecuta desde la raíz para que Spring encuentre `.env` y resuelva correctamente la carpeta relativa de imágenes.

## Autenticación

| Método | Ruta | Función |
| --- | --- | --- |
| POST | `/auth/register` | Crear una cuenta con rol `JUGADOR` |
| POST | `/auth/login` | Iniciar sesión y obtener JWT |
| GET | `/auth/me` | Consultar el usuario autenticado |
| PUT | `/auth/me` | Actualizar el perfil |
| PUT | `/auth/me/contrasena` | Cambiar la contraseña |

Para las rutas privadas envía:

```http
Authorization: Bearer <token>
```

Los roles son `ADMINISTRADOR` y `JUGADOR`. Consulta los controladores y servicios para conocer los permisos específicos de cada operación.

### Cuenta inicial de desarrollo

`DataInitializer` crea el administrador `imanol` con contraseña inicial `123456` si no existe, y completa su perfil de jugador cuando falta. Cambia esa contraseña y adapta el inicializador antes de exponer la aplicación: las credenciales iniciales están incorporadas al código.

## Recursos de la API

Las rutas directas del backend no llevan el prefijo `/api`.

| Ruta base | Recurso |
| --- | --- |
| `/usuarios` | Usuarios y avatares |
| `/jugadores` | Jugadores y consultas de historial |
| `/torneos` | Torneos, inscripciones, grupos y llaves |
| `/partidos` | Partidos, confirmaciones y ganador |
| `/participaciones` | Participaciones en partidos |
| `/grupos` | Clasificación de grupos |
| `/bando` | Bandos |
| `/categorias` | Categorías |
| `/trofeos` | Trofeos |

La carpeta [Collection](Collection/) contiene solicitudes de prueba. Abre [opencollection.yml](Collection/opencollection.yml) con una versión compatible de Bruno, ajusta `baseUrl` y las credenciales y ejecuta `Login > login` para cargar el token de sesión.

## OAuth2 y frontend

El frontend asociado es `foot_gamers`. En desarrollo utiliza `http://localhost:5173`, origen configurado en CORS y en el ejemplo de `FRONTEND_URL`.

Completa el identificador y secreto de cada proveedor que quieras habilitar. Para acceso directo al backend local, registra estos callbacks:

- Google: `http://localhost:8080/login/oauth2/code/google`.
- Discord: `http://localhost:8080/login/oauth2/code/discord`.

El flujo comienza en `/oauth2/authorization/google` o `/oauth2/authorization/discord`. Con un proxy o dominio público, adapta los callbacks a la URL externa utilizada realmente por el flujo.

El frontend puede usar `VITE_API_URL=/api` y `VITE_PROXY_TARGET=http://localhost:8080`. Vite elimina el prefijo: `/api/auth/login` se reenvía al backend como `/auth/login`.

## Imágenes de perfil

`PUT /usuarios/{id}/imagen` recibe un formulario multipart con el campo `archivo`. `GET /usuarios/{id}/imagen` permite consultar la imagen y está configurado como público.

El almacenamiento reconoce PNG, JPEG y WebP según `IMAGENES_FORMATOS`, valida la firma inicial y limita cada archivo a 2 MB. Los nombres los genera el servidor y los archivos se guardan en `IMAGENES_DIR`.

## Pruebas y compilación

```powershell
# Todas las pruebas
.\mvnw.cmd test

# Pruebas unitarias de autenticación y JWT
.\mvnw.cmd "-Dtest=AuthServiceRegistroTest,AuthServicePerfilTest,JwtAuthenticationFilterTest" test

# Compilar, ejecutar pruebas y generar el JAR
.\mvnw.cmd clean package

# Ejecutar el artefacto
java -jar target/FootGamers-0.0.1-SNAPSHOT.jar
```

En Linux/macOS sustituye `.\mvnw.cmd` por `./mvnw`. La prueba de carga del contexto requiere la configuración de la aplicación y PostgreSQL accesible. Usa una base de pruebas: el arranque ejecuta el inicializador de datos.

## Docker

El archivo se llama `dockerfile` y se indica explícitamente:

```powershell
docker build -f dockerfile -t footgamers-api .
docker run --name footgamers-api --env-file .env -e IMAGENES_DIR=/data/uploads -p 8080:8080 -v footgamers-uploads:/data/uploads footgamers-api
```

El volumen conserva los avatares al recrear el contenedor. Ajusta `DB_URL`: `localhost` dentro del contenedor apunta al propio contenedor. En Docker Desktop puedes usar `host.docker.internal` para PostgreSQL en el equipo anfitrión; entre contenedores utiliza el nombre del servicio en una red compartida.

La construcción Docker omite las pruebas con `-DskipTests`; ejecútalas por separado antes de publicar la imagen.

## Estructura

```text
src/main/java/org/example/footgamers/
├── config/              # Inicialización
├── controller/          # Endpoints HTTP
├── dto/                 # Solicitudes y respuestas
├── entities/            # Entidades JPA y enumeraciones
├── exception/           # Manejo de errores
├── repository/          # Acceso a datos
├── security/            # JWT y OAuth2
└── service/
    ├── almacen/         # Imágenes
    ├── implementacion/  # Lógica de negocio
    └── reglas/          # Interfaces
```

Configuración: `src/main/resources/application.yaml`. Pruebas: `src/test/java`.
