package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor  implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserRole role = UserRole.of((String) request.getAttribute("userRole"));
        Object id = request.getAttribute("userId");
        if(UserRole.ADMIN != role){
            throw new AuthException("권한이 없습니다.");
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUrl = (query == null) ? uri : (uri + "?" + query);

        Instant now = Instant.now();

        log.info("[ADMIN AUTH OK] time={} userId={} role={} {} {}",
                now, id, role, method, fullUrl);

        return true;
    }
}