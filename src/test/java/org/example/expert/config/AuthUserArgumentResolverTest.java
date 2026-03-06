package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserArgumentResolverTest {

    private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    static class TestController {

        public void valid(@Auth AuthUser authUser) {
        }

        public void invalidAnnotationOnly(@Auth String value) {
        }

        public void invalidTypeOnly(AuthUser authUser) {
        }
    }

    @Test
    void supportsParameter_valid() throws Exception {

        Method method = TestController.class.getMethod("valid", AuthUser.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        boolean result = resolver.supportsParameter(parameter);

        assertTrue(result);
    }

    @Test
    void supportsParameter_annotationOnly_exception() throws Exception {

        Method method = TestController.class.getMethod("invalidAnnotationOnly", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertThrows(AuthException.class,
                () -> resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_typeOnly_exception() throws Exception {

        Method method = TestController.class.getMethod("invalidTypeOnly", AuthUser.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertThrows(AuthException.class,
                () -> resolver.supportsParameter(parameter));
    }

    @Test
    void resolveArgument_success() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);
        request.setAttribute("email", "user@test.com");
        request.setAttribute("userRole", "USER");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        AuthUser authUser = (AuthUser) result;

        assertEquals(1L, authUser.getId());
        assertEquals("user@test.com", authUser.getEmail());
        assertEquals(UserRole.USER, authUser.getUserRole());
    }
}