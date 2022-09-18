package ru.practicum.shareit.user;

import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {
    @Mock
    UserRepository userRepository;

    UserServiceImpl userService;

    User user1;
    User user2;
    User user3;

    User user4;

    List<User> list = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "Bob", "bobby@mail.ru");
        user2 = new User(2L, "John", "johny@ya.ru");
        user3 = new User(3L, "Mary", "marianna@hotmail.com");

        user4 = new User(4L, "Anastasia", "nastyGirl@gmail.com");

        list.add(user1);
        list.add(user2);
        list.add(user3);

        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void getAll() {
        Mockito
                .when(userRepository.findAll()).thenReturn(list);
        List<User> testList = userService.getAll();
        assertNotNull(testList);
        assertEquals(3, testList.size());
        assertTrue(testList.contains(user1));
        assertTrue(testList.contains(user2));
        assertTrue(testList.contains(user3));
        assertFalse(testList.contains(user4));
    }

    @Test
    void deleteUser() {
        Mockito
                .doNothing().when(userRepository).deleteById(anyLong());
        userService.deleteUser(4L);
    }

    @Test
    void patchUser() {
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user3));
        Mockito
                .when(userRepository.save(user3)).thenReturn(user4);

        User testUser = userService.patchUser(3L, user4);

        assertNotNull(testUser);
        assertEquals(3L, testUser.getId());
        assertEquals("nastyGirl@gmail.com", testUser.getEmail());
    }

    @Test
    void getUser() {
        Mockito
                .when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        User testUser = userService.getUser(2L);
        assertNotNull(testUser);
        assertEquals(2L, testUser.getId());
        assertEquals("johny@ya.ru", testUser.getEmail());
        assertEquals("John", testUser.getName());
    }

    @Test
    void postUser() {
        Mockito.when(userRepository.save(user1)).thenReturn(user1);

        User testUser = userService.postUser(user1);
        assertNotNull(testUser);
        assertEquals(1L, testUser.getId());
        assertEquals("bobby@mail.ru", testUser.getEmail());
        assertEquals("Bob", testUser.getName());
    }

}