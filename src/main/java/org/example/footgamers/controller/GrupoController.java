package org.example.footgamers.controller;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.response.ClasificacionResponseDto;
import org.example.footgamers.service.reglas.IGrupo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final IGrupo grupoService;

    @GetMapping("/{id}/clasificacion")
    public ResponseEntity<List<ClasificacionResponseDto>> clasificacion(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.clasificacion(id));
    }
}