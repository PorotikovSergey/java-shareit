package ru.practicum.shareit.requests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RequestRepositoryTest {

    @Autowired
    RequestRepository requestRepository;
    @Autowired
    UserRepository userRepository;

    Request requestOne;
    Request requestTwo;

    @BeforeEach
    void beforeEach() {
        User requestor1 = new User(1L, "bob", "bob@mail.ru");
        User requestor2 = new User(2L, "bob2", "bob2@mail.ru");

        userRepository.save(requestor1);
        userRepository.save(requestor2);

        requestOne = new Request(1L, "request-1", Date.from(Instant.now()), requestor1, null);
        requestTwo = new Request(2L, "request-2", Date.from(Instant.now()), requestor2, null);

        requestRepository.save(requestOne);
        requestRepository.save(requestTwo);
    }

    @Test
    void findRequestsByRequestor() {
        List<Request> list = requestRepository.findRequestsByRequestorId(2L);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("request-2", list.get(0).getDescription());
    }

    @AfterEach
    void afterEach() {
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }
}