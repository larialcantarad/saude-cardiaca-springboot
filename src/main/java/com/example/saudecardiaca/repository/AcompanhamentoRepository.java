package com.example.saudecardiaca.repository;

import com.example.saudecardiaca.model.Acompanhamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AcompanhamentoRepository extends JpaRepository<Acompanhamento, Long> {
    // Busca o histórico filtrando estritamente pelo ID do usuário logado
    List<Acompanhamento> findByUsuarioId(Long usuarioId);
}