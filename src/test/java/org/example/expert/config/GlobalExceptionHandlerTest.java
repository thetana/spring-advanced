package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void invalidRequestException() {

        InvalidRequestException ex = new InvalidRequestException("invalid");

        ResponseEntity<Map<String, Object>> response =
                handler.invalidRequestExceptionException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid", response.getBody().get("message"));
    }

    @Test
    void authException() {

        AuthException ex = new AuthException("auth error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleAuthException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("auth error", response.getBody().get("message"));
    }

    @Test
    void serverException() {

        ServerException ex = new ServerException("server error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleServerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("server error", response.getBody().get("message"));
    }

    @Test
    void methodArgumentNotValidException() {

        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError =
                new FieldError("obj", "field", "validation error");

        when(bindingResult.getFieldError()).thenReturn(fieldError);

        MethodParameter parameter = mock(MethodParameter.class);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.methodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("validation error", response.getBody().get("message"));
    }

    @Test
    void getErrorResponse() {

        ResponseEntity<Map<String, Object>> response =
                handler.getErrorResponse(HttpStatus.BAD_REQUEST, "error");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();

        assertEquals("BAD_REQUEST", body.get("status"));
        assertEquals(400, body.get("code"));
        assertEquals("error", body.get("message"));
    }
}