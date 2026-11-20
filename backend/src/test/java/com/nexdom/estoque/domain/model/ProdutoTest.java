package com.nexdom.estoque.domain.model;

import com.nexdom.estoque.domain.exception.RegraNegocioException;
import com.nexdom.estoque.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoTest {

    @Test
    void normalizaCodigoEMantemSaldoInicial() {
        Produto produto = Produto.novo(" nb-01 ", " Notebook ", TipoProduto.ELETRONICO, new BigDecimal("10.5"), 4);

        assertThat(produto.getCodigo()).isEqualTo("NB-01");
        assertThat(produto.getDescricao()).isEqualTo("Notebook");
        assertThat(produto.getValorFornecedor()).isEqualByComparingTo("10.50");
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(4);
    }

    @Test
    void entradaAumentaSaldoESaidaDiminui() {
        Produto produto = Produto.novo("TV-1", "TV", TipoProduto.ELETRONICO, new BigDecimal("100"), 2);

        produto.adicionarEstoque(3);
        produto.retirarEstoque(4);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(1);
    }

    @Test
    void saidaAcimaDoSaldoNaoAlteraEstoqueEInformaAQuantidade() {
        Produto produto = Produto.novo("TV-1", "TV", TipoProduto.ELETRONICO, new BigDecimal("100"), 2);

        assertThatThrownBy(() -> produto.retirarEstoque(5))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo insuficiente para o produto TV-1. Quantidade disponível: 2. Quantidade solicitada: 5.");
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(2);
    }

    @Test
    void atualizacaoNaoMexeNoSaldo() {
        Produto produto = Produto.novo("TV-1", "TV", TipoProduto.ELETRONICO, new BigDecimal("100"), 8);

        produto.atualizar("tv-2", "TV 50", TipoProduto.ELETRODOMESTICO, new BigDecimal("120"));

        assertThat(produto.getCodigo()).isEqualTo("TV-2");
        assertThat(produto.getDescricao()).isEqualTo("TV 50");
        assertThat(produto.getTipoProduto()).isEqualTo(TipoProduto.ELETRODOMESTICO);
        assertThat(produto.getValorFornecedor()).isEqualByComparingTo("120.00");
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(8);
    }

    @Test
    void rejeitaValorNegativo() {
        assertThatThrownBy(() -> Produto.novo("TV-1", "TV", TipoProduto.ELETRONICO, new BigDecimal("-1"), 0))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O valor no fornecedor não pode ser negativo.");
    }
}
