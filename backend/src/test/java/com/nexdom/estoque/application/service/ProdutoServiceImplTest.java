package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.ProdutoAtualizacaoRequest;
import com.nexdom.estoque.application.dto.ProdutoRequest;
import com.nexdom.estoque.domain.exception.ConflitoDadosException;
import com.nexdom.estoque.domain.exception.RecursoNaoEncontradoException;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoProduto;
import com.nexdom.estoque.infrastructure.persistence.MovimentoEstoqueRepository;
import com.nexdom.estoque.infrastructure.persistence.ProdutoRepository;
import com.nexdom.estoque.support.ProdutoFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @InjectMocks
    private ProdutoServiceImpl produtoService;

    @Test
    void criaProdutoComCodigoNormalizado() {
        when(produtoRepository.existsByCodigo("NB-01")).thenReturn(false);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = produtoService.criar(new ProdutoRequest(
                "nb-01",
                "Notebook",
                TipoProduto.ELETRONICO,
                new BigDecimal("2500"),
                10
        ));

        assertThat(response.codigo()).isEqualTo("NB-01");
        assertThat(response.quantidadeEstoque()).isEqualTo(10);
        assertThat(response.valorFornecedor()).isEqualByComparingTo("2500.00");
    }

    @Test
    void recusaCodigoDuplicado() {
        when(produtoRepository.existsByCodigo("NB-01")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.criar(new ProdutoRequest(
                "NB-01", "Notebook", TipoProduto.ELETRONICO, new BigDecimal("10"), 0
        )))
                .isInstanceOf(ConflitoDadosException.class)
                .hasMessage("Já existe um produto com o código NB-01.");
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void atualizacaoPreservaQuantidadeEmEstoque() {
        Produto produto = ProdutoFixture.comId(
                Produto.novo("NB-01", "Notebook", TipoProduto.ELETRONICO, new BigDecimal("10"), 6),
                1L
        );
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.existsByCodigoAndIdNot("NB-02", 1L)).thenReturn(false);

        var response = produtoService.atualizar(1L, new ProdutoAtualizacaoRequest(
                "nb-02", "Notebook novo", TipoProduto.ELETRONICO, new BigDecimal("12")
        ));

        assertThat(response.codigo()).isEqualTo("NB-02");
        assertThat(response.quantidadeEstoque()).isEqualTo(6);
    }

    @Test
    void exclusaoRemoveMovimentosAntesDoProduto() {
        when(produtoRepository.existsById(4L)).thenReturn(true);

        produtoService.excluir(4L);

        verify(movimentoEstoqueRepository).deleteByProdutoId(4L);
        verify(produtoRepository).deleteById(4L);
    }

    @Test
    void buscaInexistente() {
        when(produtoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscar(9L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado.");
    }
}
