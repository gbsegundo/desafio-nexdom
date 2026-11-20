package com.nexdom.estoque.domain.service;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Lucro de uma saída = (valor de venda - valor do fornecedor) x quantidade.
 * O total do produto é a soma das saídas. Entradas não entram no cálculo.
 */
public class CalculadoraLucro {

    public LucroCalculado calcular(Produto produto, List<MovimentoEstoque> movimentos) {
        int quantidade = 0;
        BigDecimal lucro = BigDecimal.ZERO;

        for (MovimentoEstoque movimento : movimentos) {
            if (movimento.getTipoMovimentacao() != TipoMovimentacao.SAIDA) {
                continue;
            }
            quantidade += movimento.getQuantidade();
            BigDecimal lucroUnitario = movimento.getValorVenda().subtract(produto.getValorFornecedor());
            lucro = lucro.add(lucroUnitario.multiply(BigDecimal.valueOf(movimento.getQuantidade())));
        }

        return new LucroCalculado(quantidade, lucro.setScale(2, RoundingMode.HALF_UP));
    }
}
