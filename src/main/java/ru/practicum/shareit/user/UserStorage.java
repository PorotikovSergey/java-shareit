package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Repository
@Qualifier("userStorage")
public class UserStorage {
    private Map<Long, User> users = new HashMap();

    public Collection<User> getAll() {
        return users.values();
    }

    public User addUser(User user) {
        validateUser(user);
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
        validateUser(newUser);
        return users.replace(id, newUser);
    }

//=======================================================================

    private void validateUser(User testUser) {
        Pattern emailPattern = Pattern.compile(
                "\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*\\.\\w{2,4}");

        if (testUser.getId() < 0) {
            throw new ValidationException("Id юзера не может быть отрицательным. " +
                    "Вы пытаетесь задать id: " + testUser.getId());
        }
        if (!emailPattern.matcher(testUser.getEmail()).matches()) {
            throw new ValidationException("Email " + testUser.getEmail() + " не соответсвтует требованиям.");
        }
        for (User user: users.values()) {
            if(user.getEmail().equals(testUser.getEmail())) {
                throw new ValidationException("Юзер с таким email уже существует.");
            }
        }
    }
}
