package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.MovimentoRequest;
import com.nexdom.estoque.application.dto.MovimentoResponse;
import com.nexdom.estoque.application.mapper.MovimentoMapper;
import com.nexdom.estoque.application.movimentacao.ProcessadorMovimentacao;
import com.nexdom.estoque.domain.exception.RecursoNaoEncontradoException;
import com.nexdom.estoque.domain.exception.RegraNegocioException;
import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import com.nexdom.estoque.infrastructure.persistence.MovimentoEstoqueRepository;
import com.nexdom.estoque.infrastructure.persistence.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class MovimentoEstoqueServiceImpl implements MovimentoEstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final Map<TipoMovimentacao, ProcessadorMovimentacao> processadores;

    public MovimentoEstoqueServiceImpl(
            ProdutoRepository produtoRepository,
            MovimentoEstoqueRepository movimentoEstoqueRepository,
            List<ProcessadorMovimentacao> processadores
    ) {
        this.produtoRepository = produtoRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
        this.processadores = new EnumMap<>(TipoMovimentacao.class);
        for (ProcessadorMovimentacao processador : processadores) {
            this.processadores.put(processador.tipo(), processador);
        }
    }

    @Override
    @Transactional
    public MovimentoResponse registrar(MovimentoRequest request) {
        Produto produto = produtoRepository.findByIdComBloqueio(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));

        ProcessadorMovimentacao processador = processadores.get(request.tipoMovimentacao());
        if (processador == null) {
            throw new RegraNegocioException("Tipo de movimentação não suportado.");
        }

        MovimentoEstoque movimento = MovimentoEstoque.registrar(
                produto,
                request.tipoMovimentacao(),
                request.valorVenda(),
                request.dataVenda(),
                request.quantidade()
        );
        processador.aplicar(produto, movimento);
        return MovimentoMapper.toResponse(movimentoEstoqueRepository.save(movimento));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentoResponse> listar(Long produtoId) {
        List<MovimentoEstoque> movimentos = produtoId == null
                ? movimentoEstoqueRepository.findAllComProduto()
                : movimentoEstoqueRepository.findByProdutoComProduto(produtoId);
        return movimentos.stream().map(MovimentoMapper::toResponse).toList();
    }
}
