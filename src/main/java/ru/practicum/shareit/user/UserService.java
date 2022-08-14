package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserService {
    Collection<User> getAll();

    User postUser(User user);

    void deleteUser(long userId);

    User patchUser(long userId, User user);

    User getUser(long userId);
}