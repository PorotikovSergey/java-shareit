package ru.practicum.shareit.requests.user;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User postUser(User user);

    void deleteUser(long userId);

    User patchUser(long userId, User user);

    User getUser(long userId);
}