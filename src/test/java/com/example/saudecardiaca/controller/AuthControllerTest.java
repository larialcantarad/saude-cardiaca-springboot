package com.example.saudecardiaca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.saudecardiaca.dto.LoginRequestDTO;
import com.example.saudecardiaca.dto.RegistroRequestDTO;
import com.example.saudecardiaca.model.Usuario;
import com.example.saudecardiaca.service.TokenService;
import com.example.saudecardiaca.service.UsuarioService;
import com.example.saudecardiaca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@org.springframework.context.annotation.Import({com.example.saudecardiaca.config.SecurityConfig.class, com.example.saudecardiaca.config.SecurityFilter.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository; // Necessário para o SecurityFilter

    @Test
    public void testRegistrarUsuarioComSucesso() throws Exception {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNome("João");
        dto.setSobrenome("Silva");
        dto.setEmail("joao@example.com");
        dto.setSenha("senha123");
        dto.setConfirmarSenha("senha123");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("João");
        usuarioSalvo.setEmail("joao@example.com");

        when(usuarioService.registrar(any(RegistroRequestDTO.class))).thenReturn(usuarioSalvo);

        mockMvc.perform(post("/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.email").value("joao@example.com"));
    }

    @Test
    public void testLoginComSucesso() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("joao@example.com");
        dto.setSenha("senha123");

        Usuario usuario = new Usuario();
        usuario.setEmail("joao@example.com");

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.gerarToken(any(Usuario.class))).thenReturn("token-jwt-mock");

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("token-jwt-mock"));
    }

    @Test
    public void testRegistrarUsuarioFalhaRegraNegocio() throws Exception {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNome("João");
        dto.setEmail("joao@example.com");
        dto.setSenha("senha123");
        dto.setConfirmarSenha("senha1234");

        when(usuarioService.registrar(any(RegistroRequestDTO.class)))
                .thenThrow(new com.example.saudecardiaca.exception.RegraNegocioException("As senhas não coincidem."));

        mockMvc.perform(post("/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("As senhas não coincidem."));
    }
}
