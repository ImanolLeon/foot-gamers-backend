package org.example.footgamers.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.JugadorRequestDto;
import org.example.footgamers.dto.response.JugadorResponseDto;
import org.example.footgamers.dto.response.PartidoResponseDto;
import org.example.footgamers.dto.response.TorneoResponseDto;
import org.example.footgamers.dto.response.TrofeoResponseDto;
import org.example.footgamers.service.reglas.IJugador;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final IJugador jugadorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<JugadorResponseDto> crear(@Valid @RequestBody JugadorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jugadorService.crear(request));
    }

    @GetMapping
    public ResponseEntity<Page<JugadorResponseDto>> obtenerTodos(
            @RequestParam(defaultValue = "0") Long pagina,
            @RequestParam(defaultValue = "10") Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerTodos(pagina, tamano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JugadorResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(jugadorService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<JugadorResponseDto> obtenerPorUsername(@RequestParam String username) {
        return ResponseEntity.ok(jugadorService.obtenerPorUsername(username));
    }

    @GetMapping("/mis-trofeos")
    public ResponseEntity<Page<TrofeoResponseDto>> obtenerMisTrofeos(
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerMisTrofeos(pagina, tamano));
    }

    @GetMapping("/mis-torneos")
    public ResponseEntity<Page<TorneoResponseDto>> obtenerMisTorneos(
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerMisTorneos(pagina, tamano));
    }

    @GetMapping("/mis-partidos")
    public ResponseEntity<Page<PartidoResponseDto>> obtenerMisPartidos(
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerMisPartidos(pagina, tamano));
    }

    @GetMapping("/{id}/trofeos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<TrofeoResponseDto>> obtenerTrofeosDeJugador(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerTrofeosDeJugador(id, pagina, tamano));
    }

    @GetMapping("/{id}/torneos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<TorneoResponseDto>> obtenerTorneosDeJugador(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerTorneosDeJugador(id, pagina, tamano));
    }

    @GetMapping("/{id}/partidos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<PartidoResponseDto>> obtenerPartidosDeJugador(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(jugadorService.obtenerPartidosDeJugador(id, pagina, tamano));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<JugadorResponseDto> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody JugadorRequestDto request) {
        return ResponseEntity.ok(jugadorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        jugadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
