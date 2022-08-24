package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage, UserRepository userRepository, UserStorage userStorage1) {
        this.userRepository = userRepository;
        this.userStorage = userStorage1;
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
//    public Collection<User> getAll() {
//        return userRepository.getAll();
//    }

    @Transactional
    @Override
    public User postUser(User user) {
        validateUser(user);
        userRepository.save(user);
        return user;
    }

//    public User postUser(User user) {
//        return userRepository.addUser(user);
//    }

    public void deleteUser(long userId) {
        userStorage.deleteUser(userId);
    }

//    public User patchUser(long id, User newUser) {
//        User user = patchOneUserFromAnother(newUser, userRepository.getReferenceById(id));
//        users.replace(id, user);
//        return user;
//    }
    public User patchUser(long userId, User user) {
        return patchOneUserFromAnother(user, userRepository.getReferenceById(userId));
    }

    public User getUser(long userId) {
        return userStorage.getUser(userId);
    }

    private void validateUser(User testUser) {
        Pattern emailPattern = Pattern.compile(
                "\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*\\.\\w{2,4}");

        if (testUser.getId() < 0) {
            throw new ValidationException("Id юзера не может быть отрицательным. " +
                    "Вы пытаетесь задать id: " + testUser.getId());
        }
        if (testUser.getEmail() == null) {
            throw new ValidationException("У юзера должен быть email");
        }
        if (!emailPattern.matcher(testUser.getEmail()).matches()) {
            throw new ValidationException("Email " + testUser.getEmail() + " не соответсвтует требованиям.");
        }
        for (User user : getAll()) {
            if (user.getEmail().equals(testUser.getEmail())) {
                throw new ConflictException("Юзер с таким email уже существует.");
            }
        }
    }

    private User patchOneUserFromAnother(User donor, User recipient) {
        if (donor.getEmail() != null) {
            for (User user : getAll()) {
                if (user.getEmail().equals(donor.getEmail())) {
                    throw new ConflictException("Юзер с таким email уже существует.");
                }
            }
            recipient.setEmail(donor.getEmail());
        }
        if (donor.getName() != null) {
            recipient.setName(donor.getName());
        }
        return recipient;
    }
}