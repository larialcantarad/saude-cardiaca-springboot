package com.example.saudecardiaca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.saudecardiaca.model.Acompanhamento;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.service.AcompanhamentoService;
import com.example.saudecardiaca.service.TokenService;
import com.example.saudecardiaca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AcompanhamentoController.class)
@org.springframework.context.annotation.Import({com.example.saudecardiaca.config.SecurityConfig.class, com.example.saudecardiaca.config.SecurityFilter.class})
public class AcompanhamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AcompanhamentoService acompanhamentoService;

    @MockitoBean
    private TokenService tokenService; // Requerido pelo SecurityFilter

    @MockitoBean
    private UsuarioRepository usuarioRepository; // Requerido pelo SecurityFilter

    @Test
    public void testCadastrarComSucesso() throws Exception {
        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setId(10L);
        usuarioLogado.setEmail("usuario@example.com");

        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(80);
        acompanhamento.setNivelOxigenacao(98);
        acompanhamento.setPressaoArterial("12/8");

        Acompanhamento acompanhamentoSalvo = new Acompanhamento();
        acompanhamentoSalvo.setId(1L);
        acompanhamentoSalvo.setFrequenciaCardiaca(80);
        acompanhamentoSalvo.setNivelOxigenacao(98);
        acompanhamentoSalvo.setPressaoArterial("12/8");
        acompanhamentoSalvo.setUsuario(usuarioLogado);

        when(acompanhamentoService.cadastrar(any(Acompanhamento.class), any(Usuario.class))).thenReturn(acompanhamentoSalvo);

        mockMvc.perform(post("/acompanhamentos")
                        .with(csrf())
                        .with(user(usuarioLogado))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acompanhamento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.frequenciaCardiaca").value(80))
                .andExpect(jsonPath("$.nivelOxigenacao").value(98))
                .andExpect(jsonPath("$.pressaoArterial").value("12/8"));
    }

    @Test
    public void testListarComSucesso() throws Exception {
        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setId(10L);
        usuarioLogado.setEmail("usuario@example.com");

        Acompanhamento a1 = new Acompanhamento();
        a1.setId(1L);
        a1.setFrequenciaCardiaca(75);

        Acompanhamento a2 = new Acompanhamento();
        a2.setId(2L);
        a2.setFrequenciaCardiaca(82);

        when(acompanhamentoService.listarPorUsuario(any(Long.class))).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/acompanhamentos")
                        .with(user(usuarioLogado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].frequenciaCardiaca").value(75))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].frequenciaCardiaca").value(82));
    }

    @Test
    public void testAcessoNegadoSemAutenticacao() throws Exception {
        Acompanhamento acompanhamento = new Acompanhamento();

        mockMvc.perform(post("/acompanhamentos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acompanhamento)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCadastrarFalhaRegraNegocio() throws Exception {
        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setId(10L);
        usuarioLogado.setEmail("usuario@example.com");

        Acompanhamento acompanhamento = new Acompanhamento();
        acompanhamento.setFrequenciaCardiaca(-5);

        when(acompanhamentoService.cadastrar(any(Acompanhamento.class), any(Usuario.class)))
                .thenThrow(new com.example.saudecardiaca.exception.RegraNegocioException("Frequência cardíaca inválida"));

        mockMvc.perform(post("/acompanhamentos")
                        .with(csrf())
                        .with(user(usuarioLogado))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acompanhamento)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Frequência cardíaca inválida"));
    }
}
