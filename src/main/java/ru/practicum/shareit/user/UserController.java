package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController

@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserServiceImpl userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream().map(userMapper::fromUserToDto).collect(Collectors.toList());
    }

    @PostMapping
    public UserDto postUser(@RequestBody UserDto userDto) {
        return userMapper.fromUserToDto(userService.postUser(userMapper.fromDtoToUser(userDto)));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@PathVariable long userId, @RequestBody UserDto userDto) {
        return userMapper.fromUserToDto(userService.patchUser(userId, userMapper.fromDtoToUser(userDto)));
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable long userId) {
        return userMapper.fromUserToDto(userService.getUser(userId));
    }
}