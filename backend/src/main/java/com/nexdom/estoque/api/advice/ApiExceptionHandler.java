package com.nexdom.estoque.api.advice;

import com.nexdom.estoque.api.dto.ErroResponse;
import com.nexdom.estoque.domain.exception.ConflitoDadosException;
import com.nexdom.estoque.domain.exception.RecursoNaoEncontradoException;
import com.nexdom.estoque.domain.exception.RegraNegocioException;
import com.nexdom.estoque.domain.exception.SaldoInsuficienteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErroResponse> saldoInsuficiente(SaldoInsuficienteException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), ex.getCodigo());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), ex.getCodigo());
    }

    @ExceptionHandler(ConflitoDadosException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoDadosException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), ex.getCodigo());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getCodigo());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponse("Dados inválidos.", "DADOS_INVALIDOS", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> requisicaoIlegivel(HttpMessageNotReadableException ex) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida. Verifique os campos e os valores informados.",
                "REQUISICAO_INVALIDA"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno.", "ERRO_INTERNO");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, String codigo) {
        return ResponseEntity.status(status).body(new ErroResponse(mensagem, codigo));
    }
}
