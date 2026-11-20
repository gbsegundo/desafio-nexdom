package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.ConsultaProdutoResponse;
import com.nexdom.estoque.application.dto.LucroProdutoResponse;
import com.nexdom.estoque.domain.exception.RecursoNaoEncontradoException;
import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.domain.model.TipoProduto;
import com.nexdom.estoque.domain.service.CalculadoraLucro;
import com.nexdom.estoque.domain.service.LucroCalculado;
import com.nexdom.estoque.infrastructure.persistence.MovimentoEstoqueRepository;
import com.nexdom.estoque.infrastructure.persistence.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultaEstoqueServiceImpl implements ConsultaEstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final CalculadoraLucro calculadoraLucro;

    public ConsultaEstoqueServiceImpl(
            ProdutoRepository produtoRepository,
            MovimentoEstoqueRepository movimentoEstoqueRepository,
            CalculadoraLucro calculadoraLucro
    ) {
        this.produtoRepository = produtoRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
        this.calculadoraLucro = calculadoraLucro;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaProdutoResponse> consultarPorTipo(TipoProduto tipoProduto) {
        List<Produto> produtos = produtoRepository.findByTipoProdutoOrderByCodigoAsc(tipoProduto);
        Map<Long, List<MovimentoEstoque>> saidas = saidasPorProduto();

        return produtos.stream()
                .map(produto -> {
                    LucroCalculado calculado = calculadoraLucro.calcular(
                            produto,
                            saidas.getOrDefault(produto.getId(), List.of())
                    );
                    return new ConsultaProdutoResponse(
                            produto.getId(),
                            produto.getCodigo(),
                            produto.getDescricao(),
                            produto.getTipoProduto(),
                            calculado.quantidadeTotalSaida(),
                            produto.getQuantidadeEstoque()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LucroProdutoResponse> consultarLucro() {
        Map<Long, List<MovimentoEstoque>> saidas = saidasPorProduto();
        return produtoRepository.findAllByOrderByCodigoAsc().stream()
                .map(produto -> paraLucro(produto, saidas.getOrDefault(produto.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LucroProdutoResponse consultarLucroPorProduto(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        List<MovimentoEstoque> saidas = movimentoEstoqueRepository
                .findByProdutoIdAndTipoMovimentacao(produtoId, TipoMovimentacao.SAIDA);
        return paraLucro(produto, saidas);
    }

    private LucroProdutoResponse paraLucro(Produto produto, List<MovimentoEstoque> saidas) {
        LucroCalculado calculado = calculadoraLucro.calcular(produto, saidas);
        return new LucroProdutoResponse(
                produto.getId(),
                produto.getCodigo(),
                produto.getDescricao(),
                produto.getTipoProduto(),
                calculado.quantidadeTotalSaida(),
                calculado.totalLucro()
        );
    }

    private Map<Long, List<MovimentoEstoque>> saidasPorProduto() {
        return movimentoEstoqueRepository.findByTipoComProduto(TipoMovimentacao.SAIDA).stream()
                .collect(Collectors.groupingBy(movimento -> movimento.getProduto().getId()));
    }
}
