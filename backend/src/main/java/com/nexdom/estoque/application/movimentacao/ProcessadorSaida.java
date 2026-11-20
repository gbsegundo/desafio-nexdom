package com.nexdom.estoque.application.movimentacao;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import org.springframework.stereotype.Component;

@Component
public class ProcessadorSaida implements ProcessadorMovimentacao {

    @Override
    public TipoMovimentacao tipo() {
        return TipoMovimentacao.SAIDA;
    }

    @Override
    public void aplicar(Produto produto, MovimentoEstoque movimento) {
        produto.retirarEstoque(movimento.getQuantidade());
    }
}
