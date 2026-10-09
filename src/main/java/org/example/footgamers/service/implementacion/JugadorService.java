package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.JugadorRequestDto;
import org.example.footgamers.dto.response.HistorialJugadorResponseDto;
import org.example.footgamers.dto.response.HistorialPartidoResponseDto;
import org.example.footgamers.dto.response.JugadorResponseDto;
import org.example.footgamers.dto.response.PartidoResponseDto;
import org.example.footgamers.dto.response.TorneoResponseDto;
import org.example.footgamers.dto.response.TrofeoResponseDto;
import org.example.footgamers.entities.*;
import org.example.footgamers.entities.enums.EstadoConfirmacion;
import org.example.footgamers.entities.enums.EstadoPartido;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.*;
import org.example.footgamers.service.reglas.IAuth;
import org.example.footgamers.service.reglas.IJugador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JugadorService implements IJugador {

    private static final int TAMANO_MAXIMO_PAGINA = 100;
    private static final long PAGINA_MAXIMA = 100_000L;

    private final JugadorRepository jugadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipacionPartidoRepository participacionPartidoRepository;
    private final TorneoRepository torneoRepository;
    private final PartidoRepository partidoRepository;
    private final TrofeoRepository trofeoRepository;
    private final IAuth authService;

    @Override
    @Transactional
    public JugadorResponseDto crear(JugadorRequestDto request) {
        Jugador jugador = new Jugador();
        jugador.setUsuario(buscarUsuario(request.usuarioId()));
        jugador.setEquipoFavorito(request.equipoFavorito());
        jugador.setFechaRegistro(LocalDate.now());
        return toResponse(jugadorRepository.save(jugador));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JugadorResponseDto> obtenerTodos(Long pagina, Long tamano) {
        return jugadorRepository.findAll(PageRequest.of(pagina.intValue(), tamano.intValue()))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public JugadorResponseDto obtenerPorId(Long id) {
        return toResponse(buscarJugador(id));
    }

    @Override
    @Transactional(readOnly = true)
    public JugadorResponseDto obtenerPorUsername(String username) {
        return toResponse(buscarJugadorPorUsername(username));
    }

    @Override
    @Transactional
    public JugadorResponseDto actualizar(Long id, JugadorRequestDto request) {
        Jugador jugador = buscarJugador(id);
        jugador.setUsuario(buscarUsuario(request.usuarioId()));
        jugador.setEquipoFavorito(request.equipoFavorito());
        return toResponse(jugadorRepository.save(jugador));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        jugadorRepository.delete(buscarJugador(id));
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialJugadorResponseDto obtenerHistorial(Long id) {
        Jugador jugador = buscarJugador(id);
        List<ParticipacionPartido> participaciones = participacionPartidoRepository
                .findByJugador_Id(jugador.getId());

        long partidosJugados = 0, victorias = 0, empates = 0, derrotas = 0;
        long golesAFavor = 0, golesEnContra = 0     ;
        List<HistorialPartidoResponseDto> partidos = new ArrayList<>();

        for (ParticipacionPartido participacion : participaciones) {
            if (participacion.getEstadoConfirmacion() != EstadoConfirmacion.CONFIRMADO) {
                continue;
            }
            Partido partido = participacion.getPartido();
            if (partido == null || partido.getEstado() != EstadoPartido.FINALIZADO) {
                continue;
            }

            int[] goles = parsearResultado(partido.getResultado());
            boolean ladoUno = participacion.getBando() != null
                    && participacion.getBando().getNumeroLado() == 1;
            int golesPropios = ladoUno ? goles[0] : goles[1];
            int golesRival = ladoUno ? goles[1] : goles[0];
            golesAFavor += golesPropios;
            golesEnContra += golesRival;

            String desenlace;
            if (golesPropios > golesRival) {
                victorias++;
                desenlace = "VICTORIA";
            } else if (golesPropios == golesRival) {
                empates++;
                desenlace = "EMPATE";
            } else {
                derrotas++;
                desenlace = "DERROTA";
            }
            partidosJugados++;

            partidos.add(new HistorialPartidoResponseDto(
                    partido.getId(),
                    partido.getTorneo() != null ? partido.getTorneo().getId() : null,
                    partido.getTorneo() != null ? partido.getTorneo().getNombre() : "Amistoso",
                    partido.getTipoPartido(),
                    partido.getFase(),
                    partido.getFecha(),
                    obtenerRival(partido, participacion),
                    partido.getResultado(),
                    golesPropios + "-" + golesRival,
                    golesRival + "-" + golesPropios,
                    desenlace,
                    participacion.getEstadoConfirmacion().name()
            ));
        }

        return new HistorialJugadorResponseDto(
                jugador.getId(),
                jugador.getUsuario().getUsername(),
                jugador.getEquipoFavorito(),
                partidosJugados,
                victorias,
                empates,
                derrotas,
                golesAFavor,
                golesEnContra,
                (long) torneoRepository.findByGanador_Id(jugador.getId()).size(),
                partidos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrofeoResponseDto> obtenerMisTrofeos(Long pagina, Long tamano) {
        return obtenerTrofeosDe(jugadorActual().getId(), pagina, tamano);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TorneoResponseDto> obtenerMisTorneos(Long pagina, Long tamano) {
        return obtenerTorneosDe(jugadorActual().getId(), pagina, tamano);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartidoResponseDto> obtenerMisPartidos(Long pagina, Long tamano) {
        return obtenerPartidosDe(jugadorActual().getId(), pagina, tamano);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<TrofeoResponseDto> obtenerTrofeosDeJugador(Long jugadorId, Long pagina, Long tamano) {
        return obtenerTrofeosDe(buscarJugador(jugadorId).getId(), pagina, tamano);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<TorneoResponseDto> obtenerTorneosDeJugador(Long jugadorId, Long pagina, Long tamano) {
        return obtenerTorneosDe(buscarJugador(jugadorId).getId(), pagina, tamano);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<PartidoResponseDto> obtenerPartidosDeJugador(Long jugadorId, Long pagina, Long tamano) {
        return obtenerPartidosDe(buscarJugador(jugadorId).getId(), pagina, tamano);
    }

    private Page<TrofeoResponseDto> obtenerTrofeosDe(Long jugadorId, Long pagina, Long tamano) {
        return trofeoRepository.findByJugador_Id(jugadorId, paginacion(pagina, tamano))
                .map(this::toTrofeoResponse);
    }

    private Page<TorneoResponseDto> obtenerTorneosDe(Long jugadorId, Long pagina, Long tamano) {
        return torneoRepository.findByParticipantes_Id(jugadorId, paginacion(pagina, tamano))
                .map(this::toTorneoResponse);
    }

    private Page<PartidoResponseDto> obtenerPartidosDe(Long jugadorId, Long pagina, Long tamano) {
        return partidoRepository.findByJugador_Id(jugadorId, paginacion(pagina, tamano))
                .map(this::toPartidoResponse);
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

    private Jugador jugadorActual() {
        return authService.jugadorActual();
    }

    private String obtenerRival(Partido partido, ParticipacionPartido actual) {
        return partido.getParticipacionPartidos().stream()
                .filter(p -> !p.getId().equals(actual.getId()))
                .map(p -> p.getJugador().getUsuario().getUsername())
                .findFirst()
                .orElse("Sin rival");
    }

    private int[] parsearResultado(String resultado) {
        String[] partes = resultado == null ? new String[0] : resultado.split("-");
        if (partes.length != 2) {
            return new int[]{0, 0};
        }
        try {
            return new int[]{Integer.parseInt(partes[0].trim()), Integer.parseInt(partes[1].trim())};
        } catch (NumberFormatException e) {
            return new int[]{0, 0};
        }
    }

    private Jugador buscarJugador(Long id) {
        return jugadorRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "jugador", id)));
    }

    private Jugador buscarJugadorPorUsername(String username) {
        return jugadorRepository.findByUsuario_Username(username)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO_POR_USERNAME, "jugador", username)));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "usuario", id)));
    }

    private JugadorResponseDto toResponse(Jugador jugador) {
        return new JugadorResponseDto(
                jugador.getId(),
                jugador.getUsuario().getId(),
                jugador.getUsuario().getUsername(),
                jugador.getEquipoFavorito(),
                jugador.getFechaRegistro()
        );
    }

    private TrofeoResponseDto toTrofeoResponse(Trofeo trofeo) {
        Jugador jugador = trofeo.getJugador();
        return new TrofeoResponseDto(
                trofeo.getId(),
                trofeo.getNombre(),
                jugador != null ? jugador.getId() : null,
                jugador != null && jugador.getUsuario() != null ? jugador.getUsuario().getUsername() : null,
                trofeo.getFecha()
        );
    }

    private TorneoResponseDto toTorneoResponse(Torneo torneo) {
        Categoria categoria = torneo.getCategoria();
        Jugador ganador = torneo.getGanador();
        return new TorneoResponseDto(
                torneo.getId(),
                torneo.getNombre(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNombre() : null,
                torneo.getFechaInicio(),
                torneo.getFechaFin(),
                ganador != null ? ganador.getId() : null,
                ganador != null && ganador.getUsuario() != null ? ganador.getUsuario().getUsername() : null,
                torneo.getCantidadJugadores(),
                torneo.getCantidadGrupos(),
                torneo.getFaseActual(),
                torneo.getEstado()
        );
    }

    private PartidoResponseDto toPartidoResponse(Partido partido) {
        Bando ganador = partido.getGanador();
        return new PartidoResponseDto(
                partido.getId(),
                partido.getResultado(),
                ganador != null ? ganador.getId() : null,
                partido.getTorneo() != null ? partido.getTorneo().getId() : null,
                partido.getTipoPartido(),
                partido.getEstadoConfirmacion(),
                partido.getFecha(),
                partido.getFase(),
                partido.getGrupo() != null ? partido.getGrupo().getId() : null,
                partido.getEstado()
        );
    }
}