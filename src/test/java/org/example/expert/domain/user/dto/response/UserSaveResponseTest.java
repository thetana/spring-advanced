package org.example.expert.domain.user.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSaveResponseTest {

    @Test
    @DisplayName("UserSaveResponse 생성자 및 getter 테스트")
    void constructor_and_getter() {

        String token = "Bearer test-token";

        UserSaveResponse response = new UserSaveResponse(token);

        assertEquals(token, response.getBearerToken());
    }
}