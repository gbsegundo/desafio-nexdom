package com.nexdom.estoque.application.movimentacao;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import org.springframework.stereotype.Component;

@Component
public class ProcessadorEntrada implements ProcessadorMovimentacao {

    @Override
    public TipoMovimentacao tipo() {
        return TipoMovimentacao.ENTRADA;
    }

    @Override
    public void aplicar(Produto produto, MovimentoEstoque movimento) {
        produto.adicionarEstoque(movimento.getQuantidade());
    }
}
