package br.com.agenterag.domain.service;

import br.com.agenterag.domain.dto.CriarUsuarioRequest;
import br.com.agenterag.domain.dto.UsuarioResponse;
import br.com.agenterag.domain.exception.EmailJaCadastradoException;
import br.com.agenterag.domain.exception.UsuarioNaoEncontradoException;
import br.com.agenterag.domain.internal.Usuario;
import br.com.agenterag.domain.internal.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
}