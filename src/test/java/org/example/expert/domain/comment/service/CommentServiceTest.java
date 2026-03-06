package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    @DisplayName("comment 등록 중 할일을 찾지 못하면 InvalidRequestException(Todo not found)을 던진다")
    void saveComment_할일없음_예외() {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                commentService.saveComment(authUser, todoId, request)
        );

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("comment를 정상적으로 등록한다 - 응답에 id/contents/user(email) 포함")
    void saveComment_성공() {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        User user = User.fromAuthUser(authUser);
        // User.fromAuthUser가 id/email을 어떻게 세팅하는지에 따라 id가 null일 수 있어,
        // 응답 검증에서 id는 null 허용으로 두고 email/contents 중심으로 검증합니다.
        Todo todo = new Todo("title", "title", "contents", user);

        Comment saved = new Comment(request.getContents(), user, todo);
        ReflectionTestUtils.setField(saved, "id", 100L); // 응답 id 검증을 위해 세팅

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(commentRepository.save(any(Comment.class))).willReturn(saved);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("contents", result.getContents());
        assertNotNull(result.getUser());
        assertEquals(user.getEmail(), result.getUser().getEmail());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 댓글이 있으면 CommentResponse 리스트로 매핑된다")
    void getComments_댓글있음_매핑성공() {
        // given
        long todoId = 1L;

        User user1 = new User("u1@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user1, "id", 10L);
        User user2 = new User("u2@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user2, "id", 20L);

        Todo todo = new Todo("t", "t", "c", user1);

        Comment c1 = new Comment("c1", user1, todo);
        ReflectionTestUtils.setField(c1, "id", 101L);
        Comment c2 = new Comment("c2", user2, todo);
        ReflectionTestUtils.setField(c2, "id", 102L);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(List.of(c1, c2));

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(101L, result.get(0).getId());
        assertEquals("c1", result.get(0).getContents());
        assertEquals(10L, result.get(0).getUser().getId());
        assertEquals("u1@test.com", result.get(0).getUser().getEmail());

        assertEquals(102L, result.get(1).getId());
        assertEquals("c2", result.get(1).getContents());
        assertEquals(20L, result.get(1).getUser().getId());
        assertEquals("u2@test.com", result.get(1).getUser().getEmail());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 댓글이 없으면 빈 리스트를 반환한다")
    void getComments_댓글없음_빈리스트() {
        // given
        long todoId = 1L;
        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(List.of());

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
