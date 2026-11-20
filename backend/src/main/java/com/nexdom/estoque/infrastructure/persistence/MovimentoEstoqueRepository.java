package com.nexdom.estoque.infrastructure.persistence;

import com.nexdom.estoque.domain.model.MovimentoEstoque;
import com.nexdom.estoque.domain.model.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    @Query("""
            select m from MovimentoEstoque m
            join fetch m.produto
            order by m.dataVenda desc, m.id desc
            """)
    List<MovimentoEstoque> findAllComProduto();

    @Query("""
            select m from MovimentoEstoque m
            join fetch m.produto
            where m.produto.id = :produtoId
            order by m.dataVenda desc, m.id desc
            """)
    List<MovimentoEstoque> findByProdutoComProduto(@Param("produtoId") Long produtoId);

    @Query("""
            select m from MovimentoEstoque m
            join fetch m.produto
            where m.tipoMovimentacao = :tipo
            """)
    List<MovimentoEstoque> findByTipoComProduto(@Param("tipo") TipoMovimentacao tipo);

    List<MovimentoEstoque> findByProdutoIdAndTipoMovimentacao(Long produtoId, TipoMovimentacao tipo);

    void deleteByProdutoId(Long produtoId);
}
