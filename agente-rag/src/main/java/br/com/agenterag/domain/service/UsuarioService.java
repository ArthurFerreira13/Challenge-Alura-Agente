package br.com.agenterag.domain.service;

import br.com.agenterag.domain.dto.AtualizarNomeRequest;
import br.com.agenterag.domain.dto.CriarUsuarioRequest;
import br.com.agenterag.domain.dto.UsuarioResponse;
import br.com.agenterag.domain.exception.EmailJaCadastradoException;
import br.com.agenterag.domain.exception.UsuarioNaoEncontradoException;
import br.com.agenterag.domain.internal.AnotacaoQuestaoRepository;
import br.com.agenterag.domain.internal.ResultadoSimuladoRepository;
import br.com.agenterag.domain.internal.SimuladoSessaoRepository;
import br.com.agenterag.domain.internal.Usuario;
import br.com.agenterag.domain.internal.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SimuladoSessaoRepository sessaoRepository;
    private final AnotacaoQuestaoRepository anotacaoRepository;
    private final ResultadoSimuladoRepository resultadoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          SimuladoSessaoRepository sessaoRepository,
                          AnotacaoQuestaoRepository anotacaoRepository,
                          ResultadoSimuladoRepository resultadoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.anotacaoRepository = anotacaoRepository;
        this.resultadoRepository = resultadoRepository;
    }

    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailJaCadastradoException(request.email());
        }
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return UsuarioResponse.from(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional
    public UsuarioResponse atualizarNome(Long id, AtualizarNomeRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        usuario.setNome(request.nome());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        resultadoRepository.deleteAllBySessaoUsuario(usuario);
        sessaoRepository.deleteAllByUsuario(usuario);
        anotacaoRepository.deleteAllByUsuarioId(usuario.getId());
        usuarioRepository.delete(usuario);
    }
}