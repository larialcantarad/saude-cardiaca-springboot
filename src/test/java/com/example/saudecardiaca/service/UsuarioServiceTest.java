package com.example.saudecardiaca.service;

import com.example.saudecardiaca.dto.RegistroRequestDTO;
import com.example.saudecardiaca.exception.RegraNegocioException;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegistrarUsuarioComSucesso() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNome("João");
        dto.setEmail("joao@teste.com");
        dto.setSenha("senha123");
        dto.setConfirmarSenha("senha123");

        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        Usuario salvo = usuarioService.registrar(dto);

        assertNotNull(salvo);
        assertEquals(1L, salvo.getId());
        assertEquals("João", salvo.getNome());
        assertEquals("joao@teste.com", salvo.getEmail());
        assertEquals("senhaCriptografada", salvo.getSenha());
        
        verify(usuarioRepository, times(1)).existsByEmail("joao@teste.com");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testRegistrarFalhaEmailJaEmUso() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setEmail("joao@teste.com");
        dto.setSenha("senha123");
        dto.setConfirmarSenha("senha123");

        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(true);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            usuarioService.registrar(dto);
        });

        assertEquals("Este e-mail já está em uso.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testRegistrarFalhaSenhasNaoCoincidem() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setEmail("joao@teste.com");
        dto.setSenha("senha123");
        dto.setConfirmarSenha("senhaDiferente");

        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(false);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            usuarioService.registrar(dto);
        });

        assertEquals("As senhas não coincidem.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
