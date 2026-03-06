package org.example.expert.domain.comment.entity;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    @DisplayName("Comment 생성자 호출 시 contents/user/todo가 정상 세팅된다")
    void constructor_필드세팅() {
        // given
        User user = new User("u@test.com", "pw", UserRole.USER);
        Todo todo = new Todo("title", "title", "contents", user);

        // when
        Comment comment = new Comment("hello", user, todo);

        // then
        assertEquals("hello", comment.getContents());
        assertSame(user, comment.getUser());
        assertSame(todo, comment.getTodo());
        assertNull(comment.getId()); // 영속화 전에는 id가 null
    }

    @Test
    @DisplayName("update 호출 시 contents가 변경된다")
    void update_내용변경() {
        // given
        User user = new User("u@test.com", "pw", UserRole.USER);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment("before", user, todo);

        // when
        comment.update("after");

        // then
        assertEquals("after", comment.getContents());
    }

    @Test
    @DisplayName("JPA용 기본 생성자가 존재하고 인스턴스화 가능하다")
    void noArgsConstructor_인스턴스생성() {
        // when
        Comment comment = new Comment();

        // then
        assertNotNull(comment);
        // 기본 생성자에서는 아무 값도 안 들어가므로 null일 수 있음
        assertNull(comment.getId());
        assertNull(comment.getContents());
        assertNull(comment.getUser());
        assertNull(comment.getTodo());
    }
}