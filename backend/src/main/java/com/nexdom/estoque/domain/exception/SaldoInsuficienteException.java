package com.nexdom.estoque.domain.exception;

public class SaldoInsuficienteException extends EstoqueException {

    public SaldoInsuficienteException(String codigoProduto, int disponivel, int solicitado) {
        super(
                "SALDO_INSUFICIENTE",
                "Saldo insuficiente para o produto %s. Quantidade disponível: %d. Quantidade solicitada: %d."
                        .formatted(codigoProduto, disponivel, solicitado)
        );
    }
}
