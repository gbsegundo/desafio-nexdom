package com.nexdom.estoque.application.dto;

import com.nexdom.estoque.domain.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentoResponse(
        Long id,
        Long produtoId,
        String produtoCodigo,
        String produtoDescricao,
        TipoMovimentacao tipoMovimentacao,
        BigDecimal valorVenda,
        LocalDateTime dataVenda,
        Integer quantidade
) {
}
