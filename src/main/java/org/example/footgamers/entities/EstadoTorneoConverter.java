package org.example.footgamers.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.footgamers.entities.enums.EstadoTorneo;

/** Compatibilidad de lectura con torneos guardados antes de separar estado y fase. */
@Converter
public class EstadoTorneoConverter implements AttributeConverter<EstadoTorneo, String> {
    @Override
    public String convertToDatabaseColumn(EstadoTorneo estado) {
        return estado == null ? null : estado.name();
    }

    @Override
    public EstadoTorneo convertToEntityAttribute(String valor) {
        if (valor == null) return null;
        if ("EN_GRUPOS".equals(valor)) return EstadoTorneo.EN_PROCESO;
        return EstadoTorneo.valueOf(valor);
    }
}
