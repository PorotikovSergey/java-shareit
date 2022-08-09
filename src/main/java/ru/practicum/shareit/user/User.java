package ru.practicum.shareit.user;

import lombok.Data;
import lombok.NonNull;

/**
 * // TODO .
 */
@Data
public class User {
    private long id;
    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.id = UserIdManager.getUserId();
    }
}


