package com.nexdom.estoque.application.mapper;

import com.nexdom.estoque.application.dto.MovimentoResponse;
import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;

public final class MovimentoMapper {

    private MovimentoMapper() {
    }

    public static MovimentoResponse toResponse(MovimentoEstoque movimento) {
        Produto produto = movimento.getProduto();
        return new MovimentoResponse(
                movimento.getId(),
                produto.getId(),
                produto.getCodigo(),
                produto.getDescricao(),
                movimento.getTipoMovimentacao(),
                movimento.getValorVenda(),
                movimento.getDataVenda(),
                movimento.getQuantidade()
        );
    }
}
