package br.com.agenterag.domain.service;

import br.com.agenterag.domain.exception.SimuladoNaoEncontradoException;
import br.com.agenterag.domain.exception.UsuarioNaoEncontradoException;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.internal.SimuladoSessao;
import br.com.agenterag.domain.internal.SimuladoSessaoRepository;
import br.com.agenterag.domain.internal.StatusSessao;
import br.com.agenterag.domain.internal.Usuario;
import br.com.agenterag.domain.internal.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class SimuladoSessaoService {

    private final SimuladoSessaoRepository sessaoRepository;
    private final SimuladoRepository simuladoRepository;
    private final UsuarioRepository usuarioRepository; // NOVO
    private final Clock clock;

    public SimuladoSessaoService(SimuladoSessaoRepository sessaoRepository,
                                 SimuladoRepository simuladoRepository,
                                 UsuarioRepository usuarioRepository, // NOVO
                                 Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.simuladoRepository = simuladoRepository;
        this.usuarioRepository = usuarioRepository; // NOVO
        this.clock = clock;
    }

    @Transactional
    public SimuladoSessao iniciar(Long usuarioId, Long simuladoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
        Simulado simulado = simuladoRepository.findById(simuladoId)
                .orElseThrow(() -> new SimuladoNaoEncontradoException(simuladoId));

        SimuladoSessao sessao = new SimuladoSessao();
        sessao.setUsuario(usuario);
        sessao.setSimulado(simulado);
        sessao.setIniciadoEm(LocalDateTime.now(clock));
        sessao.setStatus(StatusSessao.EM_ANDAMENTO);

        return sessaoRepository.save(sessao);
    }
}