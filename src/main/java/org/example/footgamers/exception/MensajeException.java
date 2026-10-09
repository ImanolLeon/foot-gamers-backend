package org.example.footgamers.exception;

public class MensajeException {

    private MensajeException() {
    }

    public static final String VALOR_NO_ENCONTRADO = "El %s con id %s no se ha encontrado";
    public static final String VALOR_NO_ENCONTRADO_POR_USERNAME = "El %s con username %s no se ha encontrado";
    public static final String VALOR_YA_EN_USO = "El %s con %s %s ya se encuentra registrado";
    public static final String PARTICIPACIONES_NO_CONFIRMADAS =
            "Todas las participaciones deben estar confirmadas para asignar el ganador";
    public static final String TORNEO_SIN_GRUPOS = "El torneo aun no tiene grupos asignados";
    public static final String TORNEO_SIN_JUGADORES = "El torneo aun no tiene jugadores inscritos";
    public static final String TORNEO_TERMINADO = "No se pueden inscribir jugadores en un torneo terminado";
    public static final String TORNEO_YA_TERMINADO = "El torneo ya se encuentra terminado";
    public static final String ESTADO_TORNEO_INVALIDO = "El único cambio de estado permitido es a TERMINADO";
    public static final String TORNEO_CON_GRUPOS =
            "El torneo ya tiene grupos asignados y no admite nuevas inscripciones";
    public static final String JUGADOR_YA_INSCRITO = "El jugador ya está inscrito al torneo";
    public static final String TORNEO_SIN_CUPO = "El torneo alcanzó su cupo máximo de %s jugadores";
    public static final String GRUPOS_YA_ASIGNADOS = "El torneo ya tiene los grupos asignados";
    public static final String PARTIDOS_YA_GENERADOS = "Los partidos de la fase %s ya fueron generados";
    public static final String RESULTADO_INVALIDO = "El resultado %s no es válido, use el formato goles-goles";
    public static final String CANTIDAD_GRUPOS_INVALIDA = "El torneo debe tener como mínimo un grupo";
    public static final String JUGADORES_INSUFICIENTES = "Se necesitan al menos %s jugadores para %s grupos";
    public static final String TAMANO_PAGINA_INVALIDO = "El tamano de pagina debe estar entre 1 y %s";
    public static final String PAGINA_INVALIDA = "La pagina debe estar entre 0 y %s";
    public static final String TROFEO_CON_TORNEOS =
            "El trofeo con id %s tiene %s torneo(s) asociados y no puede eliminarse";
    public static final String IMAGEN_VACIA = "No se recibio ningun archivo de imagen";
    public static final String IMAGEN_DEMASIADO_GRANDE =
            "La imagen supera el tamano maximo permitido de %s bytes";
    public static final String FORMATO_IMAGEN_INVALIDO =
            "El archivo no es una imagen valida, se aceptan los formatos: %s";
    public static final String IMAGEN_NO_ENCONTRADA = "El usuario no tiene una imagen cargada";
    public static final String IMAGEN_NO_GUARDADA = "No se pudo guardar la imagen";
    public static final String IMAGEN_NO_LEIDA = "No se pudo leer la imagen";
    public static final String RUTA_IMAGEN_INVALIDA = "La ruta de la imagen no es valida";
    public static final String IMAGEN_SIN_PERMISO = "Solo puedes cambiar tu propia imagen";
}