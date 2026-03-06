package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AdminInterceptorTest {

    private final UserService userService = mock(UserService.class);
    private final AdminInterceptor interceptor = new AdminInterceptor(userService);

    @Test
    void preHandle_adminSuccess() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userRole", "ADMIN");
        request.setAttribute("userId", 1L);
        request.setMethod("GET");
        request.setRequestURI("/admin/test");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_notAdmin() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userRole", "USER");
        request.setAttribute("userId", 1L);

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(
                AuthException.class,
                () -> interceptor.preHandle(request, response, new Object())
        );
    }

    @Test
    void preHandle_queryExists_branch() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userRole", "ADMIN");
        request.setAttribute("userId", 1L);
        request.setMethod("GET");
        request.setRequestURI("/admin/test");
        request.setQueryString("page=1"); // query 존재

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }
}