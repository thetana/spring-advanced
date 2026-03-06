package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @Auth AuthUser 파라미터를 테스트에서 주입하기 위한 Resolver
     * (실제 구현이 어떤 방식이든, 컨트롤러 테스트에서는 "AuthUser가 들어왔다"고만 만들면 됨)
     */
    private static class TestAuthUserArgumentResolver implements HandlerMethodArgumentResolver {
        private final AuthUser authUser;

        private TestAuthUserArgumentResolver(AuthUser authUser) {
            this.authUser = authUser;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(AuthUser.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            return authUser;
        }
    }

    private MockMvc mockMvc(CommentService commentService, AuthUser authUser) {
        CommentController controller = new CommentController(commentService);

        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver(authUser))
                .build();
    }

    @Test
    @DisplayName("POST /todos/{todoId}/comments - 댓글 저장 성공 시 200 OK와 응답 바디를 반환한다")
    void saveComment_200_OK() throws Exception {
        // given
        CommentService commentService = mock(CommentService.class);
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);

        MockMvc mvc = mockMvc(commentService, authUser);

        long todoId = 10L;
        CommentSaveRequest request = new CommentSaveRequest("hello");

        CommentSaveResponse response =
                new CommentSaveResponse(100L, "hello", new org.example.expert.domain.user.dto.response.UserResponse(1L, "a@a.com"));

        when(commentService.saveComment(eq(authUser), eq(todoId), any(CommentSaveRequest.class)))
                .thenReturn(response);

        // when & then
        mvc.perform(post("/todos/{todoId}/comments", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.contents").value("hello"))
                .andExpect(jsonPath("$.user.email").value("a@a.com"));

        verify(commentService, times(1)).saveComment(eq(authUser), eq(todoId), any(CommentSaveRequest.class));
    }

    @Test
    @DisplayName("GET /todos/{todoId}/comments - 댓글 목록 조회 성공 시 200 OK와 리스트를 반환한다")
    void getComments_200_OK() throws Exception {
        // given
        CommentService commentService = mock(CommentService.class);
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER); // GET은 사용하지 않지만 resolver 세팅 통일

        MockMvc mvc = mockMvc(commentService, authUser);

        long todoId = 10L;

        List<CommentResponse> responses = List.of(
                new CommentResponse(1L, "c1", new org.example.expert.domain.user.dto.response.UserResponse(10L, "u1@test.com")),
                new CommentResponse(2L, "c2", new org.example.expert.domain.user.dto.response.UserResponse(20L, "u2@test.com"))
        );

        when(commentService.getComments(todoId)).thenReturn(responses);

        // when & then
        mvc.perform(get("/todos/{todoId}/comments", todoId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].contents").value("c1"))
                .andExpect(jsonPath("$[0].user.email").value("u1@test.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].contents").value("c2"))
                .andExpect(jsonPath("$[1].user.email").value("u2@test.com"));

        verify(commentService, times(1)).getComments(todoId);
    }
}