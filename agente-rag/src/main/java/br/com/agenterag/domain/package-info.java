/**
 * Módulo de domínio: entidades e regras de negócio do simulado OAB
 * (Simulado, Questao, SimuladoSessao, RespostaUsuario, ResultadoSimulado).
 *
 * API pública: domain.service (orquestração de casos de uso),
 * domain.controller (endpoints HTTP), domain.dto, domain.exception.
 * domain.internal é oculto por convenção de nome do Spring Modulith.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"core"}
)
package br.com.agenterag.domain;