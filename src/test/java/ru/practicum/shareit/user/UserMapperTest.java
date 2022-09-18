package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    UserMapper mapper = new UserMapper();

    User user = new User(1L, "Bob", "bob@mail.ru");
    UserDto userDto = new UserDto(2L, "Mary", "mary@ya.ru");

    @Test
    void fromUserToDto() {
        UserDto newDto = mapper.fromUserToDto(user);

        assertNotNull(newDto);
        assertEquals(1L, newDto.getId());
        assertEquals("Bob", newDto.getName());
        assertEquals("bob@mail.ru", newDto.getEmail());
    }

    @Test
    void fromDtoToUser() {
        User newUser = mapper.fromDtoToUser(userDto);

        assertNotNull(newUser);
        assertEquals(2L, newUser.getId());
        assertEquals("Mary", newUser.getName());
        assertEquals("mary@ya.ru", newUser.getEmail());
    }
}