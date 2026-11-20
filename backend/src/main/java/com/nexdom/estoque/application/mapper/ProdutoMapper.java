package com.nexdom.estoque.application.mapper;

import com.nexdom.estoque.application.dto.ProdutoResponse;
import com.nexdom.estoque.domain.model.Produto;

public final class ProdutoMapper {

    private ProdutoMapper() {
    }

    public static ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getCodigo(),
                produto.getDescricao(),
                produto.getTipoProduto(),
                produto.getValorFornecedor(),
                produto.getQuantidadeEstoque()
        );
    }
}
