package com.example.saudecardiaca.controller;

import com.example.saudecardiaca.dto.LoginRequestDTO;
import com.example.saudecardiaca.dto.RegistroRequestDTO;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.service.TokenService;
import com.example.saudecardiaca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private AuthenticationManager manager; // <-- Delega o login pro Spring Security

    @Autowired
    private TokenService tokenService; // <-- Gera o Token JWT

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrar(@RequestBody RegistroRequestDTO dto) {
        Usuario novoUsuario = service.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO dto) {
        // Passa as credenciais pro Spring Security validar
        var token = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha());
        var authentication = manager.authenticate(token);

        // Se a senha estiver correta, gera e devolve o Token JWT
        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(tokenJWT);
    }
}