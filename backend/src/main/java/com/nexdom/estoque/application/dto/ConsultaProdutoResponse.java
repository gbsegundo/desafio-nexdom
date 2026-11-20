package com.nexdom.estoque.application.dto;

import com.nexdom.estoque.domain.model.TipoProduto;

public record ConsultaProdutoResponse(
        Long id,
        String codigo,
        String descricao,
        TipoProduto tipoProduto,
        int quantidadeSaida,
        int quantidadeDisponivel
) {
}
