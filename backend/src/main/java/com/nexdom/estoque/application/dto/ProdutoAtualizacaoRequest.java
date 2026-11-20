package com.nexdom.estoque.application.dto;

import com.nexdom.estoque.domain.model.TipoProduto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoAtualizacaoRequest(
        @NotBlank(message = "Código é obrigatório.")
        @Size(max = 30, message = "Código deve ter no máximo 30 caracteres.")
        String codigo,

        @NotBlank(message = "Descrição é obrigatória.")
        @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres.")
        String descricao,

        @NotNull(message = "Tipo de produto é obrigatório.")
        TipoProduto tipoProduto,

        @NotNull(message = "Valor no fornecedor é obrigatório.")
        @DecimalMin(value = "0.0", message = "Valor no fornecedor não pode ser negativo.")
        @Digits(integer = 12, fraction = 2, message = "Valor no fornecedor deve ter no máximo 2 casas decimais.")
        BigDecimal valorFornecedor
) {
}
