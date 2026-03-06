package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.example.expert.domain.TestAuthResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ManagerControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc(ManagerService managerService, JwtUtil jwtUtil, AuthUser authUser) {

        ManagerController controller = new ManagerController(managerService, jwtUtil);

        return MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new TestAuthResolver(authUser))
                .build();
    }

    @Test
    @DisplayName("POST /todos/{todoId}/managers - 담당자 등록 성공")
    void saveManager_success() throws Exception {

        ManagerService managerService = mock(ManagerService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);

        AuthUser authUser = new AuthUser(1L, "user@test.com", UserRole.USER);

        MockMvc mvc = mockMvc(managerService, jwtUtil, authUser);

        ManagerSaveRequest request = new ManagerSaveRequest(2L);

        ManagerSaveResponse response =
                new ManagerSaveResponse(100L,
                        new org.example.expert.domain.user.dto.response.UserResponse(2L, "manager@test.com"));

        when(managerService.saveManager(eq(authUser), eq(10L), any()))
                .thenReturn(response);

        mvc.perform(post("/todos/10/managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.user.email").value("manager@test.com"));

        verify(managerService).saveManager(eq(authUser), eq(10L), any());
    }

    @Test
    @DisplayName("GET /todos/{todoId}/managers - 담당자 목록 조회 성공")
    void getManagers_success() throws Exception {

        ManagerService managerService = mock(ManagerService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);

        AuthUser authUser = new AuthUser(1L, "user@test.com", UserRole.USER);

        MockMvc mvc = mockMvc(managerService, jwtUtil, authUser);

        List<ManagerResponse> responses = List.of(
                new ManagerResponse(1L,
                        new org.example.expert.domain.user.dto.response.UserResponse(10L, "a@test.com")),
                new ManagerResponse(2L,
                        new org.example.expert.domain.user.dto.response.UserResponse(20L, "b@test.com"))
        );

        when(managerService.getManagers(10L)).thenReturn(responses);

        mvc.perform(get("/todos/10/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].user.email").value("b@test.com"));

        verify(managerService).getManagers(10L);
    }

    @Test
    @DisplayName("DELETE /todos/{todoId}/managers/{managerId} - JWT에서 userId 추출 후 삭제")
    void deleteManager_success() throws Exception {

        ManagerService managerService = mock(ManagerService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);

        AuthUser authUser = new AuthUser(1L, "user@test.com", UserRole.USER);

        MockMvc mvc = mockMvc(managerService, jwtUtil, authUser);

        Claims claims = mock(Claims.class);

        when(jwtUtil.extractClaims(any())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");

        mvc.perform(delete("/todos/10/managers/100")
                        .header("Authorization", "Bearer testtoken"))
                .andExpect(status().isOk());

        verify(managerService).deleteManager(1L, 10L, 100L);
    }
}