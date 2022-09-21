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
    @Override
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
            for (User user : getAll()) {
                if (user.getEmail().equals(donor.getEmail())) {
                    throw new ConflictException("Юзер с таким email " + user.getEmail() + " уже существует.");
                }
            }
            recipient.setEmail(donor.getEmail());
        }
        if (donor.getName() != null) {
            recipient.setName(donor.getName());
        }
        userRepository.save(recipient);
        return recipient;
    }
}