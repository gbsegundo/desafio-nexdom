package com.nexdom.estoque.application.dto;

import com.nexdom.estoque.domain.model.TipoProduto;

import java.math.BigDecimal;

public record LucroProdutoResponse(
        Long id,
        String codigo,
        String descricao,
        TipoProduto tipoProduto,
        int quantidadeTotalSaida,
        BigDecimal totalLucro
) {
}
