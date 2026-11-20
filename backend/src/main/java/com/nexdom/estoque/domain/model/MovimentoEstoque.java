package com.nexdom.estoque.domain.model;

import com.nexdom.estoque.domain.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "movimento_estoque",
        indexes = {
                @Index(name = "idx_movimento_produto", columnList = "produto_id"),
                @Index(name = "idx_movimento_tipo", columnList = "tipo_movimentacao")
        }
)
public class MovimentoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false, length = 10)
    private TipoMovimentacao tipoMovimentacao;

    @Column(name = "valor_venda", precision = 14, scale = 2)
    private BigDecimal valorVenda;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @Column(nullable = false)
    private Integer quantidade;

    protected MovimentoEstoque() {
    }

    public static MovimentoEstoque registrar(
            Produto produto,
            TipoMovimentacao tipoMovimentacao,
            BigDecimal valorVenda,
            LocalDateTime dataVenda,
            int quantidade
    ) {
        if (produto == null) {
            throw new RegraNegocioException("Produto é obrigatório.");
        }
        if (tipoMovimentacao == null) {
            throw new RegraNegocioException("Tipo de movimentação é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade movimentada deve ser maior que zero.");
        }

        BigDecimal valorNormalizado = normalizarValorVenda(tipoMovimentacao, valorVenda);

        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.produto = produto;
        movimento.tipoMovimentacao = tipoMovimentacao;
        movimento.valorVenda = valorNormalizado;
        movimento.dataVenda = dataVenda == null ? LocalDateTime.now() : dataVenda;
        movimento.quantidade = quantidade;
        return movimento;
    }

    private static BigDecimal normalizarValorVenda(TipoMovimentacao tipo, BigDecimal valorVenda) {
        if (tipo == TipoMovimentacao.ENTRADA) {
            return null;
        }
        if (valorVenda == null) {
            throw new RegraNegocioException("Valor de venda é obrigatório para saída de estoque.");
        }
        if (valorVenda.signum() < 0) {
            throw new RegraNegocioException("Valor de venda não pode ser negativo.");
        }
        return valorVenda.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public Integer getQuantidade() {
        return quantidade;
    }
}
