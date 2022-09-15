//package ru.practicum.shareit.requests;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.time.Instant;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//class RequestRepositoryTest {
//
//    @Autowired
//    RequestRepository requestRepository;
//
//    Request requestOne;
//    Request requestTwo;
//
//    @BeforeEach
//    void beforeEach() {
//        requestOne = new Request(1L, "request-1", 1, Date.from(Instant.now()), null);
//        requestTwo = new Request(2L, "request-2", 2, Date.from(Instant.now()), null);
//
//        requestRepository.save(requestOne);
//        requestRepository.save(requestTwo);
//    }
//
//    @Test
//    void findRequestsByRequestor() {
//        List<Request> list = requestRepository.findRequestsByRequestor(2L);
//        assertNotNull(list);
//        assertEquals(1, list.size());
//        assertEquals("request-2", list.get(0).getDescription());
//    }
//
//    @AfterEach
//    void afterEach() {
//        requestRepository.deleteAll();
//    }
//}