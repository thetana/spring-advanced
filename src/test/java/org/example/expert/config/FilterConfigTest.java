package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FilterConfigTest {

    @Test
    void jwtFilterBeanRegistration() {

        JwtUtil jwtUtil = mock(JwtUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();

        FilterConfig filterConfig = new FilterConfig(jwtUtil, objectMapper);

        FilterRegistrationBean<JwtFilter> bean = filterConfig.jwtFilter();

        assertNotNull(bean);
        assertNotNull(bean.getFilter());
        assertTrue(bean.getFilter() instanceof JwtFilter);
        assertTrue(bean.getUrlPatterns().contains("/*"));
    }
}