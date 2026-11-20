package com.nexdom.estoque.application.movimentacao;

import com.nexdom.estoque.domain.exception.SaldoInsuficienteException;
import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.domain.model.TipoProduto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessadorMovimentacaoTest {

    @Test
    void entradaSomaAQuantidadeNoEstoque() {
        Produto produto = Produto.novo("GL-1", "Geladeira", TipoProduto.ELETRODOMESTICO, new BigDecimal("800"), 1);
        MovimentoEstoque movimento = MovimentoEstoque.registrar(
                produto, TipoMovimentacao.ENTRADA, null, LocalDateTime.now(), 4);

        new ProcessadorEntrada().aplicar(produto, movimento);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(5);
    }

    @Test
    void saidaSemSaldoDevolveMensagemEspecificaEPreservaOEstoque() {
        Produto produto = Produto.novo("TV-1", "TV", TipoProduto.ELETRONICO, new BigDecimal("1000"), 2);
        MovimentoEstoque movimento = MovimentoEstoque.registrar(
                produto, TipoMovimentacao.SAIDA, new BigDecimal("1500"), LocalDateTime.now(), 5);

        assertThatThrownBy(() -> new ProcessadorSaida().aplicar(produto, movimento))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo insuficiente para o produto TV-1. Quantidade disponível: 2. Quantidade solicitada: 5.");
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(2);
    }
}
