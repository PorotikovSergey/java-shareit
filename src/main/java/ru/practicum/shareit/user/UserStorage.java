package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Repository
@Qualifier("userStorage")
public class UserStorage {
    private UserMapper userMapper;
    private Map<Long, User> users = new HashMap();

    public Collection<User> getAll() {
        return users.values();
    }

    public User addUser(User user) {
        validateUser(user);
        user.setId(UserIdManager.getUserId());
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
        User user = patchOneUserFromAnother(newUser, users.get(id));
        users.replace(id, user);
        return user;
    }

//=======================================================================

    private void validateUser(User testUser) {
        Pattern emailPattern = Pattern.compile(
                "\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*\\.\\w{2,4}");

        if (testUser.getId() < 0) {
            throw new ValidationException("Id юзера не может быть отрицательным. " +
                    "Вы пытаетесь задать id: " + testUser.getId());
        }
        if (testUser.getEmail()==null) {
            throw new ValidationException("У юзера должен быть email");
        }
        if (!emailPattern.matcher(testUser.getEmail()).matches()) {
            throw new ValidationException("Email " + testUser.getEmail() + " не соответсвтует требованиям.");
        }
        for (User user: users.values()) {
            if(user.getEmail().equals(testUser.getEmail())) {
                throw new ConflictException("Юзер с таким email уже существует.");
            }
        }
    }

    private User patchOneUserFromAnother(User donor, User recipient) {
        if(donor.getEmail()!=null) {
            for (User user: users.values()) {
                if(user.getEmail().equals(donor.getEmail())) {
                    throw new ConflictException("Юзер с таким email уже существует.");
                }
            }
            recipient.setEmail(donor.getEmail());
        }
        if(donor.getName()!=null) {
            recipient.setName(donor.getName());
        }
        return recipient;
    }
}
