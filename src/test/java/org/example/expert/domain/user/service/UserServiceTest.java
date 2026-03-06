package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_success() {

        User user = new User("user@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L);

        assertEquals(1L, response.getId());
        assertEquals("user@test.com", response.getEmail());
    }

    @Test
    void getUser_notFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> userService.getUser(1L)
        );

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void changePassword_samePassword_exception() {

        User user = new User("user@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("encodedPassword", "encodedPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("encodedPassword", user.getPassword())).thenReturn(true);

        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> userService.changePassword(1L, request)
        );

        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", ex.getMessage());
    }

    @Test
    void changePassword_oldPasswordMismatch_exception() {

        User user = new User("user@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("newPassword", user.getPassword()))
                .thenReturn(false);

        when(passwordEncoder.matches("oldPassword", user.getPassword()))
                .thenReturn(false);

        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> userService.changePassword(1L, request)
        );

        assertEquals("잘못된 비밀번호입니다.", ex.getMessage());
    }

    @Test
    void changePassword_success() {

        User user = new User("user@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("newPassword", user.getPassword()))
                .thenReturn(false);

        when(passwordEncoder.matches("oldPassword", user.getPassword()))
                .thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        assertDoesNotThrow(() ->
                userService.changePassword(1L, request)
        );

        verify(passwordEncoder).encode("newPassword");
    }
}