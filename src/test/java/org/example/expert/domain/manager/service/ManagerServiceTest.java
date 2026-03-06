package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
        // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    @DisplayName("saveManager: Todo가 없으면 'Todo not found' 예외")
    void saveManager_todoNotFound() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        ManagerSaveRequest req = new ManagerSaveRequest(2L);

        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, req));

        // then
        assertEquals("Todo not found", ex.getMessage());
    }

    @Test
    @DisplayName("saveManager: 일정 작성자가 아닌 유저가 담당자를 지정하면 예외")
    void saveManager_notTodoOwner() {
        // given
        AuthUser authUser = new AuthUser(1L, "owner@a.com", UserRole.USER);
        User caller = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(caller, "id", 1L);

        // todo 작성자 id를 다르게 세팅
        User realOwner = new User("real@a.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(realOwner, "id", 99L);

        long todoId = 10L;
        Todo todo = new Todo("t", "t", "c", realOwner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        ManagerSaveRequest req = new ManagerSaveRequest(2L);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, req));

        // then
        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("saveManager: 등록하려는 담당자 유저가 없으면 예외")
    void saveManager_managerUserNotFound() {
        // given
        AuthUser authUser = new AuthUser(1L, "owner@a.com", UserRole.USER);
        User owner = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(owner, "id", 1L);

        long todoId = 10L;
        Todo todo = new Todo("t", "t", "c", owner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 2L;
        ManagerSaveRequest req = new ManagerSaveRequest(managerUserId);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(userRepository.findById(managerUserId)).thenReturn(Optional.empty());

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, req));

        // then
        assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("saveManager: 일정 작성자가 본인을 담당자로 등록하려 하면 예외")
    void saveManager_ownerCannotBeManager() {
        // given
        AuthUser authUser = new AuthUser(1L, "owner@a.com", UserRole.USER);
        User owner = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(owner, "id", 1L);

        long todoId = 10L;
        Todo todo = new Todo("t", "t", "c", owner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        // managerUser를 owner와 같은 id로 세팅
        long managerUserId = 1L;
        User managerUser = new User("owner@a.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", 1L);

        ManagerSaveRequest req = new ManagerSaveRequest(managerUserId);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(userRepository.findById(managerUserId)).thenReturn(Optional.of(managerUser));

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, todoId, req));

        // then
        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("getManagers: 담당자 목록이 비어있으면 빈 리스트 반환")
    void getManagers_emptyList() {
        // given
        long todoId = 1L;
        User owner = new User("o@o.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", 1L);

        Todo todo = new Todo("t", "t", "c", owner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(managerRepository.findByTodoIdWithUser(todoId)).thenReturn(List.of());

        // when
        List<ManagerResponse> result = managerService.getManagers(todoId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("deleteManager: user가 없으면 'User not found' 예외")
    void deleteManager_userNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 10L, 100L));

        // then
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    @DisplayName("deleteManager: todo가 없으면 'Todo not found' 예외")
    void deleteManager_todoNotFound() {
        // given
        User user = new User("u@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoRepository.findById(10L)).thenReturn(Optional.empty());

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 10L, 100L));

        // then
        assertEquals("Todo not found", ex.getMessage());
    }

    @Test
    @DisplayName("deleteManager: 일정 작성자 유효성 실패(todo.user null)면 예외")
    void deleteManager_invalidTodoOwner_nullUser() {
        // given
        User user = new User("u@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "id", 10L);
        ReflectionTestUtils.setField(todo, "user", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 10L, 100L));

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteManager: 일정 작성자 유효성 실패(작성자 불일치)면 예외")
    void deleteManager_invalidTodoOwner_mismatch() {
        // given
        User caller = new User("caller@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(caller, "id", 1L);

        User realOwner = new User("owner@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(realOwner, "id", 99L);

        Todo todo = new Todo("t", "t", "c", realOwner);
        ReflectionTestUtils.setField(todo, "id", 10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(caller));
        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 10L, 100L));

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteManager: manager가 없으면 'Manager not found' 예외")
    void deleteManager_managerNotFound() {
        // given
        User owner = new User("owner@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", 1L);

        Todo todo = new Todo("t", "t", "c", owner);
        ReflectionTestUtils.setField(todo, "id", 10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(100L)).thenReturn(Optional.empty());

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 10L, 100L));

        // then
        assertEquals("Manager not found", ex.getMessage());
    }

    @Test
    @DisplayName("deleteManager: manager가 다른 todo에 속해 있으면 예외")
    void deleteManager_managerNotInTodo() {
        // given
        User owner = new User("owner@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", 1L);

        Todo todo = new Todo("t", "t", "c", owner);
        ReflectionTestUtils.setField(todo, "id", 10L);

        // manager가 가진 todo id를 다르게 세팅
        Todo otherTodo = new Todo("ot", "ot", "oc", owner);
        ReflectionTestUtils.setField(otherTodo, "id", 999L);

        Manager manager = new Manager(owner, otherTodo);
        ReflectionTestUtils.setField(manager, "id", 100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(100L)).thenReturn(Optional.of(manager));

        // when
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 10L, 100L));

        // then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteManager: 정상 케이스면 managerRepository.delete(manager)를 호출한다")
    void deleteManager_success() {
        // given
        User owner = new User("owner@u.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", 1L);

        Todo todo = new Todo("t", "t", "c", owner);
        ReflectionTestUtils.setField(todo, "id", 10L);

        User managerUser = new User("m@m.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", 2L);

        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", 100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(100L)).thenReturn(Optional.of(manager));

        // when
        managerService.deleteManager(1L, 10L, 100L);

        // then
        verify(managerRepository, times(1)).delete(manager);
    }
}
