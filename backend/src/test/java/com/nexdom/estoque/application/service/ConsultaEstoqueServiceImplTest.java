package com.nexdom.estoque.application.service;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.domain.model.TipoProduto;
import com.nexdom.estoque.domain.service.CalculadoraLucro;
import com.nexdom.estoque.infrastructure.persistence.MovimentoEstoqueRepository;
import com.nexdom.estoque.infrastructure.persistence.ProdutoRepository;
import com.nexdom.estoque.support.ProdutoFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaEstoqueServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Spy
    private CalculadoraLucro calculadoraLucro = new CalculadoraLucro();

    @InjectMocks
    private ConsultaEstoqueServiceImpl consultaEstoqueService;

    @Test
    void consultaPorTipoInformaSaidaEQuantidadeDisponivel() {
        Produto notebook = ProdutoFixture.comId(
                Produto.novo("NB-01", "Notebook", TipoProduto.ELETRONICO, new BigDecimal("2500"), 6),
                1L
        );
        Produto televisor = ProdutoFixture.comId(
                Produto.novo("TV-01", "TV", TipoProduto.ELETRONICO, new BigDecimal("2100"), 4),
                2L
        );
        MovimentoEstoque saida = MovimentoEstoque.registrar(
                notebook, TipoMovimentacao.SAIDA, new BigDecimal("3200"), LocalDateTime.of(2026, 11, 2, 10, 0), 4
        );

        when(produtoRepository.findByTipoProdutoOrderByCodigoAsc(TipoProduto.ELETRONICO))
                .thenReturn(List.of(notebook, televisor));
        when(movimentoEstoqueRepository.findByTipoComProduto(TipoMovimentacao.SAIDA))
                .thenReturn(List.of(saida));

        var consulta = consultaEstoqueService.consultarPorTipo(TipoProduto.ELETRONICO);

        assertThat(consulta).hasSize(2);
        assertThat(consulta.get(0).codigo()).isEqualTo("NB-01");
        assertThat(consulta.get(0).quantidadeSaida()).isEqualTo(4);
        assertThat(consulta.get(0).quantidadeDisponivel()).isEqualTo(6);
        assertThat(consulta.get(1).codigo()).isEqualTo("TV-01");
        assertThat(consulta.get(1).quantidadeSaida()).isZero();
        assertThat(consulta.get(1).quantidadeDisponivel()).isEqualTo(4);
    }

    @Test
    void lucroUsaValorDeVendaMenosValorDoFornecedor() {
        Produto notebook = ProdutoFixture.comId(
                Produto.novo("NB-01", "Notebook", TipoProduto.ELETRONICO, new BigDecimal("2500"), 6),
                1L
        );
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(notebook));
        when(movimentoEstoqueRepository.findByProdutoIdAndTipoMovimentacao(1L, TipoMovimentacao.SAIDA))
                .thenReturn(List.of(
                        MovimentoEstoque.registrar(notebook, TipoMovimentacao.SAIDA, new BigDecimal("3200"), LocalDateTime.now(), 3),
                        MovimentoEstoque.registrar(notebook, TipoMovimentacao.SAIDA, new BigDecimal("3000"), LocalDateTime.now(), 1)
                ));

        var lucro = consultaEstoqueService.consultarLucroPorProduto(1L);

        assertThat(lucro.quantidadeTotalSaida()).isEqualTo(4);
        assertThat(lucro.totalLucro()).isEqualByComparingTo("2600.00");
    }
}
