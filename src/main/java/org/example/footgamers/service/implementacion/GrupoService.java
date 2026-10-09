package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.AsignacionesGruposRequestDto;
import org.example.footgamers.dto.request.AsignacionJugadorGrupoDto;
import org.example.footgamers.dto.response.ClasificacionResponseDto;
import org.example.footgamers.dto.response.GrupoResponseDto;
import org.example.footgamers.dto.response.PartidoResponseDto;
import org.example.footgamers.entities.*;
import org.example.footgamers.entities.enums.*;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.*;
import org.example.footgamers.service.reglas.IGrupo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupoService implements IGrupo {

    private final TorneoRepository torneoRepository;
    private final GrupoRepository grupoRepository;
    private final ClasificacionRepository clasificacionRepository;
    private final PartidoRepository partidoRepository;
    private final BandoRepository bandoRepository;
    private final ParticipacionPartidoRepository participacionPartidoRepository;

    @Override
    @Transactional
    public List<GrupoResponseDto> crearGrupos(Long torneoId, AsignacionesGruposRequestDto request) {
        Torneo torneo = buscarTorneo(torneoId);
        if (!grupoRepository.findByTorneo_Id(torneoId).isEmpty()) {
            throw ApiException.solicitudInvalida(MensajeException.GRUPOS_YA_ASIGNADOS);
        }
        int cantidadGrupos = (int) torneo.getCantidadGrupos();
        if (cantidadGrupos < 1) {
            throw ApiException.solicitudInvalida(MensajeException.CANTIDAD_GRUPOS_INVALIDA);
        }
        List<Jugador> participantes = torneo.getParticipantes();
        if (participantes == null || participantes.isEmpty()) {
            throw ApiException.solicitudInvalida(MensajeException.TORNEO_SIN_JUGADORES);
        }
        if (participantes.size() < cantidadGrupos * 2) {
            throw ApiException.solicitudInvalida(String.format(
                    MensajeException.JUGADORES_INSUFICIENTES, cantidadGrupos * 2, cantidadGrupos));
        }

        List<Grupo> grupos = new ArrayList<>();
        for (int i = 0; i < cantidadGrupos; i++) {
            Grupo grupo = new Grupo();
            grupo.setTorneo(torneo);
            grupo.setNombre(nombreGrupo(i));
            grupos.add(grupoRepository.save(grupo));
        }
        asignarJugadores(grupos, participantes, request);
        return grupos.stream().map(this::toGrupoResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoResponseDto> listarGrupos(Long torneoId) {
        buscarTorneo(torneoId);
        return grupoRepository.findByTorneo_Id(torneoId).stream()
                .map(this::toGrupoResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClasificacionResponseDto> clasificacion(Long grupoId) {
        Grupo grupo = buscarGrupo(grupoId);
        return ordenarClasificacion(clasificacionRepository.findByGrupo_Id(grupoId)).stream()
                .map(this::toClasificacionResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<PartidoResponseDto> generarPartidosGrupos(Long torneoId) {
        Torneo torneo = buscarTorneo(torneoId);
        List<Grupo> grupos = grupoRepository.findByTorneo_Id(torneoId);
        if (grupos.isEmpty()) {
            throw ApiException.solicitudInvalida(MensajeException.TORNEO_SIN_GRUPOS);
        }
        if (!partidoRepository.findByTorneo_IdAndFase(torneoId, FaseTorneo.GRUPOS).isEmpty()) {
            throw ApiException.solicitudInvalida(String.format(MensajeException.PARTIDOS_YA_GENERADOS, "GRUPOS"));
        }

        List<Partido> partidos = new ArrayList<>();
        for (Grupo grupo : grupos) {
            List<Jugador> integrantes = clasificacionRepository.findByGrupo_Id(grupo.getId()).stream()
                    .map(Clasificacion::getJugador)
                    .toList();
            for (int i = 0; i < integrantes.size(); i++) {
                for (int j = i + 1; j < integrantes.size(); j++) {
                    partidos.add(crearPartidoGrupo(torneo, grupo, integrantes.get(i), integrantes.get(j)));
                }
            }
        }
        return partidos.stream().map(this::toPartidoResponse).toList();
    }

    @Override
    @Transactional
    public List<PartidoResponseDto> generarLlaves(Long torneoId) {
        Torneo torneo = buscarTorneo(torneoId);
        List<Grupo> grupos = grupoRepository.findByTorneo_Id(torneoId);
        if (grupos.isEmpty()) {
            throw ApiException.solicitudInvalida(MensajeException.TORNEO_SIN_GRUPOS);
        }

        List<Jugador> clasificados = new ArrayList<>();
        for (Grupo grupo : grupos) {
            List<Clasificacion> tabla = ordenarClasificacion(clasificacionRepository.findByGrupo_Id(grupo.getId()));
            if (tabla.size() < 2) {
                throw ApiException.solicitudInvalida(
                        String.format("El grupo %s no tiene suficientes jugadores para pasar a la eliminatoria", grupo.getNombre()));
            }
            clasificados.add(tabla.get(0).getJugador());
            clasificados.add(tabla.get(1).getJugador());
        }

        FaseTorneo faseInicial = faseInicialEliminatoria(clasificados.size());
        List<Partido> todos = new ArrayList<>();
        List<Partido> ronda = new ArrayList<>();
        int mitad = clasificados.size() / 2;
        for (int i = 0; i < mitad; i++) {
            ronda.add(crearPartidoLlaveInicial(torneo, faseInicial,
                    clasificados.get(i), clasificados.get(i + mitad)));
        }
        todos.addAll(ronda);

        FaseTorneo fase = faseInicial;
        while (ronda.size() > 1) {
            fase = faseSiguiente(fase);
            List<Partido> siguiente = new ArrayList<>();
            for (int k = 0; k < ronda.size(); k += 2) {
                siguiente.add(crearPartidoLlave(torneo, fase, ronda.get(k), ronda.get(k + 1)));
            }
            ronda = siguiente;
            todos.addAll(ronda);
        }

        torneo.setFaseActual(faseInicial);
        torneoRepository.save(torneo);
        return todos.stream().map(this::toPartidoResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidoResponseDto> proximaRonda(Long torneoId) {
        buscarTorneo(torneoId);
        return partidoRepository.findByTorneo_IdAndEstadoOrderById(torneoId, EstadoPartido.PROGRAMADO).stream()
                .map(this::toPartidoResponse)
                .toList();
    }

    private void asignarJugadores(List<Grupo> grupos, List<Jugador> participantes,
                                  AsignacionesGruposRequestDto request) {
        switch (request.tipoAsignacion()) {
            case ALEATORIO -> asignarAleatorio(grupos, participantes);
            case EQUILIBRADO -> asignarEquilibrado(grupos, participantes);
            case MANUAL -> asignarManual(grupos, participantes, request.asignaciones());
            default -> throw ApiException.solicitudInvalida("El tipo de asignación no es válido");
        }
    }

    private void asignarAleatorio(List<Grupo> grupos, List<Jugador> participantes) {
        List<Jugador> jugadores = new ArrayList<>(participantes);
        Collections.shuffle(jugadores);
        for (int i = 0; i < jugadores.size(); i++) {
            crearClasificacion(grupos.get(i % grupos.size()), jugadores.get(i));
        }
    }

    private void asignarEquilibrado(List<Grupo> grupos, List<Jugador> participantes) {
        List<Jugador> jugadores = new ArrayList<>(participantes);
        jugadores.sort(Comparator.comparingInt(
                (Jugador j) -> j.getTrofeos() == null ? 0 : j.getTrofeos().size()).reversed());
        for (int i = 0; i < jugadores.size(); i++) {
            crearClasificacion(grupos.get(i % grupos.size()), jugadores.get(i));
        }
    }

    private void asignarManual(List<Grupo> grupos, List<Jugador> participantes,
                               List<AsignacionJugadorGrupoDto> asignaciones) {
        if (asignaciones == null || asignaciones.isEmpty()) {
            throw ApiException.solicitudInvalida("Para la asignación MANUAL debe enviar la lista de asignaciones");
        }
        Map<String, Grupo> gruposPorNombre = grupos.stream()
                .collect(Collectors.toMap(Grupo::getNombre, g -> g));
        Map<Long, Jugador> jugadoresPorId = participantes.stream()
                .collect(Collectors.toMap(Jugador::getId, j -> j));
        Set<Long> asignados = new HashSet<>();

        for (AsignacionJugadorGrupoDto asignacion : asignaciones) {
            if (!jugadoresPorId.containsKey(asignacion.jugadorId())) {
                throw ApiException.noEncontrado(String.format(
                        MensajeException.VALOR_NO_ENCONTRADO, "jugador", asignacion.jugadorId()));
            }
            Grupo grupo = gruposPorNombre.get(asignacion.nombreGrupo());
            if (grupo == null) {
                throw ApiException.noEncontrado(String.format(
                        MensajeException.VALOR_NO_ENCONTRADO, "grupo", asignacion.nombreGrupo()));
            }
            if (!asignados.add(asignacion.jugadorId())) {
                throw ApiException.solicitudInvalida("El jugador " + asignacion.jugadorId() + " ya fue asignado");
            }
            crearClasificacion(grupo, jugadoresPorId.get(asignacion.jugadorId()));
        }
        if (asignados.size() != participantes.size()) {
            throw ApiException.solicitudInvalida("Debe asignar todos los jugadores inscritos al torneo");
        }
    }

    private Clasificacion crearClasificacion(Grupo grupo, Jugador jugador) {
        Clasificacion clasificacion = new Clasificacion();
        clasificacion.setGrupo(grupo);
        clasificacion.setJugador(jugador);
        clasificacion.setPartidosJugados(0);
        clasificacion.setGanados(0);
        clasificacion.setEmpatados(0);
        clasificacion.setPerdidos(0);
        clasificacion.setGolesAFavor(0);
        clasificacion.setGolesEnContra(0);
        clasificacion.setPuntos(0);
        return clasificacionRepository.save(clasificacion);
    }

    private Partido crearPartidoGrupo(Torneo torneo, Grupo grupo, Jugador jugadorUno, Jugador jugadorDos) {
        Partido partido = new Partido();
        partido.setTorneo(torneo);
        partido.setTipoPartido(TipoPartido.TORNEO);
        partido.setFecha(LocalDate.now());
        partido.setFase(FaseTorneo.GRUPOS);
        partido.setGrupo(grupo);
        partido.setEstado(EstadoPartido.PROGRAMADO);
        partido = partidoRepository.save(partido);
        crearParticipacion(partido, crearBando(1), jugadorUno);
        crearParticipacion(partido, crearBando(2), jugadorDos);
        return partido;
    }

    private Partido crearPartidoLlaveInicial(Torneo torneo, FaseTorneo fase, Jugador jugadorUno, Jugador jugadorDos) {
        Partido partido = crearPartidoLlave(torneo, fase, null, null);
        crearParticipacion(partido, crearBando(1), jugadorUno);
        crearParticipacion(partido, crearBando(2), jugadorDos);
        return partido;
    }

    private Partido crearPartidoLlave(Torneo torneo, FaseTorneo fase, Partido fuenteUno, Partido fuenteDos) {
        Partido partido = new Partido();
        partido.setTorneo(torneo);
        partido.setTipoPartido(TipoPartido.TORNEO);
        partido.setFecha(LocalDate.now());
        partido.setFase(fase);
        partido.setEstado(EstadoPartido.PROGRAMADO);
        partido.setFuenteUno(fuenteUno);
        partido.setFuenteDos(fuenteDos);
        return partidoRepository.save(partido);
    }

    private Bando crearBando(int numeroLado) {
        Bando bando = new Bando();
        bando.setNumeroLado(numeroLado);
        return bandoRepository.save(bando);
    }

    private ParticipacionPartido crearParticipacion(Partido partido, Bando bando, Jugador jugador) {
        ParticipacionPartido participacion = new ParticipacionPartido();
        participacion.setPartido(partido);
        participacion.setBando(bando);
        participacion.setJugador(jugador);
        participacion.setEquipo(jugador.getUsuario().getUsername());
        participacion.setEstadoConfirmacion(EstadoConfirmacion.CONFIRMADO);
        return participacionPartidoRepository.save(participacion);
    }

    private List<Clasificacion> ordenarClasificacion(List<Clasificacion> clasificacion) {
        return clasificacion.stream()
                .sorted(Comparator
                        .comparingInt(Clasificacion::getPuntos).reversed()
                        .thenComparingInt(c -> c.getGolesAFavor() - c.getGolesEnContra()).reversed()
                        .thenComparingInt(Clasificacion::getGolesAFavor).reversed())
                .toList();
    }

    private FaseTorneo faseInicialEliminatoria(int total) {
        if (total == 2) return FaseTorneo.FINAL;
        if (total == 4) return FaseTorneo.SEMIFINALES;
        if (total == 8) return FaseTorneo.CUARTOS;
        if (total == 16) return FaseTorneo.OCTAVOS;
        throw ApiException.solicitudInvalida(
                "La cantidad de clasificados (" + total + ") no forma una llave válida");
    }

    private FaseTorneo faseSiguiente(FaseTorneo fase) {
        return switch (fase) {
            case OCTAVOS -> FaseTorneo.CUARTOS;
            case CUARTOS -> FaseTorneo.SEMIFINALES;
            case SEMIFINALES -> FaseTorneo.FINAL;
            default -> throw ApiException.solicitudInvalida("No hay fase siguiente para " + fase);
        };
    }

    private String nombreGrupo(int indice) {
        if (indice < 26) {
            return String.valueOf((char) ('A' + indice));
        }
        return "G" + (indice - 26 + 1);
    }

    private Torneo buscarTorneo(Long id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "torneo", id)));
    }

    private Grupo buscarGrupo(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "grupo", id)));
    }

    private GrupoResponseDto toGrupoResponse(Grupo grupo) {
        return new GrupoResponseDto(grupo.getId(), grupo.getTorneo().getId(), grupo.getNombre());
    }

    private ClasificacionResponseDto toClasificacionResponse(Clasificacion clasificacion) {
        return new ClasificacionResponseDto(
                clasificacion.getGrupo().getId(),
                clasificacion.getGrupo().getNombre(),
                clasificacion.getJugador().getId(),
                clasificacion.getJugador().getUsuario().getUsername(),
                clasificacion.getPartidosJugados(),
                clasificacion.getGanados(),
                clasificacion.getEmpatados(),
                clasificacion.getPerdidos(),
                clasificacion.getGolesAFavor(),
                clasificacion.getGolesEnContra(),
                clasificacion.getPuntos()
        );
    }

    private PartidoResponseDto toPartidoResponse(Partido partido) {
        Torneo torneo = partido.getTorneo();
        Bando ganador = partido.getGanador();
        return new PartidoResponseDto(
                partido.getId(),
                partido.getResultado(),
                ganador != null ? ganador.getId() : null,
                torneo != null ? torneo.getId() : null,
                partido.getTipoPartido(),
                partido.getEstadoConfirmacion(),
                partido.getFecha(),
                partido.getFase(),
                partido.getGrupo() != null ? partido.getGrupo().getId() : null,
                partido.getEstado()
        );
    }
}