package com.nexdom.estoque.api.controller;

import com.nexdom.estoque.application.dto.ConsultaProdutoResponse;
import com.nexdom.estoque.application.dto.LucroProdutoResponse;
import com.nexdom.estoque.application.service.ConsultaEstoqueService;
import com.nexdom.estoque.domain.model.TipoProduto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaEstoqueController {

    private final ConsultaEstoqueService consultaEstoqueService;

    public ConsultaEstoqueController(ConsultaEstoqueService consultaEstoqueService) {
        this.consultaEstoqueService = consultaEstoqueService;
    }

    @GetMapping("/produtos")
    public List<ConsultaProdutoResponse> consultarPorTipo(@RequestParam TipoProduto tipo) {
        return consultaEstoqueService.consultarPorTipo(tipo);
    }

    @GetMapping("/lucro")
    public List<LucroProdutoResponse> consultarLucro() {
        return consultaEstoqueService.consultarLucro();
    }

    @GetMapping("/lucro/{produtoId}")
    public LucroProdutoResponse consultarLucroPorProduto(@PathVariable Long produtoId) {
        return consultaEstoqueService.consultarLucroPorProduto(produtoId);
    }
}
