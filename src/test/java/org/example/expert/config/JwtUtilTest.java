package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Base64 인코딩된 256bit key
    private static final String TEST_SECRET =
            "c2VjdXJldGVzdGtleXNlY3JldHRlc3RrZXlzZWNyZXR0ZXN0a2V5";

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);

        jwtUtil.init();
    }

    @Test
    void createToken_success() {

        String token = jwtUtil.createToken(
                1L,
                "user@test.com",
                UserRole.USER
        );

        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    void substringToken_success() {

        String token = "Bearer abc.def.ghi";

        String result = jwtUtil.substringToken(token);

        assertEquals("abc.def.ghi", result);
    }

    @Test
    void substringToken_exception() {

        assertThrows(
                ServerException.class,
                () -> jwtUtil.substringToken("invalidToken")
        );
    }

    @Test
    void substringToken_nullToken_exception() {

        assertThrows(
                ServerException.class,
                () -> jwtUtil.substringToken(null)
        );
    }

    @Test
    void substringToken_emptyToken_exception() {

        assertThrows(
                ServerException.class,
                () -> jwtUtil.substringToken("")
        );
    }

    @Test
    void extractClaims_success() {

        String token = jwtUtil.createToken(
                1L,
                "user@test.com",
                UserRole.USER
        );

        String pureToken = jwtUtil.substringToken(token);

        Claims claims = jwtUtil.extractClaims(pureToken);

        assertEquals("1", claims.getSubject());
        assertEquals("user@test.com", claims.get("email"));
    }
}