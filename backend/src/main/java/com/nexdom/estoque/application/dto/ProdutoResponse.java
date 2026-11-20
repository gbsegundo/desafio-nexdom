package com.nexdom.estoque.application.dto;

import com.nexdom.estoque.domain.model.TipoProduto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String codigo,
        String descricao,
        TipoProduto tipoProduto,
        BigDecimal valorFornecedor,
        Integer quantidadeEstoque
) {
}
