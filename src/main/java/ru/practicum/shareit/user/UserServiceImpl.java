package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ErrorHandler;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public User postUser(User user) {
        validateUser(user);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким id не существует!");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    @Override
    public User patchUser(long userId, User user) {
        patchOneUserFromAnother(user, userRepository.getReferenceById(userId));
        return userRepository.getReferenceById(userId);
    }

    public User getUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким id не существует!");
        }
        return userRepository.getReferenceById(userId);
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
//        for (User user : getAll()) {
//            if (user.getEmail().equals(testUser.getEmail())) {
//                throw new ConflictException("Юзер с таким email уже существует.");
//            }
//        }
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