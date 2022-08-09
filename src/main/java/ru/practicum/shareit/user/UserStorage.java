package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@Qualifier("userStorage")
public class UserStorage {
    private Map<Long,User> users = new HashMap();

    public Collection<User> getAll() {
        return  users.values();
    }

    public User addUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public User getUser(long id) {
        return users.get(id);
    }

    public void deleteUser(long id) {
        users.remove(id);
    }

    public User patchUser(long id, User newUser) {
        return users.replace(id, newUser);
    }
}
