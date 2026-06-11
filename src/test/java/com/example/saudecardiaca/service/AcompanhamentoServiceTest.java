package com.example.saudecardiaca.service;

import com.example.saudecardiaca.exception.RegraNegocioException;
import com.example.saudecardiaca.model.Acompanhamento;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.repository.AcompanhamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AcompanhamentoServiceTest {

    @InjectMocks
    private AcompanhamentoService acompanhamentoService;

    @Mock
    private AcompanhamentoRepository acompanhamentoRepository;

    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario();
        usuarioLogado.setId(1L);
        usuarioLogado.setEmail("teste@teste.com");
    }

    @Test
    void testCadastrarComSucesso() {
        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(80);
        acompanhamento.setNivelOxigenacao(98);
        acompanhamento.setPressaoArterial("120/80");

        when(acompanhamentoRepository.save(any(Acompanhamento.class))).thenAnswer(invocation -> {
            Acompanhamento a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        Acompanhamento salvo = acompanhamentoService.cadastrar(acompanhamento, usuarioLogado);

        assertNotNull(salvo);
        assertEquals(10L, salvo.getId());
        assertEquals(usuarioLogado, salvo.getUsuario());
        verify(acompanhamentoRepository, times(1)).save(acompanhamento);
    }

    @Test
    void testCadastrarFrequenciaCardiacaInvalida() {
        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(-10);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            acompanhamentoService.cadastrar(acompanhamento, usuarioLogado);
        });

        assertEquals("Frequência cardíaca inválida", exception.getMessage());
        verify(acompanhamentoRepository, never()).save(any());
    }

    @Test
    void testCadastrarOxigenacaoInvalidaMenorQue95() {
        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(80);
        acompanhamento.setNivelOxigenacao(90);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            acompanhamentoService.cadastrar(acompanhamento, usuarioLogado);
        });

        assertEquals("Oxigenação inválida", exception.getMessage());
        verify(acompanhamentoRepository, never()).save(any());
    }

    @Test
    void testCadastrarOxigenacaoInvalidaMaiorQue100() {
        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(80);
        acompanhamento.setNivelOxigenacao(105);

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            acompanhamentoService.cadastrar(acompanhamento, usuarioLogado);
        });

        assertEquals("Oxigenação inválida", exception.getMessage());
        verify(acompanhamentoRepository, never()).save(any());
    }

    @Test
    void testCadastrarPressaoArterialInvalida() {
        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(80);
        acompanhamento.setNivelOxigenacao(98);
        acompanhamento.setPressaoArterial("12080"); // Formato inválido, sem a barra

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            acompanhamentoService.cadastrar(acompanhamento, usuarioLogado);
        });

        assertEquals("Pressão arterial inválida", exception.getMessage());
        verify(acompanhamentoRepository, never()).save(any());
    }

    @Test
    void testListarPorUsuario() {
        Acompanhamento a1 = new Acompanhamento();
        a1.setId(1L);
        Acompanhamento a2 = new Acompanhamento();
        a2.setId(2L);

        when(acompanhamentoRepository.findByUsuarioId(1L)).thenReturn(List.of(a1, a2));

        List<Acompanhamento> lista = acompanhamentoService.listarPorUsuario(1L);

        assertEquals(2, lista.size());
        verify(acompanhamentoRepository, times(1)).findByUsuarioId(1L);
    }
}
