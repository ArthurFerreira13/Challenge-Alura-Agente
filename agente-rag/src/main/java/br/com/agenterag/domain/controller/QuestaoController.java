package br.com.agenterag.domain.controller;

import br.com.agenterag.domain.dto.QuestaoResponse;
import br.com.agenterag.domain.dto.VerificarRespostaRequest;
import br.com.agenterag.domain.dto.VerificarRespostaResponse;
import br.com.agenterag.domain.internal.Disciplina;
import br.com.agenterag.domain.service.QuestaoConsultaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoConsultaService questaoConsultaService;

    public QuestaoController(QuestaoConsultaService questaoConsultaService) {
        this.questaoConsultaService = questaoConsultaService;
    }

    @GetMapping
    public Page<QuestaoResponse> listar(
            @RequestParam(required = false) Disciplina disciplina,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return questaoConsultaService.listar(disciplina, pageable)
                .map(QuestaoResponse::from);
    }

    @PostMapping("/{questaoId}/verificar")
    public VerificarRespostaResponse verificar(@PathVariable Long questaoId,
                                               @RequestBody VerificarRespostaRequest request) {
        boolean correta = questaoConsultaService.verificarResposta(questaoId, request.alternativa());
        String alternativaCorreta = questaoConsultaService.buscarAlternativaCorreta(questaoId);
        return new VerificarRespostaResponse(correta, alternativaCorreta);
    }
}