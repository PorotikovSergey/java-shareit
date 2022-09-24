package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;
    private final Mapper mapper;

    @Autowired
    public UserController(UserServiceImpl userService, Mapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream().map(mapper::fromUserToDto).collect(Collectors.toList());
    }

    @PostMapping
    public UserDto postUser(@Valid @RequestBody UserDto userDto) {
        return mapper.fromUserToDto(userService.postUser(mapper.fromDtoToUser(userDto)));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@PathVariable long userId, @Valid @RequestBody UserDto userDto) {
        return mapper.fromUserToDto(userService.patchUser(userId, mapper.fromDtoToUser(userDto)));
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable long userId) {
        return mapper.fromUserToDto(userService.getUser(userId));
    }
}