package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User postUser(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("У юзера должен быть email");
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public User patchUser(long userId, User user) {
        return patchOneUserFromAnother(user, userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет")));
    }

    public User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет"));
    }

    private User patchOneUserFromAnother(User donor, User recipient) {
        if (donor.getEmail() != null) {
            if (userRepository.findUserByEmail(donor.getEmail()) != null) {
                throw new ConflictException("Юзер с таким email " + donor.getEmail() + " уже существует.");
            }
            recipient.setEmail(donor.getEmail());
        }
        if (donor.getName() != null) {
            recipient.setName(donor.getName());
        }
        return recipient;
    }

}
