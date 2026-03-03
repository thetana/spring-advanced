package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AdminAop {
    @Around("execution(* org.example.expert.domain.user.service.UserAdminService.*(..)) || execution(* org.example.expert.domain.comment.service.CommentAdminService.*(..))")
    public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes())
                        .getRequest();
        String url = request.getRequestURI();
        String method = request.getMethod();
        Long userId = (Long) request.getAttribute("userId"); // JWT 필터에서 넣었다고 가정

        Object[] args = joinPoint.getArgs();

        LocalDateTime requestTime = LocalDateTime.now();

        log.info("API 요청 시작");
        log.info("userId: {}", userId);
        log.info("time: {}", requestTime);
        log.info("url: {} {}", method, url);
        log.info("requestBody: {}", Arrays.toString(args));
        Object result = joinPoint.proceed(); // 실제 메서드 실행 -> Filter에서 doFilter 와 비슷함.

        log.info("responseBody: {}", result);

        return result;
    }
}
