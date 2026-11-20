package com.nexdom.estoque.infrastructure.persistence;

import com.nexdom.estoque.domain.model.Produto;
import com.nexdom.estoque.domain.model.TipoProduto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    List<Produto> findAllByOrderByCodigoAsc();

    List<Produto> findByTipoProdutoOrderByCodigoAsc(TipoProduto tipoProduto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> findByIdComBloqueio(@Param("id") Long id);
}
