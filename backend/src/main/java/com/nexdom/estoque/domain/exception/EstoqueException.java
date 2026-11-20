package com.nexdom.estoque.domain.exception;

public abstract class EstoqueException extends RuntimeException {

    private final String codigo;

    protected EstoqueException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
