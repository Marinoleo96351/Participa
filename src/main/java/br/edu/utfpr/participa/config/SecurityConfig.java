package br.edu.utfpr.participa.config;

import br.edu.utfpr.participa.comum.ErroDTO;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository,
                                     ObjectMapper objectMapper) throws Exception {
        http
                .securityContext(securityContext -> securityContext.securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf.spa())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/*.html", "/pages/**", "/professor/**", "/css/**", "/js/**", "/fonts/**", "/icons/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/cadastro", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/turmas/entrar").hasRole("ESTUDANTE")
                        .requestMatchers(HttpMethod.POST, "/api/aulas/*/duvidas").hasRole("ESTUDANTE")
                        .requestMatchers(HttpMethod.POST, "/api/duvidas/*/apoio").hasRole("ESTUDANTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/duvidas/*/apoio").hasRole("ESTUDANTE")
                        .requestMatchers("/api/professor/**").hasRole("PROFESSOR")
                        .requestMatchers("/api/moderacao/**").hasAnyRole("PROFESSOR", "MONITOR")
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sair")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) ->
                                escreverErro(response, objectMapper, 401, "Não autenticado."))
                        .accessDeniedHandler((request, response, ex) ->
                                escreverErro(response, objectMapper, 403, "Acesso negado.")));
        return http.build();
    }

    private void escreverErro(jakarta.servlet.http.HttpServletResponse response, ObjectMapper objectMapper,
                               int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErroDTO(mensagem));
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
