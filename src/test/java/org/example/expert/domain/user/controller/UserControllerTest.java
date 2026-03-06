package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.TestAuthResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc(UserService userService) {

        UserController controller = new UserController(userService);

        ObjectMapper objectMapper = new ObjectMapper();

        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        return MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(converter)
                .setCustomArgumentResolvers(
                        new TestAuthResolver(
                                new AuthUser(1L, "user@test.com", UserRole.USER)
                        )
                )
                .build();
    }

    @Test
    @DisplayName("GET /users/{userId} - 사용자 조회")
    void getUser_success() throws Exception {

        UserService userService = mock(UserService.class);
        MockMvc mvc = mockMvc(userService);

        UserResponse response = new UserResponse(1L, "user@test.com");

        when(userService.getUser(1L)).thenReturn(response);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));

        verify(userService).getUser(1L);
    }

    @Test
    @DisplayName("PUT /users - 비밀번호 변경")
    void changePassword_success() throws Exception {

        UserService userService = mock(UserService.class);
        MockMvc mvc = mockMvc(userService);

        ObjectMapper mapper = new ObjectMapper();

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword123!", "newPassword123!");

        mvc.perform(put("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).changePassword(eq(1L), any(UserChangePasswordRequest.class));
    }
}