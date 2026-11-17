package org.example.footgamers.entities;

import org.example.footgamers.entities.enums.EstadoTorneo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EstadoTorneoConverterTest {
    private final EstadoTorneoConverter converter = new EstadoTorneoConverter();
    @Test void aceptaEstadoAntiguoSinModificarLosActuales() {
        assertEquals(EstadoTorneo.EN_PROCESO, converter.convertToEntityAttribute("EN_GRUPOS"));
        for (var estado : EstadoTorneo.values()) {
            assertEquals(estado, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(estado)));
        }
    }
    @Test void conservaNulosYNoOcultaEstadosDesconocidos() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToDatabaseColumn(null));
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("DESCONOCIDO"));
    }
}
