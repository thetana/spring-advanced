package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.expert.domain.TestAuthResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private UserService userService; // ← 이것이 핵심

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc(TodoService todoService) {

        TodoController controller = new TodoController(todoService);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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
    @DisplayName("POST /todos - 일정 생성")
    void saveTodo_success() throws Exception {

        TodoService todoService = mock(TodoService.class);

        MockMvc mvc = mockMvc(todoService);

        TodoSaveRequest request = new TodoSaveRequest("title", "contents");

        TodoSaveResponse response =
                new TodoSaveResponse(
                        1L,
                        "title",
                        "contents",
                        "맑음",
                        new org.example.expert.domain.user.dto.response.UserResponse(1L, "user@test.com")
                );

        when(todoService.saveTodo(any(), any())).thenReturn(response);

        mvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"));

        verify(todoService).saveTodo(any(), any());
    }

    @Test
    @DisplayName("GET /todos - 페이지 조회")
    void getTodos_success() throws Exception {

        TodoResponse todo =
                new TodoResponse(
                        1L,
                        "title",
                        "contents",
                        "맑음",
                        new UserResponse(1L, "user@test.com"),
                        null,
                        null
                );

        Page<TodoResponse> page = new PageImpl<>(List.of(todo));

        when(todoService.getTodos(1, 10)).thenReturn(page);

        mockMvc.perform(get("/todos?page=1&size=10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /todos/{todoId} - 단건 조회")
    void getTodo_success() throws Exception {

        TodoService todoService = mock(TodoService.class);

        MockMvc mvc = mockMvc(todoService);

        TodoResponse response =
                new TodoResponse(
                        1L,
                        "title",
                        "contents",
                        "맑음",
                        new UserResponse(1L, "user@test.com"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        when(todoService.getTodo(1L)).thenReturn(response);

        mvc.perform(get("/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"));

        verify(todoService).getTodo(1L);
    }
}