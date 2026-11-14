package br.edu.utfpr.participa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração provisória (branch chore/configuracao-inicial). A versão definitiva,
 * com login por JSON, CSRF para SPA e as regras por caminho, entra na
 * feature/base-acesso-api (seção 4.4 do plano de desenvolvimento).
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/fonts/**", "/icons/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
