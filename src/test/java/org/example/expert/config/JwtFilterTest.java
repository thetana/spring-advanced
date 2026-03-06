package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtFilter jwtFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        jwtUtil = mock(JwtUtil.class);
        objectMapper = new ObjectMapper();
        jwtFilter = new JwtFilter(jwtUtil, objectMapper);
    }

    @Test
    void authUrl_passFilter() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/signin");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void authorizationHeaderMissing() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/todos");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void claimsNull() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(null);

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void adminAccessDenied() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/test");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        when(claims.get("userRole", String.class)).thenReturn("USER");
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("user@test.com");

        jwtFilter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void validToken_passFilter() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        when(claims.get("userRole", String.class)).thenReturn("ADMIN");
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("user@test.com");

        jwtFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void jwtMalformedException() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenThrow(new MalformedJwtException("bad"));

        jwtFilter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());
    }

    @Test
    void jwtExpiredException() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");

        when(jwtUtil.extractClaims("token"))
                .thenThrow(new ExpiredJwtException(null, claims, "expired"));

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void unexpectedException() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");

        when(jwtUtil.extractClaims("token"))
                .thenThrow(new RuntimeException("boom"));

        jwtFilter.doFilter(request, response, chain);

        assertEquals(500, response.getStatus());
    }

    @Test
    void adminAccessAllowed() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/test");
        request.addHeader("Authorization", "Bearer token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);

        when(jwtUtil.substringToken("Bearer token")).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        when(claims.get("userRole", String.class)).thenReturn("ADMIN");
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("admin@test.com");

        jwtFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}