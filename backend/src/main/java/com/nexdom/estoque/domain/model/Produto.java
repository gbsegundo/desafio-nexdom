package com.nexdom.estoque.domain.model;

import com.nexdom.estoque.domain.exception.RegraNegocioException;
import com.nexdom.estoque.domain.exception.SaldoInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false, length = 30)
    private TipoProduto tipoProduto;

    @Column(name = "valor_fornecedor", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorFornecedor;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    protected Produto() {
    }

    public static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraNegocioException("Código é obrigatório.");
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    public static Produto novo(
            String codigo,
            String descricao,
            TipoProduto tipoProduto,
            BigDecimal valorFornecedor,
            int quantidadeEstoque
    ) {
        Produto produto = new Produto();
        produto.codigo = normalizarCodigo(codigo);
        produto.descricao = normalizarDescricao(descricao);
        produto.tipoProduto = exigirTipo(tipoProduto);
        produto.valorFornecedor = normalizarValor(valorFornecedor);
        produto.quantidadeEstoque = exigirQuantidadeNaoNegativa(quantidadeEstoque);
        return produto;
    }

    public void atualizar(String codigo, String descricao, TipoProduto tipoProduto, BigDecimal valorFornecedor) {
        this.codigo = normalizarCodigo(codigo);
        this.descricao = normalizarDescricao(descricao);
        this.tipoProduto = exigirTipo(tipoProduto);
        this.valorFornecedor = normalizarValor(valorFornecedor);
    }

    public void adicionarEstoque(int quantidade) {
        int quantidadeValida = exigirQuantidadePositiva(quantidade);
        this.quantidadeEstoque = this.quantidadeEstoque + quantidadeValida;
    }

    public void retirarEstoque(int quantidade) {
        int quantidadeValida = exigirQuantidadePositiva(quantidade);
        if (quantidadeValida > this.quantidadeEstoque) {
            throw new SaldoInsuficienteException(this.codigo, this.quantidadeEstoque, quantidadeValida);
        }
        this.quantidadeEstoque = this.quantidadeEstoque - quantidadeValida;
    }

    private static String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descrição é obrigatória.");
        }
        return descricao.trim();
    }

    private static TipoProduto exigirTipo(TipoProduto tipoProduto) {
        if (tipoProduto == null) {
            throw new RegraNegocioException("Tipo de produto é obrigatório.");
        }
        return tipoProduto;
    }

    private static BigDecimal normalizarValor(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("O valor no fornecedor não pode ser negativo.");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static int exigirQuantidadeNaoNegativa(int quantidade) {
        if (quantidade < 0) {
            throw new RegraNegocioException("A quantidade em estoque não pode ser negativa.");
        }
        return quantidade;
    }

    private static int exigirQuantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade movimentada deve ser maior que zero.");
        }
        return quantidade;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoProduto getTipoProduto() {
        return tipoProduto;
    }

    public BigDecimal getValorFornecedor() {
        return valorFornecedor;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
}
