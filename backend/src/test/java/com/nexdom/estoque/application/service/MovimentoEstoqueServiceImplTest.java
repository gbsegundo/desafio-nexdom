package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.MovimentoRequest;
import com.nexdom.estoque.application.movimentacao.ProcessadorEntrada;
import com.nexdom.estoque.application.movimentacao.ProcessadorSaida;
import com.nexdom.estoque.domain.exception.SaldoInsuficienteException;
import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.domain.model.TipoProduto;
import com.nexdom.estoque.infrastructure.persistence.MovimentoEstoqueRepository;
import com.nexdom.estoque.infrastructure.persistence.ProdutoRepository;
import com.nexdom.estoque.support.ProdutoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentoEstoqueServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    private MovimentoEstoqueServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MovimentoEstoqueServiceImpl(
                produtoRepository,
                movimentoEstoqueRepository,
                List.of(new ProcessadorEntrada(), new ProcessadorSaida())
        );
    }

    @Test
    void entradaAumentaOSaldoEGravaOMovimento() {
        Produto produto = produtoComSaldo(3);
        when(produtoRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registrar(new MovimentoRequest(
                1L, TipoMovimentacao.ENTRADA, new BigDecimal("10"), LocalDateTime.of(2026, 11, 1, 9, 0), 2
        ));

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(5);
        assertThat(response.tipoMovimentacao()).isEqualTo(TipoMovimentacao.ENTRADA);
        assertThat(response.valorVenda()).isNull();
        assertThat(response.quantidade()).isEqualTo(2);
    }

    @Test
    void saidaComSaldoDiminuiOEstoque() {
        Produto produto = produtoComSaldo(10);
        when(produtoRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registrar(new MovimentoRequest(
                1L, TipoMovimentacao.SAIDA, new BigDecimal("3200"), LocalDateTime.of(2026, 11, 2, 10, 0), 4
        ));

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(6);
        assertThat(response.valorVenda()).isEqualByComparingTo("3200.00");
        assertThat(response.produtoCodigo()).isEqualTo("NB-01");
    }

    @Test
    void saidaSemSaldoNaoGravaMovimento() {
        Produto produto = produtoComSaldo(2);
        when(produtoRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.registrar(new MovimentoRequest(
                1L, TipoMovimentacao.SAIDA, new BigDecimal("1500"), LocalDateTime.now(), 5
        )))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo insuficiente para o produto NB-01. Quantidade disponível: 2. Quantidade solicitada: 5.");

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(2);
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    private Produto produtoComSaldo(int saldo) {
        return ProdutoFixture.comId(
                Produto.novo("NB-01", "Notebook", TipoProduto.ELETRONICO, new BigDecimal("2500"), saldo),
                1L
        );
    }
}
