package com.nexdom.estoque.domain.service;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.domain.model.TipoProduto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraLucroTest {

    private final CalculadoraLucro calculadora = new CalculadoraLucro();

    @Test
    void somaQuantidadeELucroApenasDasSaidas() {
        Produto produto = Produto.novo("NB-1", "Notebook", TipoProduto.ELETRONICO, new BigDecimal("10.00"), 10);
        LocalDateTime data = LocalDateTime.of(2026, 11, 1, 10, 0);
        MovimentoEstoque primeiraSaida = MovimentoEstoque.registrar(
                produto, TipoMovimentacao.SAIDA, new BigDecimal("15.00"), data, 2);
        MovimentoEstoque segundaSaida = MovimentoEstoque.registrar(
                produto, TipoMovimentacao.SAIDA, new BigDecimal("12.00"), data.plusDays(1), 1);
        MovimentoEstoque entrada = MovimentoEstoque.registrar(
                produto, TipoMovimentacao.ENTRADA, new BigDecimal("99.00"), data, 4);

        LucroCalculado resultado = calculadora.calcular(produto, List.of(primeiraSaida, segundaSaida, entrada));

        assertThat(resultado.quantidadeTotalSaida()).isEqualTo(3);
        assertThat(resultado.totalLucro()).isEqualByComparingTo("12.00");
    }

    @Test
    void produtoSemSaidaTemLucroZero() {
        Produto produto = Produto.novo("MV-1", "Mesa", TipoProduto.MOVEL, new BigDecimal("40.00"), 2);

        LucroCalculado resultado = calculadora.calcular(produto, List.of());

        assertThat(resultado.quantidadeTotalSaida()).isZero();
        assertThat(resultado.totalLucro()).isEqualByComparingTo("0.00");
    }
}
