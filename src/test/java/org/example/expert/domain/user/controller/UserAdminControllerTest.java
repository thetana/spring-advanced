package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAdminControllerTest {

    private MockMvc mockMvc(UserAdminService userAdminService) {

        UserAdminController controller = new UserAdminController(userAdminService);

        ObjectMapper objectMapper = new ObjectMapper();

        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        return MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    @DisplayName("PATCH /admin/users/{userId} - 유저 권한 변경")
    void changeUserRole_success() throws Exception {

        UserAdminService userAdminService = mock(UserAdminService.class);
        MockMvc mvc = mockMvc(userAdminService);

        ObjectMapper mapper = new ObjectMapper();

        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        mvc.perform(patch("/admin/users/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userAdminService).changeUserRole(eq(1L), any(UserRoleChangeRequest.class));
    }
}