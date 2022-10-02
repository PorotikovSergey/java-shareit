package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.mapper.Mapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserServiceImpl userService;
    @MockBean
    Mapper userMapper;

    @Autowired
    private MockMvc mvc;

    List<User> list = new ArrayList<>();

    User user;
    UserDto userDto;
    User user2;
    UserDto userDto2;

    @BeforeEach
    void setUp() {
        user = new User(
                1L,
                "John",
                "john.doe@mail.com"
        );

        userDto = new UserDto(
                1L,
                "John",
                "john.doe@mail.com"
        );

        user2 = new User(
                2L,
                "John2",
                "john2@mail.com"
        );

        userDto2 = new UserDto(
                2L,
                "John2",
                "john2@mail.com"
        );

        list.add(user);
        list.add(user2);

    }

    @Test
    void saveNewUser() throws Exception {

        when(userMapper.fromUserToDto(any()))
                .thenReturn(userDto);
        when(userService.postUser(any()))
                .thenReturn(user);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));

        verify(userService, times(1)).postUser(any());
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getAll() throws Exception {
        when(userMapper.fromUserToDto(user))
                .thenReturn(userDto);
        when(userMapper.fromUserToDto(user2))
                .thenReturn(userDto2);
        when(userService.getAll())
                .thenReturn(list);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[0].email", is("john.doe@mail.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("John2")))
                .andExpect(jsonPath("$[1].email", is("john2@mail.com")));

        verify(userService, times(1)).getAll();
        verifyNoMoreInteractions(userService);

    }

    @Test
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser(user.getId());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1);
        verifyNoMoreInteractions(userService);
    }


    @Test
    void patchUser() throws Exception {
        when(userMapper.fromUserToDto(any()))
                .thenReturn(userDto);
        when(userService.patchUser(1L, user))
                .thenReturn(user);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));

        verify(userService, times(1)).patchUser(1L, null);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUser() throws Exception {
        when(userMapper.fromUserToDto(user2))
                .thenReturn(userDto2);
        when(userService.getUser(anyLong()))
                .thenReturn(user2);

        mvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user2.getName())))
                .andExpect(jsonPath("$.email", is(user2.getEmail())));

        verify(userService, times(1)).getUser(anyLong());
        verifyNoMoreInteractions(userService);
    }
}
