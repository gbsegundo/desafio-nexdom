package com.nexdom.estoque.domain.exception;

public class RecursoNaoEncontradoException extends EstoqueException {

    public RecursoNaoEncontradoException(String mensagem) {
        super("NAO_ENCONTRADO", mensagem);
    }
}
