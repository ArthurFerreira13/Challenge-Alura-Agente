package br.com.agenterag.domain.dto;


public record SalvarAnotacaoRequest(
        Long usuarioId,
        Long questaoId,
        String texto
) {}