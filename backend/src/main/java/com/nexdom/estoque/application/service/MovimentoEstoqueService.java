package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.MovimentoRequest;
import com.nexdom.estoque.application.dto.MovimentoResponse;

import java.util.List;

public interface MovimentoEstoqueService {

    MovimentoResponse registrar(MovimentoRequest request);

    List<MovimentoResponse> listar(Long produtoId);
}
