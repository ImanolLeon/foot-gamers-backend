package org.example.footgamers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.PartidoRequestDto;
import org.example.footgamers.dto.response.PartidoResponseDto;
import org.example.footgamers.service.reglas.IPartido;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final IPartido partidoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDto> crear(@Valid @RequestBody PartidoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partidoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<Page<PartidoResponseDto>> obtenerTodos(
            @RequestParam(defaultValue = "0") Long pagina,
            @RequestParam(defaultValue = "10") Long tamano) {
        return ResponseEntity.ok(partidoService.obtenerTodos(pagina, tamano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(partidoService.obtenerPorId(id));
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<PartidoResponseDto>> listarPorTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(partidoService.listarPorTorneo(torneoId));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<PartidoResponseDto>> listarPorGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(partidoService.listarPorGrupo(grupoId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDto> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody PartidoRequestDto request) {
        return ResponseEntity.ok(partidoService.actualizar(id, request));
    }

    @PutMapping("/{id}/ganador/{bandoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDto> asignarGanadorBando(@PathVariable Long id,
                                                                  @PathVariable Long bandoId) {
        return ResponseEntity.ok(partidoService.asignarGanadorBando(id, bandoId));
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDto> confirmarPartido(@PathVariable Long id,
                                                               @RequestParam String resultado) {
        return ResponseEntity.ok(partidoService.confirmarPartido(id, resultado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        partidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}