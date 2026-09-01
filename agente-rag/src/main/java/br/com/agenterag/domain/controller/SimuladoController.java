package br.com.agenterag.domain.controller;

import br.com.agenterag.domain.dto.IniciarSessaoRequest;
import br.com.agenterag.domain.dto.QuestaoResponse;
import br.com.agenterag.domain.dto.ResponderRequest;
import br.com.agenterag.domain.dto.ResultadoSimuladoResponse;
import br.com.agenterag.domain.dto.SessaoResponse;
import br.com.agenterag.domain.dto.SimuladoResumoResponse;
import br.com.agenterag.domain.exception.ErroResponse;
import br.com.agenterag.domain.exception.SessaoNaoEncontradaException;
import br.com.agenterag.domain.exception.SimuladoNaoEncontradoException;
import br.com.agenterag.domain.internal.ResultadoSimulado;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.internal.SimuladoSessao;
import br.com.agenterag.domain.internal.SimuladoSessaoRepository;
import br.com.agenterag.domain.service.ApuracaoResultadoService;
import br.com.agenterag.domain.service.RespostaSimuladoService;
import br.com.agenterag.domain.service.SimuladoSessaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/simulados")
public class SimuladoController {

    private final SimuladoSessaoService simuladoSessaoService;
    private final RespostaSimuladoService respostaSimuladoService;
    private final ApuracaoResultadoService apuracaoResultadoService;
    // Acesso direto aos repositórios aqui é provisório — pra fazer funcionar
    // rápido. Quando refatorarmos, isso deveria virar um service próprio
    // de consulta (ex.: SimuladoConsultaService) em vez do controller ler
    // repository diretamente.
    private final SimuladoRepository simuladoRepository;
    private final SimuladoSessaoRepository simuladoSessaoRepository;
    private final Clock clock;

    public SimuladoController(SimuladoSessaoService simuladoSessaoService,
                              RespostaSimuladoService respostaSimuladoService,
                              ApuracaoResultadoService apuracaoResultadoService,
                              SimuladoRepository simuladoRepository,
                              SimuladoSessaoRepository simuladoSessaoRepository,
                              Clock clock) {
        this.simuladoSessaoService = simuladoSessaoService;
        this.respostaSimuladoService = respostaSimuladoService;
        this.apuracaoResultadoService = apuracaoResultadoService;
        this.simuladoRepository = simuladoRepository;
        this.simuladoSessaoRepository = simuladoSessaoRepository;
        this.clock = clock;
    }

    /** Lista os simulados disponíveis pra iniciar. */
    @GetMapping
    public ResponseEntity<List<SimuladoResumoResponse>> listar() {
        List<SimuladoResumoResponse> simulados = simuladoRepository.findAll().stream()
                .map(SimuladoResumoResponse::from)
                .toList();
        return ResponseEntity.ok(simulados);
    }

    /**
     * Questões do simulado, sem a alternativa correta — é o que o front usa
     * pra montar a tela do simulado. O gabarito só aparece depois do /apurar.
     */
    @GetMapping("/{simuladoId}/questoes")
    public ResponseEntity<List<QuestaoResponse>> listarQuestoes(@PathVariable Long simuladoId) {
        Simulado simulado = simuladoRepository.findById(simuladoId)
                .orElseThrow(() -> new SimuladoNaoEncontradoException(simuladoId));
        List<QuestaoResponse> questoes = simulado.getQuestoes().stream()
                .map(QuestaoResponse::from)
                .toList();
        return ResponseEntity.ok(questoes);
    }

    /** Status atual de uma sessão (pra retomar um simulado em andamento). */
    @GetMapping("/sessoes/{sessaoId}")
    public ResponseEntity<SessaoResponse> buscarSessao(@PathVariable Long sessaoId) {
        SimuladoSessao sessao = simuladoSessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new SessaoNaoEncontradaException(sessaoId));
        return ResponseEntity.ok(SessaoResponse.from(sessao, clock));
    }

    /** Inicia uma nova sessão de simulado para o usuário informado. */
    @PostMapping("/{simuladoId}/sessoes")
    public ResponseEntity<SessaoResponse> iniciar(@PathVariable Long simuladoId,
                                                  @Valid @RequestBody IniciarSessaoRequest request) {
        SimuladoSessao sessao = simuladoSessaoService.iniciar(request.usuarioId(), simuladoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SessaoResponse.from(sessao, clock));
    }

    /** Registra (ou substitui) a resposta do usuário para uma questão da sessão. */
    @PostMapping("/sessoes/{sessaoId}/respostas")
    public ResponseEntity<Void> responder(@PathVariable Long sessaoId,
                                          @Valid @RequestBody ResponderRequest request) {
        respostaSimuladoService.responder(sessaoId, request.questaoId(), request.alternativa());
        return ResponseEntity.noContent().build();
    }

    /** Fecha a sessão, apura o resultado (geral e por disciplina) e devolve o resumo. */
    @PostMapping("/sessoes/{sessaoId}/apurar")
    public ResponseEntity<ResultadoSimuladoResponse> apurar(@PathVariable Long sessaoId) {
        ResultadoSimulado resultado = apuracaoResultadoService.apurar(sessaoId);
        return ResponseEntity.ok(ResultadoSimuladoResponse.from(resultado));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> handleSessaoNaoEncontrada(IllegalArgumentException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> handleSessaoJaConcluida(IllegalStateException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleCorpoRequisicaoAusenteOuInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição é obrigatório e deve ser um JSON válido.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacaoAtributos(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dados de requisição inválidos.");
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), mensagem, LocalDateTime.now(clock)));
    }
}