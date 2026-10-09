package org.example.footgamers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.AsignacionesGruposRequestDto;
import org.example.footgamers.dto.request.CambiarEstadoTorneoRequestDto;
import org.example.footgamers.dto.request.TorneoRequestDto;
import org.example.footgamers.dto.response.ClasificacionResponseDto;
import org.example.footgamers.dto.response.GrupoResponseDto;
import org.example.footgamers.dto.response.PartidoResponseDto;
import org.example.footgamers.dto.response.TorneoInscripcionResponseDto;
import org.example.footgamers.dto.response.TorneoResponseDto;
import org.example.footgamers.service.reglas.IGrupo;
import org.example.footgamers.service.reglas.ITorneo;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/torneos")
@RequiredArgsConstructor
public class TorneoController {

    private final ITorneo torneoService;
    private final IGrupo grupoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TorneoResponseDto> crear(@Valid @RequestBody TorneoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(torneoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<Page<TorneoResponseDto>> obtenerTodos(
            @RequestParam(defaultValue = "0") Long pagina,
            @RequestParam(defaultValue = "10") Long tamano) {
        return ResponseEntity.ok(torneoService.obtenerTodos(pagina, tamano));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<Page<TorneoInscripcionResponseDto>> obtenerDisponibles(
            @RequestParam(defaultValue = "0") Long pagina,
            @RequestParam(defaultValue = "10") Long tamano) {
        return ResponseEntity.ok(torneoService.obtenerDisponibles(pagina, tamano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TorneoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TorneoResponseDto> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody TorneoRequestDto request) {
        return ResponseEntity.ok(torneoService.actualizar(id, request));
    }

    @PutMapping("/{id}/ganador/{jugadorId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TorneoResponseDto> asignarGanador(@PathVariable Long id,
                                                            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(torneoService.asignarGanador(id, jugadorId));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TorneoResponseDto> cambiarEstado(@PathVariable Long id,
                                                          @Valid @RequestBody CambiarEstadoTorneoRequestDto request) {
        return ResponseEntity.ok(torneoService.cambiarEstado(id, request));
    }

    @PostMapping("/{id}/jugadores/{jugadorId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TorneoResponseDto> inscribirJugador(@PathVariable Long id,
                                                              @PathVariable Long jugadorId) {
        return ResponseEntity.ok(torneoService.inscribirJugador(id, jugadorId));
    }

    @PostMapping("/{id}/inscripcion")
    public ResponseEntity<TorneoResponseDto> inscribirme(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.inscribirme(id));
    }

    @PostMapping("/{id}/grupos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<GrupoResponseDto>> crearGrupos(
            @PathVariable Long id,
            @Valid @RequestBody AsignacionesGruposRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.crearGrupos(id, request));
    }

    @GetMapping("/{id}/grupos")
    public ResponseEntity<List<GrupoResponseDto>> listarGrupos(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.listarGrupos(id));
    }

    @PostMapping("/{id}/partidos/grupos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PartidoResponseDto>> generarPartidosGrupos(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.generarPartidosGrupos(id));
    }

    @PostMapping("/{id}/llaves")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PartidoResponseDto>> generarLlaves(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.generarLlaves(id));
    }

    @GetMapping("/{id}/proximaRonda")
    public ResponseEntity<List<PartidoResponseDto>> proximaRonda(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.proximaRonda(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        torneoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}