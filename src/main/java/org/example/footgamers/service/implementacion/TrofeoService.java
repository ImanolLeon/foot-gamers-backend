package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.TrofeoRequestDto;
import org.example.footgamers.dto.response.TrofeoResponseDto;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Trofeo;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.TrofeoRepository;
import org.example.footgamers.service.reglas.ITrofeo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrofeoService implements ITrofeo {

    private static final int TAMANO_MAXIMO_PAGINA = 100;
    private static final long PAGINA_MAXIMA = 100_000L;

    private final TrofeoRepository trofeoRepository;
    private final JugadorRepository jugadorRepository;

    @Override
    @Transactional
    public TrofeoResponseDto crear(TrofeoRequestDto request) {
        Trofeo trofeo = new Trofeo();
        trofeo.setNombre(request.nombre());
        trofeo.setFecha(request.fecha());
        trofeo.setJugador(buscarJugadorOpcional(request.jugadorId()));
        return toResponse(trofeoRepository.save(trofeo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrofeoResponseDto> obtenerTodos(Long pagina, Long tamano) {
        return trofeoRepository.findAll(paginacion(pagina, tamano))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TrofeoResponseDto obtenerPorId(Long id) {
        return toResponse(buscarTrofeo(id));
    }

    @Override
    @Transactional
    public TrofeoResponseDto actualizar(Long id, TrofeoRequestDto request) {
        Trofeo trofeo = buscarTrofeo(id);
        trofeo.setNombre(request.nombre());
        trofeo.setFecha(request.fecha());
        if (request.jugadorId() != null) {
            trofeo.setJugador(buscarJugadorOpcional(request.jugadorId()));
        }
        return toResponse(trofeoRepository.save(trofeo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Trofeo trofeo = buscarTrofeo(id);
        int torneosAsociados = trofeo.getTorneos() == null ? 0 : trofeo.getTorneos().size();
        if (torneosAsociados > 0) {
            throw ApiException.valorYaEnUso(
                    String.format(MensajeException.TROFEO_CON_TORNEOS, id, torneosAsociados));
        }
        trofeoRepository.delete(trofeo);
    }

    private Trofeo buscarTrofeo(Long id) {
        return trofeoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "trofeo", id)));
    }

    private Jugador buscarJugadorOpcional(Long jugadorId) {
        if (jugadorId == null) {
            return null;
        }
        return jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "jugador", jugadorId)));
    }

    private Pageable paginacion(Long pagina, Long tamano) {
        if (pagina == null || pagina < 0 || pagina > PAGINA_MAXIMA) {
            throw ApiException.solicitudInvalida(
                    String.format(MensajeException.PAGINA_INVALIDA, PAGINA_MAXIMA));
        }
        if (tamano == null || tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw ApiException.solicitudInvalida(
                    String.format(MensajeException.TAMANO_PAGINA_INVALIDO, TAMANO_MAXIMO_PAGINA));
        }
        return PageRequest.of(pagina.intValue(), tamano.intValue(), Sort.by(Sort.Direction.DESC, "id"));
    }

    private TrofeoResponseDto toResponse(Trofeo trofeo) {
        Jugador jugador = trofeo.getJugador();
        return new TrofeoResponseDto(
                trofeo.getId(),
                trofeo.getNombre(),
                jugador != null ? jugador.getId() : null,
                jugador != null && jugador.getUsuario() != null ? jugador.getUsuario().getUsername() : null,
                trofeo.getFecha()
        );
    }
}
