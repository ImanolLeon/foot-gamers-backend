package org.example.footgamers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.BandoRequestDto;
import org.example.footgamers.dto.response.BandoResponseDto;
import org.example.footgamers.service.reglas.IBando;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bando")
@RequiredArgsConstructor
public class BandoController {

    private final IBando bandoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BandoResponseDto> crear(@Valid @RequestBody BandoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bandoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<Page<BandoResponseDto>> obtenerTodos(
            @RequestParam(defaultValue = "0") Long pagina,
            @RequestParam(defaultValue = "10") Long tamano) {
        return ResponseEntity.ok(bandoService.obtenerTodos(pagina, tamano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BandoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bandoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BandoResponseDto> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody BandoRequestDto request) {
        return ResponseEntity.ok(bandoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        bandoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}