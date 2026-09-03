package br.com.agenterag.domain.controller;

import br.com.agenterag.domain.dto.AtualizarNomeRequest;
import br.com.agenterag.domain.dto.CriarUsuarioRequest;
import br.com.agenterag.domain.dto.UsuarioResponse;
import br.com.agenterag.domain.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizarNome(@PathVariable Long id,
                                                         @RequestBody AtualizarNomeRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarNome(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}