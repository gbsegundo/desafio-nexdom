package com.nexdom.estoque.api.dto;

import java.util.Map;

public record ErroResponse(String mensagem, String codigo, Map<String, String> campos) {

    public ErroResponse(String mensagem, String codigo) {
        this(mensagem, codigo, Map.of());
    }
}
