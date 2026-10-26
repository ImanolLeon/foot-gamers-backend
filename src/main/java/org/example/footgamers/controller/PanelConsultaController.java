package org.example.footgamers.controller;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.response.JugadorResponseDto;
import org.example.footgamers.dto.response.ParticipacionPartidoResponseDto;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.repository.PartidoRepository;
import org.example.footgamers.repository.TorneoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Consultas de detalle del panel; la cadena de seguridad exige autenticación. */
@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PanelConsultaController {
    private final TorneoRepository torneos;
    private final PartidoRepository partidos;

    @GetMapping("/torneos/{id}/participantes")
    public List<JugadorResponseDto> participantes(@PathVariable Long id) {
        var torneo = torneos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Torneo no encontrado"));
        return torneo.getParticipantes().stream().map(j -> new JugadorResponseDto(
                j.getId(), j.getUsuario().getId(), j.getUsuario().getUsername(),
                j.getEquipoFavorito(), j.getFechaRegistro())).toList();
    }

    @GetMapping("/partidos/{id}/participaciones")
    public List<ParticipacionPartidoResponseDto> participaciones(@PathVariable Long id) {
        var partido = partidos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Partido no encontrado"));
        return partido.getParticipacionPartidos().stream().map(p -> new ParticipacionPartidoResponseDto(
                p.getId(), partido.getId(), p.getJugador().getId(), p.getJugador().getUsuario().getUsername(),
                p.getBando().getId(), p.getBando().getNumeroLado(), p.getEquipo(),
                p.getEstadoConfirmacion())).toList();
    }
}
