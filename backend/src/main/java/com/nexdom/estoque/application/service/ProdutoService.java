package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.ProdutoAtualizacaoRequest;
import com.nexdom.estoque.application.dto.ProdutoRequest;
import com.nexdom.estoque.application.dto.ProdutoResponse;

import java.util.List;

public interface ProdutoService {

    ProdutoResponse criar(ProdutoRequest request);

    List<ProdutoResponse> listar();

    ProdutoResponse buscar(Long id);

    ProdutoResponse atualizar(Long id, ProdutoAtualizacaoRequest request);

    void excluir(Long id);
}
