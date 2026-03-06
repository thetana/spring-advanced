package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    void saveTodo_성공() {

        AuthUser authUser = new AuthUser(1L, "user@test.com", UserRole.USER);
        TodoSaveRequest request = new TodoSaveRequest("title", "contents");

        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo savedTodo = new Todo("title", "contents", "맑음", user);
        ReflectionTestUtils.setField(savedTodo, "id", 100L);

        when(weatherClient.getTodayWeather()).thenReturn("맑음");
        when(todoRepository.save(any())).thenReturn(savedTodo);

        TodoSaveResponse result = todoService.saveTodo(authUser, request);

        assertNotNull(result);
        assertEquals("title", result.getTitle());
        assertEquals("contents", result.getContents());
        assertEquals("맑음", result.getWeather());
        assertEquals("user@test.com", result.getUser().getEmail());

        verify(weatherClient).getTodayWeather();
        verify(todoRepository).save(any());
    }

    @Test
    void getTodos_페이지조회() {

        User user = new User("user@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("title", "contents", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 10L);
        ReflectionTestUtils.setField(todo, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(todo, "modifiedAt", LocalDateTime.now());

        Page<Todo> page = new PageImpl<>(List.of(todo));

        when(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class)))
                .thenReturn(page);

        Page<TodoResponse> result = todoService.getTodos(1, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("title", result.getContent().get(0).getTitle());

        verify(todoRepository).findAllByOrderByModifiedAtDesc(any());
    }

    @Test
    void getTodo_notFound() {

        when(todoRepository.findByIdWithUser(1L))
                .thenReturn(Optional.empty());

        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> todoService.getTodo(1L)
        );

        assertEquals("Todo not found", ex.getMessage());
    }

    @Test
    void getTodo_성공() {

        User user = new User("user@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("title", "contents", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 10L);
        ReflectionTestUtils.setField(todo, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(todo, "modifiedAt", LocalDateTime.now());

        when(todoRepository.findByIdWithUser(10L))
                .thenReturn(Optional.of(todo));

        TodoResponse result = todoService.getTodo(10L);

        assertEquals("title", result.getTitle());
        assertEquals("user@test.com", result.getUser().getEmail());
    }
}