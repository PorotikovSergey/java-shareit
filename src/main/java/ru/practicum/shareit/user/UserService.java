package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserMapper userMapper, UserStorage userStorage) {
        this.userMapper = userMapper;
        this.userStorage = userStorage;
    }

    public Collection<UserDto> getAll() {
        return userStorage.getAll().stream().map(userMapper::fromUserToDto).collect(Collectors.toList());
    }

    public UserDto postUser(User user) {
        return userMapper.fromUserToDto(userStorage.addUser(user));
    }

    public void deleteUser(long userId) {
        userStorage.deleteUser(userId);
    }

    public UserDto patchUser(long userId, User user) {
        return userMapper.fromUserToDto(userStorage.patchUser(userId, user));
    }

    public UserDto getUser(long userId) {
        return userMapper.fromUserToDto(userStorage.getUser(userId));
    }
}
