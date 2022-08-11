package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserService {
    public Collection<UserDto> getAll();

    public UserDto postUser(User user);

    public void deleteUser(long userId);

    public UserDto patchUser(long userId, User user);

    public UserDto getUser(long userId);
}
