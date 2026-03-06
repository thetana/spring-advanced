package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void changeUserRole_userNotFound() {

        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> userAdminService.changeUserRole(1L, request)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void changeUserRole_success() {

        User user = new User("user@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userAdminService.changeUserRole(1L, request);

        assertEquals(UserRole.ADMIN, user.getUserRole());
    }
}