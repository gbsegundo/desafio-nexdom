package com.nexdom.estoque.application.movimentacao;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;

public interface ProcessadorMovimentacao {

    TipoMovimentacao tipo();

    void aplicar(Produto produto, MovimentoEstoque movimento);
}
