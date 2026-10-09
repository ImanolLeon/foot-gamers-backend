package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.PartidoRequestDto;
import org.example.footgamers.dto.response.PartidoResponseDto;
import org.example.footgamers.entities.*;
import org.example.footgamers.entities.enums.EstadoConfirmacion;
import org.example.footgamers.entities.enums.EstadoPartido;
import org.example.footgamers.entities.enums.EstadoTorneo;
import org.example.footgamers.entities.enums.FaseTorneo;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.*;
import org.example.footgamers.service.reglas.IPartido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartidoService implements IPartido {

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final BandoRepository bandoRepository;
    private final ParticipacionPartidoRepository participacionPartidoRepository;
    private final ClasificacionRepository clasificacionRepository;
    private final GrupoRepository grupoRepository;
    private final TrofeoRepository trofeoRepository;

    @Override
    @Transactional
    public PartidoResponseDto crear(PartidoRequestDto request) {
        Partido partido = new Partido();
        partido.setTorneo(buscarTorneoOpcional(request.torneoId()));
        partido.setTipoPartido(request.tipoPartido());
        partido.setFecha(request.fecha());
        partido.setEstadoConfirmacion(EstadoConfirmacion.NO_CONFIRMADO);
        partido.setEstado(EstadoPartido.PROGRAMADO);
        return toResponse(partidoRepository.save(partido));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartidoResponseDto> obtenerTodos(Long pagina, Long tamano) {
        return partidoRepository.findAll(PageRequest.of(pagina.intValue(), tamano.intValue()))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidoResponseDto> listarPorTorneo(Long torneoId) {
        buscarTorneo(torneoId);
        return partidoRepository.findByTorneo_Id(torneoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidoResponseDto> listarPorGrupo(Long grupoId) {
        buscarGrupo(grupoId);
        return partidoRepository.findByGrupo_Id(grupoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PartidoResponseDto obtenerPorId(Long id) {
        return toResponse(buscarPartido(id));
    }

    @Override
    @Transactional
    public PartidoResponseDto actualizar(Long id, PartidoRequestDto request) {
        Partido partido = buscarPartido(id);
        partido.setTorneo(buscarTorneoOpcional(request.torneoId()));
        partido.setTipoPartido(request.tipoPartido());
        partido.setFecha(request.fecha());
        return toResponse(partidoRepository.save(partido));
    }

    @Override
    @Transactional
    public PartidoResponseDto asignarGanadorBando(Long id, Long bandoId) {
        Partido partido = buscarPartido(id);
        Bando bando = buscarBando(bandoId);
        validarParticipacionesConfirmadas(partido);
        partido.setGanador(bando);
        partido = partidoRepository.save(partido);
        Jugador ganador = jugadorDelBando(partido, bando);
        if (partido.getFase() == FaseTorneo.FINAL) {
            finalizarTorneo(partido.getTorneo(), ganador);
        } else if (partido.getFase() != null && partido.getFase() != FaseTorneo.GRUPOS) {
            avanzarGanador(partido, ganador);
        }
        return toResponse(partido);
    }

    @Override
    @Transactional
    public PartidoResponseDto confirmarPartido(Long id, String resultado) {
        Partido partido = buscarPartido(id);
        if (partido.getEstado() == EstadoPartido.FINALIZADO) {
            throw ApiException.solicitudInvalida("El partido ya fue finalizado");
        }
        int[] goles = parsearResultado(resultado);
        partido.setResultado(resultado);
        partido.setEstadoConfirmacion(EstadoConfirmacion.CONFIRMADO);
        partido.setEstado(EstadoPartido.FINALIZADO);
        partido = partidoRepository.save(partido);
        if (partido.getFase() == FaseTorneo.GRUPOS && partido.getGrupo() != null) {
            actualizarClasificacion(partido, goles[0], goles[1]);
        }
        return toResponse(partido);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        partidoRepository.delete(buscarPartido(id));
    }

    private int[] parsearResultado(String resultado) {
        String[] partes = resultado.split("-");
        if (partes.length != 2) {
            throw ApiException.solicitudInvalida(String.format(MensajeException.RESULTADO_INVALIDO, resultado));
        }
        try {
            return new int[]{Integer.parseInt(partes[0].trim()), Integer.parseInt(partes[1].trim())};
        } catch (NumberFormatException e) {
            throw ApiException.solicitudInvalida(String.format(MensajeException.RESULTADO_INVALIDO, resultado));
        }
    }

    private void actualizarClasificacion(Partido partido, int golesUno, int golesDos) {
        for (ParticipacionPartido participacion : partido.getParticipacionPartidos()) {
            boolean ladoUno = participacion.getBando() != null
                    && participacion.getBando().getNumeroLado() == 1;
            int golesFavor = ladoUno ? golesUno : golesDos;
            int golesContra = ladoUno ? golesDos : golesUno;

            Clasificacion clasificacion = clasificacionRepository
                    .findByGrupo_IdAndJugador_Id(partido.getGrupo().getId(), participacion.getJugador().getId())
                    .orElseThrow(() -> ApiException.noEncontrado(
                            "El jugador no tiene clasificación en el grupo"));
            clasificacion.setPartidosJugados(clasificacion.getPartidosJugados() + 1);
            clasificacion.setGolesAFavor(clasificacion.getGolesAFavor() + golesFavor);
            clasificacion.setGolesEnContra(clasificacion.getGolesEnContra() + golesContra);
            if (golesFavor > golesContra) {
                clasificacion.setGanados(clasificacion.getGanados() + 1);
                clasificacion.setPuntos(clasificacion.getPuntos() + 3);
            } else if (golesFavor == golesContra) {
                clasificacion.setEmpatados(clasificacion.getEmpatados() + 1);
                clasificacion.setPuntos(clasificacion.getPuntos() + 1);
            } else {
                clasificacion.setPerdidos(clasificacion.getPerdidos() + 1);
            }
            clasificacionRepository.save(clasificacion);
        }
    }

    private Jugador jugadorDelBando(Partido partido, Bando bando) {
        return partido.getParticipacionPartidos().stream()
                .filter(p -> p.getBando() != null && p.getBando().getId().equals(bando.getId()))
                .map(ParticipacionPartido::getJugador)
                .findFirst()
                .orElseThrow(() -> ApiException.solicitudInvalida("El bando no pertenece al partido"));
    }

    private void avanzarGanador(Partido partido, Jugador ganador) {
        List<Partido> siguientes = partidoRepository.findByFuenteUno_IdOrFuenteDos_Id(partido.getId(), partido.getId());
        if (siguientes.isEmpty()) {
            return;
        }
        Partido siguiente = siguientes.get(0);
        boolean esFuenteUno = siguiente.getFuenteUno() != null
                && siguiente.getFuenteUno().getId().equals(partido.getId());
        int lado = esFuenteUno ? 1 : 2;

        if (siguiente.getParticipacionPartidos() != null) {
            for (ParticipacionPartido participacion : siguiente.getParticipacionPartidos()) {
                if (participacion.getBando() != null && participacion.getBando().getNumeroLado() == lado) {
                    participacionPartidoRepository.delete(participacion);
                }
            }
        }

        Bando bando = new Bando();
        bando.setNumeroLado(lado);
        bando = bandoRepository.save(bando);

        ParticipacionPartido participacion = new ParticipacionPartido();
        participacion.setPartido(siguiente);
        participacion.setBando(bando);
        participacion.setJugador(ganador);
        participacion.setEquipo(ganador.getUsuario().getUsername());
        participacion.setEstadoConfirmacion(EstadoConfirmacion.CONFIRMADO);
        participacionPartidoRepository.save(participacion);
    }

    private void finalizarTorneo(Torneo torneo, Jugador ganador) {
        if (torneo == null) {
            return;
        }
        torneo.setGanador(ganador);
        torneo.setEstado(EstadoTorneo.TERMINADO);
        torneo.setFaseActual(FaseTorneo.FINAL);
        torneoRepository.save(torneo);
        if (torneo.getTrofeo() != null) {
            Trofeo trofeo = torneo.getTrofeo();
            trofeo.setJugador(ganador);
            trofeoRepository.save(trofeo);
        }
    }

    private void validarParticipacionesConfirmadas(Partido partido) {
        List<ParticipacionPartido> participaciones = partido.getParticipacionPartidos();
        boolean todasConfirmadas = participaciones != null && !participaciones.isEmpty()
                && participaciones.stream()
                .allMatch(p -> p.getEstadoConfirmacion() == EstadoConfirmacion.CONFIRMADO);
        if (!todasConfirmadas) {
            throw ApiException.solicitudInvalida(MensajeException.PARTICIPACIONES_NO_CONFIRMADAS);
        }
    }

    private Partido buscarPartido(Long id) {
        return partidoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "partido", id)));
    }

    private Grupo buscarGrupo(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "grupo", id)));
    }

    private Torneo buscarTorneoOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return buscarTorneo(id);
    }

    private Torneo buscarTorneo(Long id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "torneo", id)));
    }

    private Bando buscarBando(Long id) {
        return bandoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "bando", id)));
    }

    private PartidoResponseDto toResponse(Partido partido) {
        return new PartidoResponseDto(
                partido.getId(),
                partido.getResultado(),
                partido.getGanador() != null ? partido.getGanador().getId() : null,
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