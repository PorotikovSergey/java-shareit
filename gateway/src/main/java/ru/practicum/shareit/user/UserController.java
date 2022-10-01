package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController

@RequestMapping(path = "/users")
public class UserController {
    private final UserClient userClient;

    @Autowired
    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping
    public List<ResponseEntity<Object>> getAll() {
        System.out.println("into --> controller getAll");
        return userClient.getAll();
    }

    @PostMapping
    public ResponseEntity<Object> postUser(@RequestBody @Valid UserDto userDto) {
        System.out.println("into --> controller postUser");
        return userClient.postUser(userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        System.out.println("into --> controller deleteUser");
        userClient.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> patchUser(@PathVariable long userId, @RequestBody @Valid UserDto userDto) {
        System.out.println("into --> controller patchUser");
        return userClient.patchUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable long userId) {
        System.out.println("into --> controller getUser");
        return userClient.getUser(userId);
    }
}