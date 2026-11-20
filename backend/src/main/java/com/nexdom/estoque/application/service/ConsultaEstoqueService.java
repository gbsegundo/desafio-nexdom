package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.ConsultaProdutoResponse;
import com.nexdom.estoque.application.dto.LucroProdutoResponse;
import com.nexdom.estoque.domain.model.TipoProduto;

import java.util.List;

public interface ConsultaEstoqueService {

    List<ConsultaProdutoResponse> consultarPorTipo(TipoProduto tipoProduto);

    List<LucroProdutoResponse> consultarLucro();

    LucroProdutoResponse consultarLucroPorProduto(Long produtoId);
}
