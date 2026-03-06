package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_success() {

        SignupRequest request = new SignupRequest(
                "user@test.com",
                "password",
                "USER"
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        User savedUser = new User("user@test.com", "encoded", UserRole.USER);
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.createToken(1L, "user@test.com", UserRole.USER))
                .thenReturn("Bearer token");

        SignupResponse response = authService.signup(request);

        assertEquals("Bearer token", response.getBearerToken());
    }

    @Test
    void signup_emailAlreadyExists() {

        SignupRequest request = new SignupRequest(
                "user@test.com",
                "password",
                "USER"
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(
                InvalidRequestException.class,
                () -> authService.signup(request)
        );
    }

    @Test
    void signin_userNotFound() {

        SigninRequest request = new SigninRequest("user@test.com", "password");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRequestException.class,
                () -> authService.signin(request)
        );
    }

    @Test
    void signin_wrongPassword() {

        SigninRequest request = new SigninRequest("user@test.com", "password");

        User user = new User("user@test.com", "encoded", UserRole.USER);

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded"))
                .thenReturn(false);

        assertThrows(
                AuthException.class,
                () -> authService.signin(request)
        );
    }

    @Test
    void signin_success() {

        SigninRequest request = new SigninRequest("user@test.com", "password");

        User user = new User("user@test.com", "encoded", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded"))
                .thenReturn(true);

        when(jwtUtil.createToken(1L, "user@test.com", UserRole.USER))
                .thenReturn("Bearer token");

        SigninResponse response = authService.signin(request);

        assertEquals("Bearer token", response.getBearerToken());
    }
}