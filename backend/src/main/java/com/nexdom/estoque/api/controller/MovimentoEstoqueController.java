package com.nexdom.estoque.api.controller;

import com.nexdom.estoque.application.dto.MovimentoRequest;
import com.nexdom.estoque.application.dto.MovimentoResponse;
import com.nexdom.estoque.application.service.MovimentoEstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimentos")
public class MovimentoEstoqueController {

    private final MovimentoEstoqueService movimentoEstoqueService;

    public MovimentoEstoqueController(MovimentoEstoqueService movimentoEstoqueService) {
        this.movimentoEstoqueService = movimentoEstoqueService;
    }

    @PostMapping
    public ResponseEntity<MovimentoResponse> registrar(@Valid @RequestBody MovimentoRequest request) {
        return ResponseEntity.status(201).body(movimentoEstoqueService.registrar(request));
    }

    @GetMapping
    public List<MovimentoResponse> listar(@RequestParam(required = false) Long produtoId) {
        return movimentoEstoqueService.listar(produtoId);
    }
}
