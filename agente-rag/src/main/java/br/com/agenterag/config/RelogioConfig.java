package br.com.agenterag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RelogioConfig {

    /**
     * Clock injetável em vez de LocalDateTime.now() direto — permite fixar o
     * tempo em testes (Clock.fixed(...)) e evita "spooky action" se o
     * servidor mudar de timezone.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}