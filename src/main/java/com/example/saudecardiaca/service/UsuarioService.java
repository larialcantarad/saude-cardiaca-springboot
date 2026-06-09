package com.example.saudecardiaca.service;

import com.example.saudecardiaca.dto.RegistroRequestDTO;
import com.example.saudecardiaca.exception.RegraNegocioException;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder; // <-- Injeta o criptografador do Spring

    public Usuario registrar(RegistroRequestDTO dto) {
        // 1. Validar se o e-mail já existe
        if (repository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("Este e-mail já está em uso.");
        }

        // 2. Validar confirmação de senha
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new RegraNegocioException("As senhas não coincidem.");
        }

        // 3. Montar e salvar o usuário
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setSobrenome(dto.getSobrenome());
        usuario.setEmail(dto.getEmail());
        usuario.setNumeroTelefone(dto.getNumeroTelefone());

        // Criptografa a senha antes de salvar no banco!
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setSexo(dto.getSexo());
        usuario.setPaisResidencia(dto.getPaisResidencia());

        return repository.save(usuario);
    }
}