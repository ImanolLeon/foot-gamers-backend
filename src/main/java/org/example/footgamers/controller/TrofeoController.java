package org.example.footgamers.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.TrofeoRequestDto;
import org.example.footgamers.dto.response.TrofeoResponseDto;
import org.example.footgamers.service.reglas.ITrofeo;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trofeos")
@RequiredArgsConstructor
public class TrofeoController {

    private final ITrofeo trofeoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TrofeoResponseDto> crear(@Valid @RequestBody TrofeoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trofeoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<Page<TrofeoResponseDto>> obtenerTodos(
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) Long pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Long tamano) {
        return ResponseEntity.ok(trofeoService.obtenerTodos(pagina, tamano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrofeoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(trofeoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TrofeoResponseDto> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody TrofeoRequestDto request) {
        return ResponseEntity.ok(trofeoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        trofeoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
