package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.CambiarEstadoTorneoRequestDto;
import org.example.footgamers.dto.request.TorneoRequestDto;
import org.example.footgamers.dto.response.TorneoInscripcionResponseDto;
import org.example.footgamers.dto.response.TorneoResponseDto;
import org.example.footgamers.entities.Categoria;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Torneo;
import org.example.footgamers.entities.Trofeo;
import org.example.footgamers.entities.enums.EstadoTorneo;
import org.example.footgamers.entities.enums.FaseTorneo;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.CategoriaRepository;
import org.example.footgamers.repository.GrupoRepository;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.TorneoRepository;
import org.example.footgamers.repository.TrofeoRepository;
import org.example.footgamers.repository.proyeccion.TorneoParticipantesProjection;
import org.example.footgamers.service.reglas.IAuth;
import org.example.footgamers.service.reglas.ITorneo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TorneoService implements ITorneo {

    private static final int TAMANO_MAXIMO_PAGINA = 100;
    private static final long PAGINA_MAXIMA = 100_000L;

    private final TorneoRepository torneoRepository;
    private final CategoriaRepository categoriaRepository;
    private final JugadorRepository jugadorRepository;
    private final TrofeoRepository trofeoRepository;
    private final GrupoRepository grupoRepository;
    private final IAuth authService;

    @Override
    @Transactional
    public TorneoResponseDto crear(TorneoRequestDto request) {
        Torneo torneo = new Torneo();
        torneo.setNombre(request.nombre());
        torneo.setCategoria(buscarCategoria(request.categoriaId()));
        torneo.setTrofeo(buscarTrofeo(request.trofeoId()));
        torneo.setFechaInicio(request.fechaInicio());
        torneo.setFechaFin(request.fechaFin());
        torneo.setCantidadJugadores(request.cantidadJugadores());
        torneo.setCantidadGrupos(request.cantidadGrupos());
        torneo.setFaseActual(FaseTorneo.GRUPOS);
        torneo.setEstado(EstadoTorneo.EN_PROCESO);
        return toResponse(torneoRepository.save(torneo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TorneoResponseDto> obtenerTodos(Long pagina, Long tamano) {
        return torneoRepository.findAll(paginacion(pagina, tamano))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TorneoInscripcionResponseDto> obtenerDisponibles(Long pagina, Long tamano) {
        Jugador jugador = authService.jugadorActual();
        Page<Torneo> paginaTorneos = torneoRepository.findByEstado(
                EstadoTorneo.EN_PROCESO, paginacion(pagina, tamano));
        if (paginaTorneos.isEmpty()) {
            return paginaTorneos.map(torneo -> toInscripcionResponse(torneo, 0L, false));
        }
        List<Long> idsTorneos = paginaTorneos.getContent().stream()
                .map(Torneo::getId)
                .toList();
        Map<Long, Long> inscritosPorTorneo = torneoRepository.contarParticipantes(idsTorneos)
                .stream()
                .collect(Collectors.toMap(
                        TorneoParticipantesProjection::getTorneoId,
                        TorneoParticipantesProjection::getInscritos));
        Set<Long> yaInscritoEn = new HashSet<>(
                torneoRepository.buscarIdsInscritosEn(idsTorneos, jugador.getId()));
        return paginaTorneos.map(torneo -> toInscripcionResponse(
                torneo,
                inscritosPorTorneo.getOrDefault(torneo.getId(), 0L),
                yaInscritoEn.contains(torneo.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public TorneoResponseDto obtenerPorId(Long id) {
        return toResponse(buscarTorneo(id));
    }

    @Override
    @Transactional
    public TorneoResponseDto actualizar(Long id, TorneoRequestDto request) {
        Torneo torneo = buscarTorneo(id);
        torneo.setNombre(request.nombre());
        torneo.setCategoria(buscarCategoria(request.categoriaId()));
        torneo.setTrofeo(buscarTrofeo(request.trofeoId()));
        torneo.setFechaInicio(request.fechaInicio());
        torneo.setFechaFin(request.fechaFin());
        torneo.setCantidadJugadores(request.cantidadJugadores());
        torneo.setCantidadGrupos(request.cantidadGrupos());
        return toResponse(torneoRepository.save(torneo));
    }

    @Override
    @Transactional
    public TorneoResponseDto asignarGanador(Long id, Long jugadorId) {
        Torneo torneo = buscarTorneo(id);
        Jugador ganador = buscarJugador(jugadorId);
        torneo.setGanador(ganador);
        torneo.setEstado(EstadoTorneo.TERMINADO);
        torneo.setFaseActual(FaseTorneo.FINAL);
        if (torneo.getTrofeo() != null) {
            torneo.getTrofeo().setJugador(ganador);
        }
        return toResponse(torneoRepository.save(torneo));
    }

    @Override
    @Transactional
    public TorneoResponseDto cambiarEstado(Long id, CambiarEstadoTorneoRequestDto request) {
        Torneo torneo = buscarTorneo(id);
        if (torneo.getEstado() == EstadoTorneo.TERMINADO) {
            throw ApiException.solicitudInvalida(MensajeException.TORNEO_YA_TERMINADO);
        }
        if (request.estado() != EstadoTorneo.TERMINADO) {
            throw ApiException.solicitudInvalida(MensajeException.ESTADO_TORNEO_INVALIDO);
        }
        torneo.setEstado(EstadoTorneo.TERMINADO);
        return toResponse(torneoRepository.save(torneo));
    }

    @Override
    @Transactional
    public TorneoResponseDto inscribirJugador(Long id, Long jugadorId) {
        return inscribir(buscarTorneo(id), buscarJugador(jugadorId));
    }

    @Override
    @Transactional
    public TorneoResponseDto inscribirme(Long id) {
        return inscribir(buscarTorneo(id), authService.jugadorActual());
    }

    private TorneoResponseDto inscribir(Torneo torneo, Jugador jugador) {
        validarInscripcion(torneo, jugador);
        List<Jugador> participantes = torneo.getParticipantes();
        if (participantes == null) {
            participantes = new ArrayList<>();
            torneo.setParticipantes(participantes);
        }
        participantes.add(jugador);
        return toResponse(torneoRepository.save(torneo));
    }

    private void validarInscripcion(Torneo torneo, Jugador jugador) {
        if (torneo.getEstado() != EstadoTorneo.EN_PROCESO) {
            throw ApiException.solicitudInvalida(MensajeException.TORNEO_TERMINADO);
        }
        if (!grupoRepository.findByTorneo_Id(torneo.getId()).isEmpty()) {
            throw ApiException.solicitudInvalida(MensajeException.TORNEO_CON_GRUPOS);
        }
        if (torneoRepository.existsByIdAndParticipantes_Id(torneo.getId(), jugador.getId())) {
            throw ApiException.valorYaEnUso(MensajeException.JUGADOR_YA_INSCRITO);
        }
        long inscritos = contarParticipantes(torneo.getId());
        if (inscritos >= torneo.getCantidadJugadores()) {
            throw ApiException.solicitudInvalida(
                    String.format(MensajeException.TORNEO_SIN_CUPO, torneo.getCantidadJugadores()));
        }
    }

    private long contarParticipantes(Long torneoId) {
        return torneoRepository.contarParticipantes(List.of(torneoId))
                .stream()
                .findFirst()
                .map(TorneoParticipantesProjection::getInscritos)
                .orElse(0L);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        torneoRepository.delete(buscarTorneo(id));
    }

    private Torneo buscarTorneo(Long id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "torneo", id)));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "categoria", id)));
    }

    private Jugador buscarJugador(Long id) {
        return jugadorRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "jugador", id)));
    }

    private Trofeo buscarTrofeo(Long id) {
        return trofeoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "trofeo", id)));
    }

    private TorneoResponseDto toResponse(Torneo torneo) {
        Jugador ganador = torneo.getGanador();
        return new TorneoResponseDto(
                torneo.getId(),
                torneo.getNombre(),
                torneo.getCategoria().getId(),
                torneo.getCategoria().getNombre(),
                torneo.getFechaInicio(),
                torneo.getFechaFin(),
                ganador != null ? ganador.getId() : null,
                ganador != null ? ganador.getUsuario().getUsername() : null,
                torneo.getCantidadJugadores(),
                torneo.getCantidadGrupos(),
                torneo.getFaseActual(),
                torneo.getEstado()
        );
    }

    private TorneoInscripcionResponseDto toInscripcionResponse(Torneo torneo, long inscritos,
                                                               boolean yaInscrito) {
        long cantidadJugadores = torneo.getCantidadJugadores();
        Categoria categoria = torneo.getCategoria();
        return new TorneoInscripcionResponseDto(
                torneo.getId(),
                torneo.getNombre(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNombre() : null,
                torneo.getFechaInicio(),
                torneo.getFechaFin(),
                cantidadJugadores,
                inscritos,
                Math.max(0L, cantidadJugadores - inscritos),
                torneo.getEstado(),
                yaInscrito
        );
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
        return PageRequest.of(pagina.intValue(), tamano.intValue(),
                Sort.by(Sort.Direction.ASC, "fechaInicio", "id"));
    }
}