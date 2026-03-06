package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentAdminControllerTest {

    @Test
    @DisplayName("DELETE /admin/comments/{commentId} 요청 시 service.deleteComment를 호출하고 200을 반환한다")
    void deleteComment_200_OK() throws Exception {
        // given
        CommentAdminService commentAdminService = mock(CommentAdminService.class);
        CommentAdminController controller = new CommentAdminController(commentAdminService);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        long commentId = 100L;

        // when & then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId))
                .andExpect(status().isOk());

        verify(commentAdminService, times(1)).deleteComment(commentId);
    }
}