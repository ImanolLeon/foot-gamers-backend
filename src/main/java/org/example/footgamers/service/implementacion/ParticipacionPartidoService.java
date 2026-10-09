package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.ParticipacionPartidoRequestDto;
import org.example.footgamers.dto.response.ParticipacionPartidoResponseDto;
import org.example.footgamers.entities.Bando;
import org.example.footgamers.entities.enums.EstadoConfirmacion;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.ParticipacionPartido;
import org.example.footgamers.entities.Partido;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.BandoRepository;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.ParticipacionPartidoRepository;
import org.example.footgamers.repository.PartidoRepository;
import org.example.footgamers.service.reglas.IParticipacionPartido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipacionPartidoService implements IParticipacionPartido {

    private final ParticipacionPartidoRepository participacionPartidoRepository;
    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final BandoRepository bandoRepository;

    @Override
    @Transactional
    public ParticipacionPartidoResponseDto crear(ParticipacionPartidoRequestDto request) {
        ParticipacionPartido participacion = new ParticipacionPartido();
        participacion.setPartido(buscarPartido(request.partidoId()));
        participacion.setJugador(buscarJugador(request.jugadorId()));
        participacion.setBando(buscarBando(request.bandoId()));
        participacion.setEquipo(request.equipo());
        participacion.setEstadoConfirmacion(EstadoConfirmacion.NO_CONFIRMADO);
        return toResponse(participacionPartidoRepository.save(participacion));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParticipacionPartidoResponseDto> obtenerTodos(Long pagina, Long tamano) {
        return participacionPartidoRepository.findAll(PageRequest.of(pagina.intValue(), tamano.intValue()))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipacionPartidoResponseDto obtenerPorId(Long id) {
        return toResponse(buscarParticipacion(id));
    }

    @Override
    @Transactional
    public ParticipacionPartidoResponseDto actualizar(Long id, ParticipacionPartidoRequestDto request) {
        ParticipacionPartido participacion = buscarParticipacion(id);
        participacion.setBando(buscarBando(request.bandoId()));
        participacion.setEquipo(request.equipo());
        return toResponse(participacionPartidoRepository.save(participacion));
    }

    @Override
    @Transactional
    public ParticipacionPartidoResponseDto confirmar(Long id) {
        ParticipacionPartido participacion = buscarParticipacion(id);
        participacion.setEstadoConfirmacion(EstadoConfirmacion.CONFIRMADO);
        return toResponse(participacionPartidoRepository.save(participacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        participacionPartidoRepository.delete(buscarParticipacion(id));
    }

    private ParticipacionPartido buscarParticipacion(Long id) {
        return participacionPartidoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "participación", id)));
    }

    private Partido buscarPartido(Long id) {
        return partidoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "partido", id)));
    }

    private Jugador buscarJugador(Long id) {
        return jugadorRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "jugador", id)));
    }

    private Bando buscarBando(Long id) {
        return bandoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "bando", id)));
    }

    private ParticipacionPartidoResponseDto toResponse(ParticipacionPartido participacion) {
        Jugador jugador = participacion.getJugador();
        Bando bando = participacion.getBando();
        return new ParticipacionPartidoResponseDto(
                participacion.getId(),
                participacion.getPartido().getId(),
                jugador.getId(),
                jugador.getUsuario().getUsername(),
                bando.getId(),
                bando.getNumeroLado(),
                participacion.getEquipo(),
                participacion.getEstadoConfirmacion()
        );
    }
}