package com.nexdom.estoque.config;

import com.nexdom.estoque.domain.service.CalculadoraLucro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    CalculadoraLucro calculadoraLucro() {
        return new CalculadoraLucro();
    }
}
