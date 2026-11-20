package com.nexdom.estoque.application.dto;

import com.nexdom.estoque.domain.model.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentoRequest(
        @NotNull(message = "Produto é obrigatório.")
        Long produtoId,

        @NotNull(message = "Tipo de movimentação é obrigatório.")
        TipoMovimentacao tipoMovimentacao,

        @DecimalMin(value = "0.0", message = "Valor de venda não pode ser negativo.")
        @Digits(integer = 12, fraction = 2, message = "Valor de venda deve ter no máximo 2 casas decimais.")
        BigDecimal valorVenda,

        LocalDateTime dataVenda,

        @NotNull(message = "Quantidade movimentada é obrigatória.")
        @Min(value = 1, message = "Quantidade movimentada deve ser maior que zero.")
        Integer quantidade
) {
}
