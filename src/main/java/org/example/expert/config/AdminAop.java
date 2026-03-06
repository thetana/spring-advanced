package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAop {

    private final ObjectMapper objectMapper;

    @Around("execution(* org.example.expert.domain..controller..*(..))")
    public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes())
                        .getRequest();

        String url = request.getRequestURI();
        String method = request.getMethod();
        Long userId = (Long) request.getAttribute("userId");

        LocalDateTime requestTime = LocalDateTime.now();

        String requestJson = extractRequestBody(joinPoint);

        Object result = joinPoint.proceed();

        Object body = result;
        if (result instanceof ResponseEntity<?> responseEntity) {
            body = responseEntity.getBody();
        }

        String responseJson = objectMapper.writeValueAsString(body);

        log.info(
                "ADMIN_API userId={} time={} {} {} request={} response={}",
                userId,
                requestTime,
                method,
                url,
                requestJson,
                responseJson
        );

        return result;
    }

    private String extractRequestBody(ProceedingJoinPoint joinPoint) {

        try {
            for (Object arg : joinPoint.getArgs()) {

                if (arg == null) continue;

                if (arg instanceof HttpServletRequest
                        || arg instanceof HttpServletResponse
                        || arg instanceof org.springframework.validation.BindingResult) {
                    continue;
                }

                return objectMapper.writeValueAsString(arg);
            }

        } catch (Exception e) {
            return "requestBody parse error";
        }

        return "{}";
    }
}
