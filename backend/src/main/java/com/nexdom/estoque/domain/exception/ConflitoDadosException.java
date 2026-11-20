package com.nexdom.estoque.domain.exception;

public class ConflitoDadosException extends EstoqueException {

    public ConflitoDadosException(String mensagem) {
        super("CONFLITO", mensagem);
    }
}
