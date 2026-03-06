package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc(AuthService authService) {

        AuthController controller = new AuthController(authService);

        ObjectMapper objectMapper = new ObjectMapper();

        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        return MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    @DisplayName("POST /auth/signup - 회원가입")
    void signup_success() throws Exception {

        AuthService authService = mock(AuthService.class);
        MockMvc mvc = mockMvc(authService);

        ObjectMapper mapper = new ObjectMapper();

        SignupRequest request =
                new SignupRequest("user@test.com", "Password123", "USER");

        SignupResponse response =
                new SignupResponse("Bearer token");

        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("Bearer token"));

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    @DisplayName("POST /auth/signin - 로그인")
    void signin_success() throws Exception {

        AuthService authService = mock(AuthService.class);
        MockMvc mvc = mockMvc(authService);

        ObjectMapper mapper = new ObjectMapper();

        SigninRequest request =
                new SigninRequest("user@test.com", "Password123");

        SigninResponse response =
                new SigninResponse("Bearer token");

        when(authService.signin(any(SigninRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/auth/signin")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("Bearer token"));

        verify(authService).signin(any(SigninRequest.class));
    }
}