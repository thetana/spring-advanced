package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminAopTest {

    private AdminAop adminAop;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {

        objectMapper = spy(new ObjectMapper());
        adminAop = new AdminAop(objectMapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/test");
        request.setMethod("POST");
        request.setAttribute("userId", 1L);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void responseEntity_branch() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"test"});
        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok("ok"));

        Object result = adminAop.executionTime(joinPoint);

        assertEquals(ResponseEntity.ok("ok"), result);
    }

    @Test
    void plainObject_branch() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"hello"});
        when(joinPoint.proceed()).thenReturn("result");

        Object result = adminAop.executionTime(joinPoint);

        assertEquals("result", result);
    }

    @Test
    void nullArg_branch() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{null});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = adminAop.executionTime(joinPoint);

        assertEquals("ok", result);
    }

    @Test
    void servletObject_skip_branch() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        BindingResult binding = mock(BindingResult.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{req, res, binding});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = adminAop.executionTime(joinPoint);

        assertEquals("ok", result);
    }

    @Test
    void objectMapper_exception_branch() throws Throwable {

        ObjectMapper mapper = mock(ObjectMapper.class);
        AdminAop aop = new AdminAop(mapper);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/test");
        request.setMethod("POST");
        request.setAttribute("userId", 1L);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"data"});
        when(joinPoint.proceed()).thenReturn("ok");

        when(mapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException())
                .thenReturn("{}");

        Object result = aop.executionTime(joinPoint);

        assertEquals("ok", result);
    }
}