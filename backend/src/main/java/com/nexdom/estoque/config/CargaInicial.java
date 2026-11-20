package com.nexdom.estoque.config;

import com.nexdom.estoque.application.dto.MovimentoRequest;
import com.nexdom.estoque.application.dto.ProdutoRequest;
import com.nexdom.estoque.application.dto.ProdutoResponse;
import com.nexdom.estoque.application.service.MovimentoEstoqueService;
import com.nexdom.estoque.application.service.ProdutoService;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.domain.model.TipoProduto;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class CargaInicial implements ApplicationRunner {

    private final ProdutoService produtoService;
    private final MovimentoEstoqueService movimentoEstoqueService;

    public CargaInicial(ProdutoService produtoService, MovimentoEstoqueService movimentoEstoqueService) {
        this.produtoService = produtoService;
        this.movimentoEstoqueService = movimentoEstoqueService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!produtoService.listar().isEmpty()) {
            return;
        }

        ProdutoResponse notebook = produtoService.criar(new ProdutoRequest(
                "NB-001",
                "Notebook 14 polegadas",
                TipoProduto.ELETRONICO,
                new BigDecimal("2500.00"),
                10
        ));
        movimento(notebook.id(), TipoMovimentacao.SAIDA, "3200.00", LocalDateTime.of(2026, 10, 20, 11, 0), 3);
        movimento(notebook.id(), TipoMovimentacao.SAIDA, "3000.00", LocalDateTime.of(2026, 11, 2, 15, 30), 1);

        ProdutoResponse geladeira = produtoService.criar(new ProdutoRequest(
                "GL-010",
                "Geladeira frost free",
                TipoProduto.ELETRODOMESTICO,
                new BigDecimal("1800.00"),
                5
        ));
        movimento(geladeira.id(), TipoMovimentacao.SAIDA, "2499.90", LocalDateTime.of(2026, 10, 28, 9, 15), 2);

        produtoService.criar(new ProdutoRequest(
                "MV-220",
                "Mesa de jantar 6 lugares",
                TipoProduto.MOVEL,
                new BigDecimal("450.00"),
                8
        ));

        ProdutoResponse televisor = produtoService.criar(new ProdutoRequest(
                "TV-440",
                "Smart TV 55 polegadas",
                TipoProduto.ELETRONICO,
                new BigDecimal("2100.00"),
                4
        ));
        movimento(televisor.id(), TipoMovimentacao.ENTRADA, null, LocalDateTime.of(2026, 11, 10, 8, 0), 2);
    }

    private void movimento(Long produtoId, TipoMovimentacao tipo, String valorVenda, LocalDateTime data, int quantidade) {
        movimentoEstoqueService.registrar(new MovimentoRequest(
                produtoId,
                tipo,
                valorVenda == null ? null : new BigDecimal(valorVenda),
                data,
                quantidade
        ));
    }
}
