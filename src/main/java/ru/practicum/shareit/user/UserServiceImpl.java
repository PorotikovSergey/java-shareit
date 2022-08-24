package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User postUser(User user) {
        return userStorage.addUser(user);
    }

    public void deleteUser(long userId) {
        userStorage.deleteUser(userId);
    }

    public User patchUser(long userId, User user) {
        return userStorage.patchUser(userId, user);
    }

    public User getUser(long userId) {
        return userStorage.getUser(userId);
    }
}