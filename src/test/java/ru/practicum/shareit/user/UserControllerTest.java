package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.requests.user.*;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
//@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    ObjectMapper mapper;
    //    @MockBean
//    UserMapper userMapper;
    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    private User user = new User(
            1L,
            "Bob",
            "bob@mail.ru");

    @Test
    void postUser() throws Exception {
        when(userService.postUser(any()))
                .thenReturn(user);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
//
//        verify(userService, times(1)).postUser(user);
    }
}

//    void getAll() throws Exception {
//        assertEquals(1, 1);
//        when(userService.getAll()).thenReturn(Collections.emptyList());
//
//        mockMvc.perform(get("/users"))
//                .andExpect(status().isOk())
//                .andExpect(content().json("[]"));
//
//        verify(userService, times(1)).getAll();


//    @Test
//    void postUser() {
//    }
//
//    @Test
//    void deleteUser() {
//    }
//
//    @Test
//    void patchUser() {
//    }
//
//    @Test
//    void getUser() {
//    }
//}