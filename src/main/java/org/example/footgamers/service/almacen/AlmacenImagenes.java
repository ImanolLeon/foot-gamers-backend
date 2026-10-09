package org.example.footgamers.service.almacen;

import lombok.extern.slf4j.Slf4j;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda y lee las imagenes de usuario en el sistema de archivos.
 * El nombre del archivo lo genera el servidor y la extension se deduce de los
 * bytes reales, nunca del nombre ni del Content-Type que envia el cliente:
 * asi se evita el path traversal (../../) y la extension suplantada.
 */
@Slf4j
@Component
public class AlmacenImagenes {

    private static final String PREFIJO = "usuario-";
    private static final Map<String, String> ALIAS_FORMATO = Map.of("jpeg", "jpg");
    private static final int TAMANO_CABECERA = 12;

    private final Path directorioBase;
    private final List<String> formatosPermitidos;
    private final long tamanoMaximoBytes;

    public AlmacenImagenes(@Value("${almacen.imagenes.directorio}") String directorio,
                           @Value("${almacen.imagenes.formatos-permitidos}") String formatos,
                           @Value("${almacen.imagenes.tamano-maximo-bytes}") long tamanoMaximoBytes) {
        this.directorioBase = Paths.get(directorio).toAbsolutePath().normalize();
        this.formatosPermitidos = Arrays.stream(formatos.split(","))
                .map(formato -> formato.trim().toLowerCase(Locale.ROOT))
                .filter(formato -> !formato.isEmpty())
                .map(formato -> ALIAS_FORMATO.getOrDefault(formato, formato))
                .distinct()
                .toList();
        this.tamanoMaximoBytes = tamanoMaximoBytes;
    }

    /**
     * @return la ruta relativa que hay que persistir en Usuario.imagen
     */
    public String guardar(MultipartFile archivo, Long usuarioId) {
        validarArchivo(archivo);
        String formato = detectarFormato(archivo);
        String nombre = PREFIJO + usuarioId + "-" + UUID.randomUUID() + "." + formato;
        try {
            Files.createDirectories(directorioBase);
            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, directorioBase.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("No se pudo guardar la imagen del usuario {}", usuarioId, e);
            throw new ApiException(MensajeException.IMAGEN_NO_GUARDADA, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return nombre;
    }

    public ImagenGuardada leer(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            throw ApiException.noEncontrado(MensajeException.IMAGEN_NO_ENCONTRADA);
        }
        Path destino = resolverSeguro(rutaRelativa);
        if (!Files.isRegularFile(destino)) {
            throw ApiException.noEncontrado(MensajeException.IMAGEN_NO_ENCONTRADA);
        }
        try {
            return new ImagenGuardada(Files.readAllBytes(destino), contentTypeDe(destino));
        } catch (IOException e) {
            log.error("No se pudo leer la imagen {}", rutaRelativa, e);
            throw new ApiException(MensajeException.IMAGEN_NO_LEIDA, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolverSeguro(rutaRelativa));
        } catch (IOException e) {
            log.warn("No se pudo borrar la imagen {}", rutaRelativa, e);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.solicitudInvalida(MensajeException.IMAGEN_VACIA);
        }
        if (archivo.getSize() > tamanoMaximoBytes) {
            throw ApiException.solicitudInvalida(
                    String.format(MensajeException.IMAGEN_DEMASIADO_GRANDE, tamanoMaximoBytes));
        }
    }

    private String detectarFormato(MultipartFile archivo) {
        String formato = formatoDe(leerCabecera(archivo));
        if (formato == null || !formatosPermitidos.contains(formato)) {
            throw ApiException.solicitudInvalida(
                    String.format(MensajeException.FORMATO_IMAGEN_INVALIDO, formatosComoTexto()));
        }
        return formato;
    }

    private String formatosComoTexto() {
        return String.join(", ", formatosPermitidos);
    }

    private byte[] leerCabecera(MultipartFile archivo) {
        byte[] cabecera = new byte[TAMANO_CABECERA];
        try (InputStream entrada = archivo.getInputStream()) {
            int leidos = 0;
            while (leidos < cabecera.length) {
                int leidosAhora = entrada.read(cabecera, leidos, cabecera.length - leidos);
                if (leidosAhora < 0) {
                    break;
                }
                leidos += leidosAhora;
            }
            return Arrays.copyOf(cabecera, leidos);
        } catch (IOException e) {
            log.warn("No se pudo leer la cabecera del archivo subido", e);
            throw ApiException.solicitudInvalida(
                    String.format(MensajeException.FORMATO_IMAGEN_INVALIDO, formatosComoTexto()));
        }
    }

    private String formatoDe(byte[] b) {
        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private String contentTypeDe(Path destino) {
        String nombre = destino.getFileName().toString().toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".png")) {
            return "image/png";
        }
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (nombre.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    /**
     * Ignora cualquier ruta relativa que intento salirse del directorio base.
     */
    private Path resolverSeguro(String rutaRelativa) {
        Path destino = directorioBase.resolve(rutaRelativa).toAbsolutePath().normalize();
        if (!destino.startsWith(directorioBase)) {
            throw ApiException.solicitudInvalida(MensajeException.RUTA_IMAGEN_INVALIDA);
        }
        return destino;
    }

    public record ImagenGuardada(byte[] contenido, String contentType) {
    }
}
