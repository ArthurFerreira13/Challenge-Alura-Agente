package br.com.agenterag.domain.controller;


import br.com.agenterag.domain.dto.AnotacaoResponse;
import br.com.agenterag.domain.dto.SalvarAnotacaoRequest;
import br.com.agenterag.domain.service.AnotacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anotacoes")
public class AnotacaoController {

    private final AnotacaoService service;

    public AnotacaoController(AnotacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AnotacaoResponse> salvarOuAtualizar(@RequestBody SalvarAnotacaoRequest request) {
        AnotacaoResponse response = service.salvarOuAtualizar(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AnotacaoResponse> buscarPorUsuarioEQuestao(
            @RequestParam Long usuarioId,
            @RequestParam Long questaoId) {

        return service.buscarPorUsuarioEQuestao(usuarioId, questaoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
