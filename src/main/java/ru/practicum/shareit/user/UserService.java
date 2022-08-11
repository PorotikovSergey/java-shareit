package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

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
        Collection<UserDto> resultCollection = new ArrayList<>();
        for(User user: userStorage.getAll()) {
            resultCollection.add(userMapper.fromUserToDto(user));
        }
        return resultCollection;
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
