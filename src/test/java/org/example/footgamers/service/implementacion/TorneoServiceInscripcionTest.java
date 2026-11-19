package org.example.footgamers.service.implementacion;

import org.example.footgamers.dto.request.TorneoRequestDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Un torneo recién creado queda en ABIERTO / INSCRIPCION, así que debe admitir
 * inscripciones. TERMINADO sigue rechazándose con su mensaje específico.
 */
class TorneoServiceInscripcionTest {

    private static final Long TORNEO_ID = 1L;
    private static final Long JUGADOR_ID = 2L;

    private final TorneoRepository torneoRepository = mock(TorneoRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final JugadorRepository jugadorRepository = mock(JugadorRepository.class);
    private final TrofeoRepository trofeoRepository = mock(TrofeoRepository.class);
    private final GrupoRepository grupoRepository = mock(GrupoRepository.class);
    private final IAuth authService = mock(IAuth.class);

    private TorneoService torneoService;

    @BeforeEach
    void setUp() {
        torneoService = new TorneoService(torneoRepository, categoriaRepository,
                jugadorRepository, trofeoRepository, grupoRepository, authService);
    }

    @Test
    void creaTorneoAbiertoEnFaseInscripcion() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria()));
        when(trofeoRepository.findById(20L)).thenReturn(Optional.of(trofeo()));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(inv -> {
            Torneo guardado = inv.getArgument(0);
            guardado.setId(TORNEO_ID);
            return guardado;
        });

        TorneoResponseDto response = torneoService.crear(new TorneoRequestDto(
                "Torneo nuevo", 10L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 20),
                16L, 20L, 4L));

        assertThat(response.estado()).isEqualTo(EstadoTorneo.ABIERTO);
        assertThat(response.faseActual()).isEqualTo(FaseTorneo.INSCRIPCION);
    }

    @Test
    void inscribeJugadorEnTorneoRecienCreadoAbierto() {
        precondicionesInscripcion(EstadoTorneo.ABIERTO);

        TorneoResponseDto response = torneoService.inscribirme(TORNEO_ID);

        assertThat(response.estado()).isEqualTo(EstadoTorneo.ABIERTO);
        ArgumentCaptor<Torneo> captor = ArgumentCaptor.forClass(Torneo.class);
        verify(torneoRepository).save(captor.capture());
        assertThat(captor.getValue().getParticipantes())
                .extracting(Jugador::getId)
                .containsExactly(JUGADOR_ID);
    }

    @Test
    void inscribeJugadorEnTorneoEnProcesoPorCompatibilidad() {
        precondicionesInscripcion(EstadoTorneo.EN_PROCESO);

        TorneoResponseDto response = torneoService.inscribirme(TORNEO_ID);

        assertThat(response.estado()).isEqualTo(EstadoTorneo.EN_PROCESO);
        verify(torneoRepository).save(any(Torneo.class));
    }

    @Test
    void rechazaInscripcionEnTorneoTerminado() {
        precondicionesInscripcion(EstadoTorneo.TERMINADO);

        assertThatThrownBy(() -> torneoService.inscribirme(TORNEO_ID))
                .isInstanceOf(ApiException.class)
                .hasMessage(MensajeException.TORNEO_TERMINADO);

        verify(torneoRepository, never()).save(any());
    }

    private void precondicionesInscripcion(EstadoTorneo estado) {
        when(torneoRepository.findById(TORNEO_ID)).thenReturn(Optional.of(torneo(estado)));
        when(authService.jugadorActual()).thenReturn(jugador());
        when(grupoRepository.findByTorneo_Id(TORNEO_ID)).thenReturn(List.of());
        when(torneoRepository.existsByIdAndParticipantes_Id(TORNEO_ID, JUGADOR_ID))
                .thenReturn(false);
        List<TorneoParticipantesProjection> conteo = List.of(inscritos(0L));
        when(torneoRepository.contarParticipantes(List.of(TORNEO_ID))).thenReturn(conteo);
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Torneo torneo(EstadoTorneo estado) {
        Torneo torneo = new Torneo();
        torneo.setId(TORNEO_ID);
        torneo.setNombre("Torneo de prueba");
        torneo.setCategoria(categoria());
        torneo.setTrofeo(trofeo());
        torneo.setCantidadJugadores(16L);
        torneo.setCantidadGrupos(4L);
        torneo.setEstado(estado);
        torneo.setFaseActual(FaseTorneo.INSCRIPCION);
        return torneo;
    }

    private static TorneoParticipantesProjection inscritos(long cantidad) {
        TorneoParticipantesProjection projection = mock(TorneoParticipantesProjection.class);
        when(projection.getInscritos()).thenReturn(cantidad);
        return projection;
    }

    private static Categoria categoria() {
        Categoria categoria = new Categoria();
        categoria.setId(10L);
        categoria.setNombre("Senior");
        return categoria;
    }

    private static Trofeo trofeo() {
        Trofeo trofeo = new Trofeo();
        trofeo.setId(20L);
        trofeo.setNombre("Copa");
        return trofeo;
    }

    private static Jugador jugador() {
        Jugador jugador = new Jugador();
        jugador.setId(JUGADOR_ID);
        return jugador;
    }
}
