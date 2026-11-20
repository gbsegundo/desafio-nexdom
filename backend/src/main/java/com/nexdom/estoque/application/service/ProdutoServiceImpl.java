package com.nexdom.estoque.application.service;

import com.nexdom.estoque.application.dto.ProdutoAtualizacaoRequest;
import com.nexdom.estoque.application.dto.ProdutoRequest;
import com.nexdom.estoque.application.dto.ProdutoResponse;
import com.nexdom.estoque.application.mapper.ProdutoMapper;
import com.nexdom.estoque.domain.exception.ConflitoDadosException;
import com.nexdom.estoque.domain.exception.RecursoNaoEncontradoException;
import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.infrastructure.persistence.MovimentoEstoqueRepository;
import com.nexdom.estoque.infrastructure.persistence.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    public ProdutoServiceImpl(
            ProdutoRepository produtoRepository,
            MovimentoEstoqueRepository movimentoEstoqueRepository
    ) {
        this.produtoRepository = produtoRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
    }

    @Override
    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        String codigo = Produto.normalizarCodigo(request.codigo());
        if (produtoRepository.existsByCodigo(codigo)) {
            throw new ConflitoDadosException("Já existe um produto com o código " + codigo + ".");
        }

        Produto produto = Produto.novo(
                codigo,
                request.descricao(),
                request.tipoProduto(),
                request.valorFornecedor(),
                request.quantidadeEstoque()
        );
        return ProdutoMapper.toResponse(produtoRepository.save(produto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAllByOrderByCodigoAsc().stream()
                .map(ProdutoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return ProdutoMapper.toResponse(buscarEntidade(id));
    }

    @Override
    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoAtualizacaoRequest request) {
        Produto produto = buscarEntidade(id);
        String codigo = Produto.normalizarCodigo(request.codigo());
        if (produtoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new ConflitoDadosException("Já existe um produto com o código " + codigo + ".");
        }
        produto.atualizar(codigo, request.descricao(), request.tipoProduto(), request.valorFornecedor());
        return ProdutoMapper.toResponse(produto);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado.");
        }
        movimentoEstoqueRepository.deleteByProdutoId(id);
        produtoRepository.deleteById(id);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }
}
