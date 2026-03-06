package org.example.expert.domain.todo.entity;

import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TodoTest {

    @Test
    @DisplayName("Todo 생성 시 managers 리스트에 작성자가 자동 등록된다")
    void createTodo_success() {

        User user = new User("test@test.com", "password", UserRole.USER);

        Todo todo = new Todo(
                "title",
                "contents",
                "sunny",
                user
        );

        assertEquals("title", todo.getTitle());
        assertEquals("contents", todo.getContents());
        assertEquals("sunny", todo.getWeather());
        assertEquals(user, todo.getUser());

        assertEquals(1, todo.getManagers().size());

        Manager manager = todo.getManagers().get(0);

        assertEquals(user, manager.getUser());
        assertEquals(todo, manager.getTodo());
    }

    @Test
    @DisplayName("Todo update 정상 동작")
    void update_success() {

        User user = new User("test@test.com", "password", UserRole.USER);

        Todo todo = new Todo(
                "title",
                "contents",
                "sunny",
                user
        );

        todo.update("new title", "new contents");

        assertEquals("new title", todo.getTitle());
        assertEquals("new contents", todo.getContents());
    }
}