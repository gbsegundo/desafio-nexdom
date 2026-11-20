package com.nexdom.estoque.domain.exception;

public class RegraNegocioException extends EstoqueException {

    public RegraNegocioException(String mensagem) {
        super("REGRA_NEGOCIO", mensagem);
    }
}
