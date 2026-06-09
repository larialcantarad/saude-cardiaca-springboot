package com.example.saudecardiaca.controller;

import com.example.saudecardiaca.model.Acompanhamento;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.service.AcompanhamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acompanhamentos")
public class AcompanhamentoController {

    @Autowired
    private AcompanhamentoService service;

    @PostMapping
    public ResponseEntity<Acompanhamento> cadastrar(
            @RequestBody Acompanhamento acompanhamento,
            @AuthenticationPrincipal Usuario usuarioLogado) { // <-- Captura o usuário logado pelo Token JWT

        // Passa o acompanhamento e o usuário autenticado para o service
        Acompanhamento novoAcompanhamento =
                service.cadastrar(acompanhamento, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(novoAcompanhamento);
    }

    @GetMapping
    public ResponseEntity<List<Acompanhamento>> listar(
            @AuthenticationPrincipal Usuario usuarioLogado) { // <-- Captura o usuário logado pelo Token JWT

        // Filtra a listagem trazendo estritamente os dados do usuário autenticado
        return ResponseEntity.ok(service.listarPorUsuario(usuarioLogado.getId()));
    }
}